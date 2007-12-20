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
package org.jboss.ejb3.test.ejbthree1154;

import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * A Test Delegate Bean to access the local methods of the TestBean
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(DelegateRemoteBusiness.class)
@RemoteBinding(jndiBinding = DelegateRemoteBusiness.JNDI_NAME)
public class DelegateBean implements DelegateRemoteBusiness
{
   // Instance Members
   @EJB
   private TestLocalHome home;

   @EJB
   private TestLocalBusiness localBusiness;

   // Required Implementations

   public boolean testBeanReturnsCorrectLocal21ViewFromHomeCreate()
   {

      // Initialize
      TestLocal test = null;

      // Obtain 2.1 view
      try
      {
         test = home.create();
      }
      catch (CreateException e)
      {
         throw new RuntimeException(e);
      }

      // Invoke and ensure returns as expected
      if (test.test() != TestLocal.RETURN_VALUE)
      {
         throw new RuntimeException("Invocation on " + TestLocal.class.getName() + " resulted in unexpected result");
      }

      // Everything's OK
      return true;

   }

   public boolean testBeanReturnsCorrectlyFromLocalBusinessInterface()
   {
      // Invoke and ensure returns as expected
      if (localBusiness.test() != TestLocalBusiness.RETURN_VALUE)
      {
         throw new RuntimeException("Invocation on " + TestLocalBusiness.class.getName()
               + " resulted in unexpected result");
      }

      // All OK
      return true;
   }
}
