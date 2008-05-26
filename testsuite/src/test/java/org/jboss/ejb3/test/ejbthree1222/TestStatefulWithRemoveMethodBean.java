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

import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteHomeBinding;

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
@Local(
{TestStatefulWithRemoveMethodLocal.class, TestStatefulWithRemoveMethodLocalBusiness.class})
@Remote(
{TestStatefulWithRemoveMethodRemoteBusiness.class, TestStatefulWithRemoveMethodRemote.class})
@LocalHome(TestStatefulWithRemoveMethodLocalHome.class)
@RemoteHome(TestStatefulWithRemoveMethodRemoteHome.class)
@LocalBinding(jndiBinding = TestStatefulWithRemoveMethodLocalBusiness.JNDI_NAME)
@RemoteBinding(jndiBinding = TestStatefulWithRemoveMethodRemoteBusiness.JNDI_NAME)
@LocalHomeBinding(jndiBinding = TestStatefulWithRemoveMethodLocalHome.JNDI_NAME)
@RemoteHomeBinding(jndiBinding = TestStatefulWithRemoveMethodRemoteHome.JNDI_NAME)
public class TestStatefulWithRemoveMethodBean implements TestStatefulWithRemoveMethodRemoteBusiness
{
   // Instance Members

   private int calls;

   // Required Implementations

   /**
    * Increments the number of calls
    */
   public void remove()
   {
      this.setCalls(this.getCalls() + 1);
   }

   public void reset()
   {
      this.setCalls(0);
   }

   public int getCalls()
   {
      return calls;
   }

   private void setCalls(int calls)
   {
      this.calls = calls;
   }
}
