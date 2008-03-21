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
package org.jboss.ejb3.test.ejbthree1222;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * TestStatefulWithRemoveMethodBean
 * 
 * A SFSB with remote view that defines a "void remove()" method
 * that is not annotated with @Remove, and should therefore act like
 * any plain method.
 *  
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Remote(TestStatefulWithRemoveMethodRemote.class)
@RemoteBinding(jndiBinding = TestStatefulWithRemoveMethodRemote.JNDI_NAME)
public class TestStatefulWithRemoveMethodBean implements TestStatefulWithRemoveMethodRemote
{
   // Class Members

   public static int CALLS = 0;

   // Required Implementations

   /**
    * Increments the number of calls
    */
   public void remove()
   {
      TestStatefulWithRemoveMethodBean.CALLS++;
   }

   public void reset()
   {
      TestStatefulWithRemoveMethodBean.CALLS = 0;
   }

   public int getCalls()
   {
      return TestStatefulWithRemoveMethodBean.CALLS;
   }
}
