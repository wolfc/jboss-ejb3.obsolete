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
package org.jboss.ejb3.stateful;

import java.util.Iterator;
import java.util.List;

import java.rmi.RemoteException;

import javax.ejb.ConcurrentAccessException;
import javax.ejb.EJBException;
import javax.ejb.ApplicationException;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.metamodel.AssemblyDescriptor;
import org.jboss.annotation.ejb.SerializedConcurrentAccess;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulInstanceInterceptor implements Interceptor
{

   public StatefulInstanceInterceptor()
   {
   }

   public String getName()
   {
      return null;
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      StatefulContainerInvocation ejb = (StatefulContainerInvocation) invocation;
      Object id = ejb.getId();
      StatefulContainer container = (StatefulContainer) ejb.getAdvisor();
      StatefulBeanContext target = container.getCache().get(id);

      boolean block = container.resolveAnnotation(SerializedConcurrentAccess.class) != null;

      if (block)
      {
         target.getLock().lockInterruptibly();
      }
      else
      {
         synchronized (target)
         {
            if (target.isInInvocation()) throw new ConcurrentAccessException("no concurrent calls on stateful bean '" + container.getName() + "' (EJB3 4.3.13)");
            target.setInInvocation(true);
         }
      }
      ejb.setTargetObject(target.getInstance());
      ejb.setBeanContext(target);
      StatefulBeanContext.currentBean.push(target);
      try
      {
         if (target.isDiscarded()) throw new EJBException("SFSB was discarded by another thread");
         return ejb.invokeNext();
      }
      catch (Exception ex)
      {
         if (isApplicationException(ex.getClass(), container)) throw ex;
         if (ex instanceof RuntimeException
                 || ex instanceof RemoteException)
         {
            container.getCache().remove(id);
            target.setDiscarded(true);
         }
         throw ex;
      }
      finally
      {
         StatefulBeanContext.currentBean.pop();
         synchronized (target)
         {
            target.setInInvocation(false);
            if (!target.isTxSynchronized() && !target.isDiscarded()) container.getCache().finished(target);
            if (block) target.getLock().unlock();
         }
      }
   }
   
   public static boolean isApplicationException(Class<?> exceptionClass, EJBContainer container)
   {
      if (exceptionClass.isAnnotationPresent(ApplicationException.class))
         return true;
      
      AssemblyDescriptor assembly = container.getAssemblyDescriptor();
      if (assembly != null)
      {
         List exceptions = assembly.getApplicationExceptions();
         if (exceptions.size() > 0)
         {
            Iterator exceptionIterator = exceptions.iterator();
            while (exceptionIterator.hasNext())
            {
               org.jboss.ejb3.metamodel.ApplicationException exception = (org.jboss.ejb3.metamodel.ApplicationException)exceptionIterator.next();
               if (exception.getExceptionClass().equals(exceptionClass.getName()))
                  return true;
            }
         }
         
      }
      return false;
   }
}
