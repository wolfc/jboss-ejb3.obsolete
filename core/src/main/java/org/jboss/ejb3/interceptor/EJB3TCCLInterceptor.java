/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ejb3.interceptor;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.EJBContainerInvocation;

/**
 * EJB3TCCLInterceptor
 *
 * {@link Interceptor} responsible for setting the correct Thread context classloader (TCCL)
 * during the EJB invocation. The {@link EJBContainer}'s classloader is set as the
 * TCCL for the duration of this invocation. The TCCL is then reset to the original 
 * classloader.
 * 
 * Note: The TCCL switch happens from the point when this interceptor is invoked. So 
 * ideally, this interceptor should be the first in the chain of the AOP interceptors
 * during the EJB invocation 
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
// TODO : This interceptor can be moved out of "core", but that isn't too
// straightforward right now because the container's classloader can 
// only be obtained from EJBContainer and would effectively mean a dependency on
// ejb3-core. There's a way to avoid this by introducing a new interface which exposes
// the container's classloader and this interface is then implemented by the EJBContainer.
// The other component can then rely on this new interface to get hold of the classloader.
// But overall, that approach isn't straightforward at this point and probably not worth the
// efforts.
public class EJB3TCCLInterceptor implements Interceptor
{

   /**
    * Returns the name of the interceptor
    * @see Interceptor#getName()
    */
   public String getName()
   {
      return this.getClass().getName();
   }

   /**
    * Sets the TCCL to the classloader of the container so
    * that the invocation happens in the context of the 
    * container's classloader. Finally upon return resets
    * the TCCL to the previous classloader. 
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      assert invocation instanceof EJBContainerInvocation : "Unexpected invocation type " + invocation.getClass()
            + " - expected " + EJBContainerInvocation.class;

      // get hold of the EJBContainer from the invocation
      EJBContainer ejbContainer = EJBContainer.getEJBContainer(invocation.getAdvisor());
      
      ClassLoader ejbContainerClassloader = ejbContainer.getClassloader();
      ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
      // TODO: Review for security manager privileged blocks
      try
      {
         // Set the TCCL to the EJBContainer's classloader
         Thread.currentThread().setContextClassLoader(ejbContainerClassloader);
         // move on
         return invocation.invokeNext();
      }
      finally
      {
         // reset to original TCCL 
         Thread.currentThread().setContextClassLoader(previousClassLoader);
      }
   }

}
