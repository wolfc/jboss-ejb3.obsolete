/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.ejbthree1646.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.security.Principal;

import javax.ejb.EJBAccessException;
import javax.security.auth.Subject;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1646.SecuredBean;
import org.jboss.ejb3.core.test.ejbthree1646.SecuredLocal;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SecurityContextUtil;
import org.jboss.security.SimplePrincipal;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AuthenticationInterceptorTestCase extends AbstractEJB3TestCase
{
   @After
   public void after()
   {
      SecurityContextAssociation.setSecurityContext(null);
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      deploy("securitymanager-beans.xml");
      
      SessionContainer container = deploySessionEjb(SecuredBean.class);
      container.setJaccContextId("test");
   }
   
   private SecurityContext login(String name, Object credential) throws Exception
   {
      SecurityContext sc = SecurityContextFactory.createSecurityContext("test");
      SecurityContextUtil util = sc.getUtil();
      Principal principal = new SimplePrincipal(name);
      Subject subject = new Subject();
      subject.getPrincipals().add(principal);
      subject.getPrivateCredentials().add(credential);
      util.createSubjectInfo(principal, credential, subject);
      SecurityContextAssociation.setSecurityContext(sc);
      return sc;
   }
   
   @Test
   public void test1() throws Exception
   {
      SecuredLocal bean = lookup("SecuredBean/local", SecuredLocal.class);
      
      SecurityContext sc = SecurityContextFactory.createSecurityContext("test");
      SecurityContextAssociation.setSecurityContext(sc);
      
      assertEquals("nobody", bean.whoAmI());
      
      try
      {
         bean.onlyAdmin();
         fail("Should have thrown EJBAccessException");
      }
      catch(EJBAccessException e)
      {
         // good
      }
   }
   
   @Test
   public void test2() throws Exception
   {
      SecuredLocal bean = lookup("SecuredBean/local", SecuredLocal.class);
      
      login("Admin", null);
      
      String me = bean.whoAmI();
      assertEquals("Admin", me);
      
      bean.onlyAdmin();
   }
   
   @Test
   public void testEquals() throws Exception
   {
      SecuredLocal bean = lookup("SecuredBean/local", SecuredLocal.class);
      
      login("Admin", null);
      
      Principal p1 = bean.getCallerPrincipal();
      Principal p2 = bean.getCallerPrincipal();
      
      assertEquals(p1, p2);
   }
   
   @Test
   public void testSame() throws Exception
   {
      SecuredLocal bean = lookup("SecuredBean/local", SecuredLocal.class);
      
      login("Admin", null);
      
      Principal p1 = bean.getCallerPrincipal();
      Principal p2 = bean.getCallerPrincipal();
      
      assertSame(p1, p2);
   }
   
   @Test
   public void testSecurityContextAssociation() throws Exception
   {
      SecuredLocal bean = lookup("SecuredBean/local", SecuredLocal.class);
      
      SecurityContext sc = login("Invalid", null);
      
      try
      {
         bean.whoAmI();
         fail("Should have thrown EJBAccessException");
      }
      catch(EJBAccessException e)
      {
         // good
      }
      
      assertSame(sc, SecurityContextAssociation.getSecurityContext());
   }
}
