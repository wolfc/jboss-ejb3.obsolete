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
package org.jboss.ejb3.test.ejbthree1629.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1629.Ejb3DescriptorHandlerTestBean;
import org.jboss.ejb3.test.ejbthree1629.Ejb3DescriptorHandlerTestRemote;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Ejb3DescriptorHandlerPrimitiveHandlingTestCase
 *
 * Test case for EJBTHREE-1629 issue.
 * Incorrect handling of primitives. The issue arises when there's a bean method
 * accepting a primitive type (double or float) and also has an annotation on the
 * method (ex: @TransactionAttribute). Furthermore, the bean needs to have a
 * corresponding entry (minimally ejb-name and ejb-class) in ejb-jar.xml, for this
 * issue to manifest.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class Ejb3DescriptorHandlerPrimitiveHandlingTestCase extends JBossTestCase
{

   /**
    * Logger
    */
   private static final Logger logger = Logger.getLogger(Ejb3DescriptorHandlerPrimitiveHandlingTestCase.class);

   /**
    * Constructor
    *
    * @param name
    */
   public Ejb3DescriptorHandlerPrimitiveHandlingTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(Ejb3DescriptorHandlerPrimitiveHandlingTestCase.class, "ejbthree1629.jar");
   }

   /**
    * Test that the bean has been deployed and available for
    * method invocations.
    *
    * @throws Throwable
    */
   public void testBeanInvocation() throws Throwable
   {
      Context ctx = new InitialContext();
      String jndiName = Ejb3DescriptorHandlerTestBean.JNDI_NAME;
      Object bean = ctx.lookup(jndiName);
      logger.info("Successfully looked up the bean at " + jndiName);

      assertNotNull("Object returned from JNDI lookup for " + jndiName + " is null", bean);
      assertTrue("Object returned from JNDI lookup for " + jndiName + " is not an instance of "
            + Ejb3DescriptorHandlerTestRemote.class, (bean instanceof Ejb3DescriptorHandlerTestRemote));

      // Call the method on the bean
      Ejb3DescriptorHandlerTestRemote primitiveTesterBean = (Ejb3DescriptorHandlerTestRemote) bean;
      double someDouble = 2.0;
      double returnedDouble = primitiveTesterBean.doOpAndReturnDouble(someDouble);
      assertEquals("Bean returned unexpected value for primitive double",returnedDouble, someDouble);

      // test on float
      float someFloat = 2.5F;
      float returnedFloat = primitiveTesterBean.doOpAndReturnFloat(someFloat);
      assertEquals("Bean returned unexpected value for primitive float",returnedFloat, someFloat);

      // test on arrays
      float[] floatArray = new float[]{someFloat};
      float[] returnedFloatArray = primitiveTesterBean.doOpAndReturnFloat(floatArray);
      assertEquals("Bean returned unexpected value for primitive float array",returnedFloatArray.length, floatArray.length);
      assertEquals("Bean returned unexpected value for primitive float array contents",returnedFloatArray[0], floatArray[0]);

      double[] doubleArray = new double[]{someDouble};
      double[] returnedDoubleArray = primitiveTesterBean.doOpAndReturnDouble(doubleArray);
      assertEquals("Bean returned unexpected value for primitive double array",returnedDoubleArray.length, doubleArray.length);
      assertEquals("Bean returned unexpected value for primitive double array contents",returnedDoubleArray[0], doubleArray[0]);

      // now some simple method which says hi
      String name = "jai";
      String returnedMessage = primitiveTesterBean.sayHi(name);
      assertEquals("Bean returned unexpected message", returnedMessage, "Hi " + name);

   }

}
