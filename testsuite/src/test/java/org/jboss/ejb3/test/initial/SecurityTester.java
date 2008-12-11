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
package org.jboss.ejb3.test.initial;

import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 *
 **/
public class SecurityTester implements SecurityTesterMBean
{
   static Logger log = Logger.getLogger(SecurityTester.class);

   public void testSecurityAssociation() throws Exception
   {
      InitialContext ctx = new InitialContext();
      SecuredTest test = (SecuredTest) ctx.lookup("initial-ejb3-test/SecuredTestBean/local");

      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();

      test.unchecked();
      test.testDefault();
      test.secured();

      client.logout();
      client.setSimple("authfail", "password");
      client.login();

      boolean securityFailure = true;
      try
      {
         test.secured();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }

      if (securityFailure) throw new RuntimeException("auth failure was not caught for method");

      securityFailure = true;
      client.logout();
      client.setSimple("rolefail", null);
      client.login();
      try
      {
         test.secured();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }
      if (securityFailure) throw new RuntimeException("role failure was not caught for method");

      client.logout();
      client.setSimple("somebody", null);
      client.login();
      log.info("test exclusion");
      securityFailure = true;
      try
      {
         test.excluded();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }
      
      if (securityFailure) throw new RuntimeException("excluded failure was not caught for method");
      client.logout();
   }
   
   public void testSecurityClient() throws Exception
   {
      
      InitialContext ctx = new InitialContext();
      SecuredTest test = (SecuredTest) ctx.lookup("initial-ejb3-test/SecuredTestBean/local");

      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple("somebody","password");
      sc.login();
      
//      AppCallbackHandler acbh = new AppCallbackHandler("somebody","password".toCharArray()); 
//      sc.setJAAS("other", acbh);
      
      sc.login();
      test.unchecked();
      test.testDefault();
      test.secured();
      
      sc.logout();

      sc.setSimple("authfail", "password");
      sc.login();
      boolean securityFailure = true;
      try
      {
         test.secured();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }

      if (securityFailure) throw new RuntimeException("auth failure was not caught for method");
      sc.logout();
      
      securityFailure = true;
      sc.setSimple("rolefail", "password");
      sc.login();
      try
      {
         test.secured();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }
      if (securityFailure) throw new RuntimeException("role failure was not caught for method");

      sc.logout();
      
      sc.setSimple("somebody", "password");
      sc.login();
      
      log.info("test exclusion");
      securityFailure = true;
      try
      {
         test.excluded();
      }
      catch (EJBAccessException ignored)
      {
         log.info(ignored.getMessage());
         securityFailure = false;
      }
      if (securityFailure) throw new RuntimeException("excluded failure was not caught for method");
      sc.logout();
   }

}



