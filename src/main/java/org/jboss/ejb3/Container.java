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
package org.jboss.ejb3;

import java.util.Hashtable;
import javax.ejb.TimerService;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.pool.Pool;
import org.jboss.ejb3.statistics.InvocationStatistics;

/**
 * Comment
 * 
 * A container is optionally associated with a security manager. If it is
 * the container is running in secured mode, if not the container is running
 * in unchecked mode.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public interface Container
{
   String ENC_CTX_NAME = "java:comp";

   // TODO: Wolf: I don't like this, why the '?', and why the public method
   /**
    * Create a bean context for use in a pool.
    * @return   a ready to use bean context
    */
   BeanContext<?> createBeanContext();
   
   Object getMBean();
   
   Class getBeanClass();

   String getEjbName();
   
   String getName();

   ObjectName getObjectName();

   TimerService getTimerService();

   /**
    * This method is called by EJBTimerServiceImpl to re-establish a persistent timer.
    */
   TimerService getTimerService(Object pKey);

   Pool getPool();

   void invokePostConstruct(BeanContext beanContext, Object[] params);

   void invokePreDestroy(BeanContext beanContext);

   void invokePostActivate(BeanContext beanContext);

   void invokePrePassivate(BeanContext beanContext);

   void invokeInit(Object bean, Class[] initTypes, Object[] initValues);

   BeanContext<?> peekContext();
   
   BeanContext<?> popContext();
   
   void pushContext(BeanContext<?> ctx);
   
   public void create() throws Exception;

   public void start() throws Exception;

   public void stop() throws Exception;

   public void destroy() throws Exception;

   InitialContext getInitialContext();

   Hashtable getInitialContextProperties();

   Context getEnc();

   void processMetadata();

   DependencyPolicy getDependencyPolicy();
   
   InvocationStatistics getInvokeStats();
   
   /**
    * Get the security manager associated with a container.
    * 
    * @param    type   the type to cast to
    * @return   the security manager or null if there is no manager associated
    */
   <T> T getSecurityManager(Class<T> type);
   
   boolean isClustered();
}
