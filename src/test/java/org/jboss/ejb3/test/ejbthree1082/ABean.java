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
package org.jboss.ejb3.test.ejbthree1082;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * Delegate bean used to test a method in Bean B that 
 * does not support Tx, invoked from a 
 * transactional context
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(BusinessRemoteA.class)
@RemoteBinding(jndiBinding=BusinessRemoteA.JNDI_NAME)
public class ABean implements BusinessRemoteA
{
   // Class Members
   private static final Logger log = Logger.getLogger(ABean.class);

   // Instance Members
   @EJB
   private BusinessLocalB b;

   // Required Implementations
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public boolean doesTransactionNotSupportedInvokedFromTxContextThrowEJBException()
   {
      try{
         // Invoke upon Tx not supported from transactional context
         b.test();
      }
      // Expected
      catch(EJBException e){
         return true;
      }
      catch(Exception e)
      {
         log.error(e);
      }
      
      // Proper exception not encountered
      return false;
   }

}
