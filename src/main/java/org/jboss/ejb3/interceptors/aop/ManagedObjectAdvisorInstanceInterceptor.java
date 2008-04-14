/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptors.aop;

import java.util.Map;
import java.util.WeakHashMap;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.InstanceAdvisorDelegate;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.ejb3.interceptors.container.ManagedObjectAdvisor;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ManagedObjectAdvisorInstanceInterceptor implements Interceptor
{
   private Map<Object, InstanceAdvisorDelegate> instanceAdvisorDelegates;
   
   public String getName()
   {
      return this.getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      @SuppressWarnings("unchecked")
      ManagedObjectAdvisor advisor = (ManagedObjectAdvisor)invocation.getAdvisor();

      InstanceAdvisorDelegate delegate = getInstanceAdvisorDelegate(invocation.getTargetObject(), advisor, advisor);
      advisor.setInstanceAdvisorDelegate(delegate);
      System.out.println("---> Using ia delegate " + delegate);

      return invocation.invokeNext();
   }
   
   private InstanceAdvisorDelegate getInstanceAdvisorDelegate(Object target, Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      if (instanceAdvisorDelegates == null)
      {
         instanceAdvisorDelegates = new WeakHashMap<Object, InstanceAdvisorDelegate>();
      }
      
      if (target == null)
      {
         throw new RuntimeException("Attempt to get instance advisor without having an instance");
      }
      
      InstanceAdvisorDelegate instanceAdvisorDelegate = instanceAdvisorDelegates.get(target);
      
      
      if(instanceAdvisorDelegate != null)
         return instanceAdvisorDelegate;

      synchronized (this)
      {
         if(instanceAdvisorDelegate == null)
         {
            instanceAdvisorDelegate = new InstanceAdvisorDelegate(advisor, instanceAdvisor);
            instanceAdvisorDelegate.initialize();
            instanceAdvisorDelegates.put(target, instanceAdvisorDelegate);
         }
      }
      return instanceAdvisorDelegate;
   }
}
