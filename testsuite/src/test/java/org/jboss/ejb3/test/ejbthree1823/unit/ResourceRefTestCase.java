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
package org.jboss.ejb3.test.ejbthree1823.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1823.ResourceRefBean;
import org.jboss.ejb3.test.ejbthree1823.ResourceRefRemote;
import org.jboss.test.JBossTestCase;

/**
 * ResourceRefTestCase
 *
 * Test case for EJBTHREE-1823.
 * Tests that an resource-ref entry without a res-type
 * does not fail the deployments with NPE.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ResourceRefTestCase extends JBossTestCase
{

   /**
    * Constructor
    * @param name
    */
   public ResourceRefTestCase(String name)
   {
      super(name);
   }

   /**
    * Deploy the artifact
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ResourceRefTestCase.class, "ejbthree1823.jar");
   }

   /**
    * Test that a resource-ref entry with a res-type does not throw an
    * NPE. Furthermore, the test additional provides a mappedName for the resource-ref
    * in which case the resource ref will be created in the ENC.
    *
    * @throws Exception
    */
   public void testResourceRefEntriesWithoutResType() throws Exception
   {
      // lookup the bean
      ResourceRefRemote bean = (ResourceRefRemote) getInitialContext().lookup(ResourceRefBean.JNDI_NAME);
      assertNotNull("Bean returned from JNDI is null", bean);

      // test datasource resource-ref which does not have a res-type specified
      boolean result = bean.isDataSourceAvailableInEnc();
      assertTrue("Datasource not bound in ENC of the bean", result);
   }

   /**
    * Test that resource-ref with proper res-type are correctly processed
    * (i.e. no regression is caused by the EJBHTREE-1823 fix)
    *
    * @throws Exception
    */
   public void testResourceRefEntriesWithResType() throws Exception
   {
      // lookup the bean
      ResourceRefRemote bean = (ResourceRefRemote) getInitialContext().lookup(ResourceRefBean.JNDI_NAME);
      assertNotNull("Bean returned from JNDI is null", bean);

      // test other resource-refs which have res-type specified
      boolean result = bean.areOtherResourcesAvailableInEnc();
      assertTrue("Not all resources bound in ENC of the bean", result);

   }

}
