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
package org.jboss.ejb3.test.ejbthree1059.local;

import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.EJBLocalHome;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * AccessBean
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(AccessRemoteBusiness.class)
@RemoteBinding(jndiBinding = AccessRemoteBusiness.JNDI_NAME)
public class AccessBean implements AccessRemoteBusiness
{

   // Instance Members
   @EJB
   TestLocalHome localHome;

   // Required Implementations

   public void testValid()
   {
      // Obtain local instance
      TestLocal local = null;
      try
      {
         local = this.localHome.createValid();
      }
      catch (CreateException ce)
      {
         throw new RuntimeException(ce);
      }

      // Invoke
      local.test();

   }

   public void testInvalid()
   {
      // Attempt to create business interface from EJB21 create
      try
      {
         this.localHome.createInvalid();
      }
      catch (CreateException ce)
      {
         // Expected
         return;
      }

      // Invoke
      throw new RuntimeException("Business interface not have been created from " + EJBLocalHome.class.getName()
            + ".create<METHOD>()");

   }

}
