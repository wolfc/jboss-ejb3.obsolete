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
package org.jboss.ejb3.test.securitydomain.unit;

import org.jboss.ejb3.test.securitydomain.SecurityDomainTest;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
public class SecurityDomainTestCase extends JBossTestCase
{
  
   public SecurityDomainTestCase(String name)
   {
      super(name);
   }

   public void testJBossSecurityDomain() throws Exception
   {
      SecurityDomainTest test = (SecurityDomainTest)getInitialContext().lookup("SecurityDomainTest");
      assertNotNull(test);
      
      try
      {
         test.testAccess();
         fail();
      }
      catch (Exception e)
      {
      }
      
      SecurityDomainTest augment = (SecurityDomainTest)getInitialContext().lookup("AugmentSecurityDomainTestBean/remote");
      assertNotNull(augment);
      
      try
      {
         augment.testAccess();
         fail();
      }
      catch (Exception e)
      {
      }
      
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      test.testAccess();
      augment.testAccess();

   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(SecurityDomainTestCase.class, "securitydomain.jar");
   }

}
