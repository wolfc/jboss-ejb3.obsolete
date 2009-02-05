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
package org.jboss.ejb3.test.jsp.unit;

import java.net.URL;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/** 
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
public class EarServletUnitTestCase extends JBossTestCase
{
   private static String REALM = "JBossTest Servlets";
   private String baseURL = HttpUtils.getBaseURL(); 
   private String baseURLNoAuth = HttpUtils.getBaseURLNoAuth(); 
   
   public EarServletUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testEJBServlet() throws Exception
   {
      try {
         URL url = new URL(baseURL+"jsp-test/TestJSP");
         HttpUtils.accessURL(url);
      } catch (Exception e){
         e.printStackTrace();
         throw e;
      }
   }
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(EarServletUnitTestCase.class, "jsp-test.ear");
   }


}