/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.protocol.jarjar;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.jboss.logging.Logger;

/**
 * Provide an input stream in jar format which is build up
 * using a subset of a jar file.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JarJarInputStream extends InputStream
{
   private static final Logger log = Logger.getLogger(JarJarInputStream.class);
   
   private PipedInputStream pipedInputStream;
   private PipedOutputStream pipedOutputStream;
   
   private Thread writingThread;
   
   /**
    * @param jarFile        the jar file to read from
    * @param rootEntry      the root entry at which the new jar file starts
    * @throws IOException
    */
   protected JarJarInputStream(final JarFile jarFile, final JarEntry rootEntry) throws IOException
   {
      this.pipedInputStream = new PipedInputStream();
      this.pipedOutputStream = new PipedOutputStream(pipedInputStream);
      
      Manifest mf = jarFile.getManifest();
      final JarOutputStream jos;
      if(mf != null)
         jos = new JarOutputStream(pipedOutputStream, mf);
      else
         jos = new JarOutputStream(pipedOutputStream);
      
      this.writingThread = new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               Enumeration<JarEntry> entries = jarFile.entries();
               while(entries.hasMoreElements())
               {
                  JarEntry entry = entries.nextElement();
                  if (isRelativeTo(rootEntry, entry))
                  {
                     InputStream in = jarFile.getInputStream(entry);
                     try
                     {
                        JarEntry newEntry = createEntry(rootEntry, entry);
                        jos.putNextEntry(newEntry);

                        copy(in, jos, entry.getSize());
                        log.trace("emitted " + newEntry);

                        jos.closeEntry();
                     }
                     finally
                     {
                        in.close();
                     }
                  }
               }
               jos.flush();
               jos.finish();
            }
            catch(IOException e)
            {
               // FIXME: How to handle this?
               log.warn(e.getMessage(), e);
            }
         }
      };
      writingThread.start();
   }

   @Override
   public void close() throws IOException
   {
      pipedOutputStream.close();
      pipedInputStream.close();
      // TODO: rejoin the thread?
      // writingThread.interrupt();
      // writingThread.join(5000);
      super.close();
   }
   
   protected void copy(InputStream in, OutputStream out, long size) throws IOException
   {
      byte buf[] = new byte[65536];
      
      while(size > 0)
      {
         int len = in.read(buf);
         if(len < 0)
            throw new EOFException("Unexpected EOF");
         out.write(buf, 0, len);
         size -= len;
      }
   }
   
   protected JarEntry createEntry(JarEntry rootEntry, JarEntry template)
   {
      if(rootEntry == null)
         return new JarEntry(template);
      
      String name = template.getName().substring(rootEntry.getName().length());
      JarEntry entry = new JarEntry(name);
      entry.setComment(template.getComment());
      entry.setSize(template.getSize());
      entry.setTime(template.getTime());
      return entry;
   }
   
   protected boolean isRelativeTo(JarEntry rootEntry, JarEntry current)
   {
      if(rootEntry == null)
         return true;
      
      if(current.getName().startsWith(rootEntry.getName()) && !current.getName().equals(rootEntry.getName()))
         return true;
      
      return false;
   }
   
   /* (non-Javadoc)
    * @see java.io.InputStream#read()
    */
   @Override
   public int read() throws IOException
   {
      return pipedInputStream.read();
   }

}
