/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors. 
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
package org.jboss.ejb3.test.interceptors.common;

import java.net.URL;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class AOPDeployer
{
   private static Logger log = Logger.getLogger(AOPDeployer.class);
   
   private String path;
   
   URL url;
   
   public AOPDeployer(String path)
   {
      this.path = path;
   }
   
   public String deploy() throws Exception
   {
      url = Thread.currentThread().getContextClassLoader().getResource(path);
      log.info("Deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);
      return "Deployed " + url;
   }
   
   public String undeploy()
   {
      try
      {
         log.info("Undeploying AOP from " + url);
         AspectXmlLoader.undeployXML(url);
      }
      catch(Exception e)
      {
         log.warn("Error undeploying " + url, e); 
      }
      return "Undeployed " + url;
   }
}
