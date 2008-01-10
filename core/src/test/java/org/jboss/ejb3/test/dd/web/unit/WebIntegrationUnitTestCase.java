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
package org.jboss.ejb3.test.dd.web.unit;

import java.net.URL;

import junit.framework.Test;

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
 @version $Revision$
 */
public class WebIntegrationUnitTestCase extends JBossTestCase
{
   private static String REALM = "JBossTest Servlets";
   private String baseURL = HttpUtils.getBaseURL(); 
   private String baseURLNoAuth = HttpUtils.getBaseURLNoAuth(); 
   
   public WebIntegrationUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testEJBServlet() throws Exception
   {
      try {
         URL url = new URL(baseURL+"dd/EJBServlet");
         HttpUtils.accessURL(url);
      } catch (Exception e){
         e.printStackTrace();
         throw e;
      }
   }


   /*
   public void testInjectionJsp() throws Exception
   {
      try {
         URL url = new URL(baseURL+"dd/InjectionTest.jsp");
         HttpUtils.accessURL(url);
      } catch (Exception e){
         e.printStackTrace();
         throw e;
      }
   }
   */

/*
   public void testRealPath() throws Exception
   {
      URL url = new URL(baseURL+"dd/APIServlet?op=testGetRealPath");
      HttpUtils.accessURL(url);
   }

   public void testHttpSessionListener() throws Exception
   {
      URL url = new URL(baseURL+"dd/APIServlet?op=testSessionListener");
      HttpUtils.accessURL(url);
   }

   public void testEJBOnStartupServlet() throws Exception
   {
      URL url = new URL(baseURL+"dd/EJBOnStartupServlet");
      HttpUtils.accessURL(url);
   }
   
   public void testStatefulSessionServlet() throws Exception
   {
      URL url = new URL(baseURL+"dd/StatefulSessionServlet");
      HttpUtils.accessURL(url);
      // Need a mechanism to force passivation...
      HttpUtils.accessURL(url);
   }
   
   public void testSnoopJSP() throws Exception
   {
      URL url = new URL(baseURL+"dd/snoop.jsp");
      HttpUtils.accessURL(url);
   }
  
   public void testSpeedServlet() throws Exception
   {
      URL url = new URL(baseURL+"dd/SpeedServlet");
      HttpUtils.accessURL(url);
   }

   public void testSnoopJSPByPattern() throws Exception
   {
      URL url = new URL(baseURL+"dd/test-snoop.snp");
      HttpUtils.accessURL(url);
   }
  
   public void testSnoopJSPByMapping() throws Exception
   {
      URL url = new URL(baseURL+"dd/test-jsp-mapping");
      HttpUtils.accessURL(url);
   }
  
   public void testJSPClasspath() throws Exception
   {
      URL url = new URL(baseURL+"dd/classpath.jsp");
      HttpUtils.accessURL(url);
   }

   public void testClientLoginServlet() throws Exception
   {
      URL url = new URL(baseURL+"dd/ClientLoginServlet");
      HttpUtils.accessURL(url);
   }
  
   public void testUserInRoleServlet() throws Exception
   {
      URL url = new URL(baseURL+"dd/restricted/UserInRoleServlet");
      HttpMethodBase request = HttpUtils.accessURL(url);
      Header errors = request.getResponseHeader("X-ExpectedUserRoles-Errors");
      log.info("X-ExpectedUserRoles-Errors: "+errors);
      assertTrue("X-ExpectedUserRoles-Errors("+errors+") is null", errors == null);
      errors = request.getResponseHeader("X-UnexpectedUserRoles-Errors");
      log.info("X-UnexpectedUserRoles-Errors: "+errors);
      assertTrue("X-UnexpectedUserRoles-Errors("+errors+") is null", errors == null);
   }
  
   public void testSecureServlet() throws Exception
   {
      URL url = new URL(baseURL+"dd/restricted/SecureServlet");
      HttpUtils.accessURL(url);
   }
 
   public void testSecureServlet2() throws Exception
   {
      URL url = new URL(baseURL+"dd/restricted2/SecureServlet");
      HttpUtils.accessURL(url);
   }
 
   public void testSubjectServlet() throws Exception
   {
      URL url = new URL(baseURL+"dd/restricted/SubjectServlet");
      HttpMethodBase request = HttpUtils.accessURL(url);
      Header hdr = request.getResponseHeader("X-SubjectServlet");
      log.info("X-SubjectServlet: "+hdr);
      assertTrue("X-SubjectServlet("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-SubjectFilter-ENC");
      log.info("X-SubjectFilter-ENC: "+hdr);
      assertTrue("X-SubjectFilter-ENC("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-SubjectFilter-SubjectSecurityManager");
      log.info("X-SubjectFilter-SubjectSecurityManager: "+hdr);
      assertTrue("X-SubjectFilter-SubjectSecurityManager("+hdr+") is NOT null", hdr != null);
   }
  
   public void testSecureServletAndUnsecureAccess() throws Exception
   {
      getLog().info("+++ testSecureServletAndUnsecureAccess");
      URL url = new URL(baseURL+"dd/restricted/SecureServlet");
      getLog().info("Accessing SecureServlet with valid login");
      HttpUtils.accessURL(url);
      String baseURL2 = "http://localhost:" + Integer.getInteger("web.port", 8080) + '/';
      URL url2 = new URL(baseURL2+"dd/restricted/UnsecureEJBServlet");
      getLog().info("Accessing SecureServlet with no login");
      HttpUtils.accessURL(url2, REALM, HttpURLConnection.HTTP_UNAUTHORIZED);
   }
  
   public void testSecureServletWithBadPass() throws Exception
   {
      String baseURL = "http://jduke:badpass@localhost:" + Integer.getInteger("web.port", 8080) + '/';
      URL url = new URL(baseURL+"dd/restricted/SecureServlet");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_UNAUTHORIZED);
   }
 
   public void testSecureServletWithNoLogin() throws Exception
   {
      String baseURL = "http://localhost:" + Integer.getInteger("web.port", 8080) + '/';
      URL url = new URL(baseURL+"dd/restricted/SecureServlet");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_UNAUTHORIZED);
   }
  
   public void testNotJbosstest() throws Exception
   {
      String baseURL = "http://localhost:" + Integer.getInteger("web.port", 8080) + '/';
      URL url = new URL(baseURL+"jbosstest-not/unrestricted/SecureServlet");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
   }
  
   public void testSecureEJBAccess() throws Exception
   {
      URL url = new URL(baseURL+"dd/restricted/SecureEJBAccess");
      HttpUtils.accessURL(url);
   }
  
   public void testIncludeEJB() throws Exception
   {
      URL url = new URL(baseURL+"dd/restricted/include_ejb.jsp");
      HttpUtils.accessURL(url);
   }
 
   public void testUnsecureEJBAccess() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"dd/UnsecureEJBAccess?method=echo");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_INTERNAL_ERROR);
   }
 
   public void testUnsecureAnonEJBAccess() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"dd/UnsecureEJBAccess?method=unchecked");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
   }

   public void testUnsecureRunAsServlet() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"dd/UnsecureRunAsServlet?method=checkRunAs");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);      
   }

   public void testUnsecureRunAsServletWithPrincipalName() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"dd/UnsecureRunAsServletWithPrincipalName?ejbName=ejb/UnsecureRunAsServletWithPrincipalNameTarget");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);      
   }

   public void testUnsecureRunAsServletWithPrincipalNameAndRoles() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"dd/UnsecureRunAsServletWithPrincipalNameAndRoles?ejbName=ejb/UnsecureRunAsServletWithPrincipalNameAndRolesTarget");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);      
   }
   */
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(WebIntegrationUnitTestCase.class, "dd-web.ear");
   }


}
