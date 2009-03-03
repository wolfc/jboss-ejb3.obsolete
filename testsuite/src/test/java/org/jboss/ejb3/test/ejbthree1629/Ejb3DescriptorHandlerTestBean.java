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
package org.jboss.ejb3.test.ejbthree1629;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Ejb3DescriptorHandlerTestBean
 *
 * Will be used for testing EJBTHREE-1629 issue. The issue happens
 * only when the methods have a annotaiton present like @TransactionAttribute
 * and accept primitive type parameters (especially double or float). To reproduce
 * the issue, there needs to be a entry for this bean in the ejb-jar.xml too, minimally
 * just the ejb-name and ejb-class.
 *
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote(Ejb3DescriptorHandlerTestRemote.class)
@RemoteBinding(jndiBinding = Ejb3DescriptorHandlerTestBean.JNDI_NAME)
public class Ejb3DescriptorHandlerTestBean implements Ejb3DescriptorHandlerTestRemote
{

   /**
    * Not appropriate to expose the JNDI_NAME through this class, since
    * the client will have to be aware of the bean implementation class.
    * But since this is just being used by a testcase, we are OK.
    */
   public static final String JNDI_NAME = "PrimitiveTesterWithTransactionBean@SomeJNDIName";

   @TransactionAttribute (TransactionAttributeType.REQUIRED)
   public double doOpAndReturnDouble(double someDouble)
   {
      return someDouble;
   }

   @TransactionAttribute (TransactionAttributeType.REQUIRED)
   public float doOpAndReturnFloat(float someFloat)
   {
      return someFloat;
   }

   @TransactionAttribute (TransactionAttributeType.REQUIRED)
   public double[] doOpAndReturnDouble(double[] someDouble)
   {
      return someDouble;
   }

   @TransactionAttribute (TransactionAttributeType.REQUIRED)
   public float[] doOpAndReturnFloat(float[] someFloat)
   {
      return someFloat;
   }

   public String sayHi(String name)
   {
      return "Hi " + name;
   }

}
