/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.client;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.client.AppClientLauncher;
import org.jboss.ejb3.metamodel.ApplicationClientDD;
import org.jboss.ejb3.metamodel.ApplicationClientDDObjectFactory;
import org.jboss.ejb3.metamodel.JBossClientDDObjectFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.util.NotImplementedException;
import org.jboss.xb.binding.JBossXBException;

/**
 * This class launches a JavaEE 5 application client.
 * 
 * The first argument is either a jar file containing the client deployment files or the application client class name.
 * The manifest file Main-Class attribute must point to the application client class.
 * It must also contain an application client deployment descriptor file (META-INF/application-client.xml).
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ClientLauncher
   implements AppClientLauncher
{
   private static final Logger log = Logger.getLogger(ClientLauncher.class);
   
   /**
    * Convenience method for launching a client container.
    * 
    * @param xml
    * @param mainClassName
    * @param applicationClientName
    * @param args
    * @throws Exception
    */
   public static void launch(JBossClientMetaData xml, String mainClassName, String applicationClientName, String args[]) throws Exception
   {
      Class<?> mainClass = Class.forName(mainClassName);
      
      ClientContainer container = new ClientContainer(xml, mainClass, applicationClientName);
      
      // TODO: postContruct
      
      container.invokeMain(args);
      
      // TODO: preDestroy
   }

   /**
    * Convenience method to load the XML descriptor.
    * 
    * @return
    * @throws IOException 
    * @throws JBossXBException 
    */
   public static JBossClientMetaData loadXML(String applicationClientName) throws NamingException
   {
      /*
      URL url = findResource("META-INF/application-client.xml");
      log.trace("application-client.xml found at " + url);
      URL jbossClientURL = findResource("META-INF/jboss-client.xml");
      log.trace("jboss-client.xml found at " + jbossClientURL);
      return loadXML(url, jbossClientURL);
      */
      log.warn("FIXME: using an unsupported hack to get metadata");
      InitialContext ctx = new InitialContext();
      JBossClientMetaData metaData = (JBossClientMetaData) ctx.lookup(applicationClientName + "/metaData");
      return metaData;
   }
   
   /**
    * Work in progress.
    * 
    * @param args   the arguments for the launcher
    */
   public static void main(String[] args)
   {
      try
      {
         if(args.length < 1)
            throw new IllegalArgumentException("expected a jar filename as argument");
         
         Class<?> mainClass;
         
         String name = args[0];
         if(name.endsWith(".jar"))
         {
            throw new NotImplementedException();
//            JarFile jarFile = new JarFile(jarName);
         }
         else
         {
            String mainClassName = name;
            mainClass = Class.forName(mainClassName);
         }
         
         URL appXmlURL = mainClass.getClassLoader().getResource("META-INF/application-client.xml");
         if(appXmlURL == null)
            throw new RuntimeException("Can't find META-INF/application-client.xml");
         
         // FIXME: client metadata
         JBossClientMetaData xml = null;
         //JBossClientMetaData xml = ApplicationClientDDObjectFactory.parse(appXmlURL);
         if(true)
            throw new RuntimeException("NYI");
         
         // FIXME: j2ee.clientName
         
         List<String> newArgs = new ArrayList<String>();
         for(int i = 1; i < args.length; i++)
         {
            newArgs.add(args[i]);
         }
         args = newArgs.toArray(args);
         
         // FIXME: when jar gets implemented this won't work anymore
         String mainClassName = name;
         launch(xml, mainClassName, "FIXME", args);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.exit(1);
      }
   }

   /**
    * The AppClientLauncher method for launching a client container.
    * 
    * @param mainClassName - the class whose main(String[]) will be invoked
    * @param clientName - the client name that maps to the server side JNDI ENC
    * @param args - the args to pass to main method
    * @throws Throwable
    */
   public void launch(String mainClassName, String clientName, String args[])
      throws Throwable
   {
      JBossClientMetaData xml = loadXML(clientName);
      launch(xml, mainClassName, clientName, args);
   }

}
