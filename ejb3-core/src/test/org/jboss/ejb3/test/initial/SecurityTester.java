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
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

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

   public void test() throws Exception
   {
      InitialContext ctx = new InitialContext();
      SecuredTest test = (SecuredTest) ctx.lookup("SecuredTestBean/local");

      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());

      test.unchecked();
      test.testDefault();
      test.secured();

      SecurityAssociation.setPrincipal(new SimplePrincipal("authfail"));

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
      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
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

      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
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

   }

}



