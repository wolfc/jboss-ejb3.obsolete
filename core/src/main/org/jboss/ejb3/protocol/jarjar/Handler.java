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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * The jarjar URL stream handler allows for directories within
 * a jarjar to be served as a jar file.
 * 
 * So the URL spec becomes: jarjar:jar:file:myjar.jar!/somedirectory/
 * 
 * It's meant to be used by the PersistenceUnitDeploment to comply
 * with javax.persistence.spi.PersistentenceUnitInfo.getPersistenceUnitRootUrl()
 *
 * @see javax.persistence.spi.PersistenceUnitInfo#getPersistenceUnitRootUrl()
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 67653 $
 */
public class Handler extends URLStreamHandler
{
   static
   {
      init();
   }

   public static void init()
   {
      String pkg = "org.jboss.ejb3.protocol";
      String pkgs = System.getProperty("java.protocol.handler.pkgs");
      if (pkgs == null || pkgs.trim().length() == 0)
         System.setProperty("java.protocol.handler.pkgs", pkg);
      else if (!pkgs.contains(pkg))
      {
         pkgs += "|" + pkg;
         System.setProperty("java.protocol.handler.pkgs", pkgs);
      }
   }
   
   /* (non-Javadoc)
    * @see java.net.URLStreamHandler#openConnection(java.net.URL)
    */
   @Override
   protected URLConnection openConnection(URL u) throws IOException
   {
      return new JarJarURLConnection(u);
   }

}
