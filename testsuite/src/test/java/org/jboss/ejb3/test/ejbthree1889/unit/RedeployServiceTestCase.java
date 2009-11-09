/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1889.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.common.EJB3TestCase;
import org.jboss.ejb3.test.ejbthree1889.b.EJBTestBRemote;

/**
 * When a service is redeployed its consumers should be restarted and keep on functioning.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RedeployServiceTestCase extends EJB3TestCase
{
   public RedeployServiceTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(RedeployServiceTestCase.class, "ejbthree1889a.jar,ejbthree1889b.jar");
   }
   
   public void testRedeployA() throws Exception
   {
      // all is deployed, so all should be well
      EJBTestBRemote testB = lookup("EJBTestBService/remote", EJBTestBRemote.class);
      
      String result = testB.sayHello();
      
      assertEquals("Hello from A", result);
      
      redeploy("ejbthree1889a.jar");
      
      result = testB.sayHello();
      
      assertEquals("Hello from A", result);
   }
}
