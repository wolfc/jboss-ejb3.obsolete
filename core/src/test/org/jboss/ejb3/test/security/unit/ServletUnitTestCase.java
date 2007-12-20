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
package org.jboss.ejb3.test.security.unit;

import java.io.IOException;
import java.net.URL;

import junit.framework.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.jboss.test.JBossTestCase;
import org.jboss.ejb3.test.dd.web.util.HttpUtils;

/** Tests of servlet container integration into the JBoss server. This test
 requires than a web container be integrated into the JBoss server. The tests
 currently do NOT use the java.net.HttpURLConnection and associated http client
 and  these do not return valid HTTP error codes so if a failure occurs it
 is best to connect the webserver using a browser to look for additional error
 info.

 The secure access tests require a user named 'jduke' with a password of 'theduke'
 with a role of 'AuthorizedUser' in the servlet container.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 61136 $
 */
public class ServletUnitTestCase extends JBossTestCase
{
   private static String REALM = "JBossTest Servlets";
   
   public ServletUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testEJBServlet() throws Exception
   {
      try {
         URL url = new URL(HttpUtils.getBaseURL()+"security-web/EJBServlet");
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
      return getDeploySetup(ServletUnitTestCase.class, "security.jar, security-web.war");
   }


}
