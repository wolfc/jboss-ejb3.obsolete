/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1858.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1858.SimpleStateless;
import org.jboss.ejb3.test.ejbthree1858.StatelessBean;
import org.jboss.test.JBossTestCase;

/**
 * ResourceEnvRefTestCase
 *
 * EJBTHREE-1858 exposes a bug where a NPE is thrown when a EJBContext is being
 * configured through ejb-jar.xml as a resource-env-ref.
 *
 * This testcase tests the fix for that issue, by configuring EJBContext through
 * a resource-env-ref and then looking up that through the bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ResourceEnvRefTestCase extends JBossTestCase
{

   /**
    * Constructor
    *
    * @param name
    */
   public ResourceEnvRefTestCase(String name)
   {
      super(name);
   }

   /**
    * Test that the resources configured through resource-env-ref are bound
    * correctly
    *
    * @throws Exception
    */
   public void testResourceEnvRefWithoutInjectionTarget() throws Exception
   {
      SimpleStateless bean = (SimpleStateless) getInitialContext().lookup(StatelessBean.JNDI_NAME);
      // check EJBContext through resource-env-ref was handled
      assertTrue("resource-env-ref did not handle EJBContext", bean.isEJBContextAvailableThroughResourceEnvRef());
      // check UserTransaction through resource-env-ref was handled
      assertTrue("resource-env-ref did not handle UserTransaction", bean
            .isUserTransactionAvailableThroughResourceEnvRef());
      // check some other resource through resource-env-ref was handled
      assertTrue("resource-env-ref did not setup the other resource in java:comp/env of the bean", bean
            .isOtherResourceAvailableThroughResourceEnvRef());
   }

   /**
    * Deploy the test artifact(s)
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ResourceEnvRefTestCase.class, "ejbthree1858.ear");
   }
}
