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
package org.jboss.ejb3.test.ejbthree1148;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * A Test Stateless EJB w/ an ejbCreate() method and
 * no 2.1 View
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(TestRemoteBusiness.class)
@RemoteBinding(jndiBinding = TestRemoteBusiness.JNDI_NAME)
public class TestBean implements TestRemoteBusiness
{
   // Class Members
   private static final Logger logger = Logger.getLogger(TestBean.class);

   // Instance Members

   private int returnValue = TestRemoteBusiness.RETURN_VALUE;

   // Required Implementations 
   public int test()
   {
      return returnValue;
   }

   // ejbCreate()
   public void ejbCreate()
   {
      // Set return value to unexpected result, proving this was used as a callback
      returnValue = TestRemoteBusiness.RETURN_VALUE - 1;
      // Log the error
      logger.error("ejbCreate SHOULD NOT BE USED AS CALLBACK WITH NO 2.1 VIEW");
   }
}
