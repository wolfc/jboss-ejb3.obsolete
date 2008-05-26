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
package org.jboss.ejb3.test.clusteredservice.unit;

import java.net.URL;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/** 
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
public class ServiceTestCase extends JBossTestCase
{
   private static String REALM = "JBossTest Servlets";
   private String node1URL = "http://jduke:theduke@localhost:8080/"; 
   private String node2URL = "http://jduke:theduke@localhost:8180/"; 
  
   private static final String USE_JBOSS = "UseJBossWebLoader";
   
   public ServiceTestCase(String name)
   {
      super(name);
   }
   
   public void testEJBServlet() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName tomcat = new ObjectName("jboss.web:service=WebServer");
      
      try {
         server.setAttribute(tomcat, new Attribute(USE_JBOSS, true));
         
         assertTrue((Boolean)server.getAttribute(tomcat, USE_JBOSS));
         
         try 
         {
            URL url = new URL(node1URL+"clusteredservice/EJBServlet");
            HttpUtils.accessURL(url);
            HttpUtils.accessURL(url);
         } catch (Exception e)
         {
         }
         
         try 
         {
            URL url = new URL(node2URL+"clusteredservice/EJBServlet");
            HttpUtils.accessURL(url);
            HttpUtils.accessURL(url);
         } catch (Exception e)
         {
         }
      }
      finally
      {
         server.setAttribute(tomcat, new Attribute(USE_JBOSS, false));
      }
   }
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceTestCase.class, "");
   }


}
