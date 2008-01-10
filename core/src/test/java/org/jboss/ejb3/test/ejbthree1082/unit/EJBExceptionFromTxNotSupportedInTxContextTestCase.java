/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1082.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1082.BusinessRemoteA;
import org.jboss.test.JBossTestCase;

import com.sun.corba.se.impl.javax.rmi.PortableRemoteObject;

/**
 * Test Case ensuring that a method invocation marked as Transaction 
 * NOT_SUPPORTED in a Transactional Context throws an EJBException
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
public class EJBExceptionFromTxNotSupportedInTxContextTestCase extends JBossTestCase
{

   // Constructor

   public EJBExceptionFromTxNotSupportedInTxContextTestCase(String name)
   {
      super(name);
   }

   // Suite

   public static Test suite() throws Exception
   {
      return getDeploySetup(EJBExceptionFromTxNotSupportedInTxContextTestCase.class, "ejbthree1082.jar");
   }

   // Test Case

   public void testInvocationOnTransactionNotSupportedFromTxThrowsEJBException() throws Exception
   {
      // Obtain test bean
      BusinessRemoteA bean = (BusinessRemoteA) new PortableRemoteObject().narrow(this.getInitialContext().lookup(
            BusinessRemoteA.JNDI_NAME), BusinessRemoteA.class);
      
      // Ensure exception is of type expected
      JBossTestCase.assertTrue(bean.doesTransactionNotSupportedInvokedFromTxContextThrowEJBException());
   }

}
