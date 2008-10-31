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
package org.jboss.ejb3.test.singleton;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.aop.AbstractInterceptor;
import org.jboss.ejb3.test.singleton.lock.Lock;
import org.jboss.ejb3.test.singleton.lock.LockFactory;
import org.jboss.ejb3.test.singleton.lock.SimpleReadWriteLockFactory;
import org.jboss.logging.Logger;

/**
 * A SingletonLockInterceptor.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class SingletonLockInterceptor extends AbstractInterceptor
{
   private static final Logger log = Logger.getLogger(SingletonLockInterceptor.class);

   // container/instance lock
   private final Lock readLock;
   private final Lock writeLock;
   
   {
      LockFactory factory = new  SimpleReadWriteLockFactory(); //JUCReadWriteLockFactory();
      readLock = factory.createLock(true);
      writeLock = factory.createLock(false);
   }
   
   public String getName()
   {
      return "SingletonLockInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      //EJBContainer container = getEJBContainer(invocation);
      /* way to get to the metadata and determine whether the method has READ or WRITE lock
      JBossEnterpriseBeanMetaData xml = container.getXml();
      if(xml != null)
         log.info("metadata is available");
         */
      
      // for now consider methods starting with "get" as having READ lock
      boolean isReadMethod = false;
      String methodName = null;
      MethodInvocation mi = (MethodInvocation) invocation;
      if(mi.getMethod() != null)
      {
         methodName = mi.getMethod().getName();
         isReadMethod = methodName.startsWith("get") || methodName.startsWith("is");
         //log.info(container.getEjbName() + '.' + methodName + " is read concurrency: " + isReadMethod);
         //if(mi.getArguments() != null && mi.getArguments().length > 0)
         //   methodName += "(" + mi.getArguments()[0] + ')';
      }
      
      Lock lock = isReadMethod ? readLock : writeLock;      
      lock.lock();
      try
      {
         return invocation.invokeNext();
      }
      finally
      {
         lock.unlock();
      }
   }
}
