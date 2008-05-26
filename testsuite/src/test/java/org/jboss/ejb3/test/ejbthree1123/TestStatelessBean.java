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
package org.jboss.ejb3.test.ejbthree1123;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * A Stateless EJB used to determine whether a message has been 
 * received by the MDB
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */

@Stateless
@Remote(TestStatelessRemoteBusiness.class)
@RemoteBinding(jndiBinding=TestStatelessRemoteBusiness.JNDI_NAME)
public class TestStatelessBean implements TestStatelessRemoteBusiness
{
   // Class Members
   public static boolean IS_MESSAGE_RECEIVED = false;

   // Required Implementations
   
   public boolean isMessageReceived()
   {
      return TestStatelessBean.IS_MESSAGE_RECEIVED;
   }
   
   public void clearStatus()
   {
      TestStatelessBean.IS_MESSAGE_RECEIVED = false;
   }

}
