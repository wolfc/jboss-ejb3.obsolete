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

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.logging.Logger;

/**
 * Connect to a jarjar.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class JarJarURLConnection extends URLConnection
{
   private static final Logger log = Logger.getLogger(JarJarURLConnection.class);
   
   private URL delegate;
   private JarFile jarFile;
   private JarEntry rootEntry;
   
   protected JarJarURLConnection(URL url)
   {
      super(url);
   }

   @Override
   public void connect() throws IOException
   {
      if(connected) return;
      
      delegate = new URL(getURL().toString().substring(7));
      log.debug("delegate " + delegate);
      JarURLConnection conn = (JarURLConnection) delegate.openConnection();
      this.jarFile = conn.getJarFile();
      log.trace("jar file " + jarFile);
      this.rootEntry = conn.getJarEntry();
      log.trace("root entry " + rootEntry);
      
      connected = true;
   }

   @Override
   public InputStream getInputStream() throws IOException
   {
      if(!connected) connect();
      
      String spec = delegate.toString();
      URL jarFileURL;
      if(rootEntry != null)
         jarFileURL = new URL(spec.substring(0, spec.length() - rootEntry.getName().length()));
      else
         jarFileURL = delegate;
      log.debug("jar file url " + jarFileURL);
      
      return new JarJarInputStream(jarFile, rootEntry);
   }
}
