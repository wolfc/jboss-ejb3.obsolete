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
package org.jboss.ejb3.test.naming.unit;

import javax.naming.InitialContext;
import org.jboss.test.JBossTestCase;
import org.jboss.ejb3.test.naming.bad.BadInjector;

/**
 * Tests of the secure access to EJBs.
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision$
 */
public class BadInjectionsUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the ENCUnitTestCase object
    *
    * @param name  Testcase name
    */
   public BadInjectionsUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test deployment of an ejb3 bean that tries to inject the same
    * resource to a field and a method.
    * 
    * @throws Exception
    */
   public void testFieldMethodCollision() throws Exception
   {
      log.info("+++ testFieldMethodCollision");
      super.redeploy("bad-field-method.jar");
      log.error("Should not have been able to deploy bad-field-method.jar");
      InitialContext ctx = getInitialContext();
      Object ref = ctx.lookup("BadFieldMethodBean/remote");
      log.info("Found BadInjector");
      BadInjector bean = (BadInjector) ref;
      assertEquals("BadFieldMethodBean", bean.getKey());
      bean.remove();
      super.undeploy("bad-field-method.jar");
   }
}
