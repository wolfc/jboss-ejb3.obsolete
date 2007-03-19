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

import org.jboss.injection.Injector;
import org.jboss.annotation.ejb.cache.tree.CacheConfig;
import org.jboss.aop.Advisor;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class AbstractPool implements Pool
{
   private static final Logger log = Logger.getLogger(EJBContainer.class);

   protected Class beanClass;
   protected Class contextClass;
   protected Injector[] injectors;
   protected Container container;

   public AbstractPool()
   {

   }

   public void initialize(Container container, Class contextClass, Class beanClass, int maxSize, long timeout)
   {
      this.beanClass = beanClass;
      this.contextClass = contextClass;
      this.container = container;
   }

   public void setMaxSize(int maxSize)
   {
   }

   protected BeanContext create()
   {
      Object bean;
      BeanContext ctx;
      try
      {
         bean = container.construct();
         ctx = (BeanContext) contextClass.newInstance();
         ctx.setContainer(container);
         ctx.setInstance(bean);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      if (ctx instanceof StatefulBeanContext)
      {
         StatefulBeanContext sfctx = (StatefulBeanContext) ctx;
         sfctx.setId(new GUID());
         // Tell context how to handle replication
         Advisor advisor = (Advisor) container;
         CacheConfig config = (CacheConfig) advisor.resolveAnnotation(CacheConfig.class);
         if (config != null)
         {
            sfctx.setReplicationIsPassivation(config.replicationIsPassivation());
         }
         // this is for propagated extended PC's
         ctx = sfctx = sfctx.pushContainedIn();
      }
      try
      {
         if (injectors != null)
         {
            for (int i = 0; i < injectors.length; i++)
            {
               injectors[i].inject(ctx);
            }
         }

         ctx.initialiseInterceptorInstances();

      }
      finally
      {
         if (ctx instanceof StatefulBeanContext)
         {
            // this is for propagated extended PC's
            StatefulBeanContext sfctx = (StatefulBeanContext) ctx;
            sfctx.popContainedIn();
         }
      }

      //TODO This needs to be reimplemented as replacement for create() on home interface
      container.invokeInit(bean);

      container.invokePostConstruct(ctx, new Object[0]);
      return ctx;
   }

   protected BeanContext create(Class[] initTypes, Object[] initValues)
   {
      Object bean;
      BeanContext ctx;
      try
      {
         bean = container.construct();
         ctx = (BeanContext) contextClass.newInstance();
         ctx.setContainer(container);
         ctx.setInstance(bean);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      if (ctx instanceof StatefulBeanContext)
      {         
         StatefulBeanContext sfctx = (StatefulBeanContext) ctx;
         sfctx.setId(new GUID());
         // Tell context how to handle replication
         Advisor advisor = (Advisor) container;
         CacheConfig config = (CacheConfig) advisor.resolveAnnotation(CacheConfig.class);
         if (config != null)
         {
            sfctx.setReplicationIsPassivation(config.replicationIsPassivation());
         }
         // this is for propagated extended PC's
         ctx = sfctx = sfctx.pushContainedIn();
      }
      try
      {
         if (injectors != null)
         {
            for (int i = 0; i < injectors.length; i++)
            {
               injectors[i].inject(ctx);
            }
         }

         ctx.initialiseInterceptorInstances();

      }
      finally
      {
         if (ctx instanceof StatefulBeanContext)
         {
            // this is for propagated extended PC's
            StatefulBeanContext sfctx = (StatefulBeanContext) ctx;
            sfctx.popContainedIn();
         }
      }
      //TODO This needs to be reimplemented as replacement for create() on home interface
      container.invokeInit(bean, initTypes, initValues);

      container.invokePostConstruct(ctx, initValues);
      return ctx;
   }
   
   public void remove(BeanContext ctx)
   {
      try
      {
         container.invokePreDestroy(ctx);
      }
      finally
      {
         ctx.remove();
      }
   }

   public void discard(BeanContext ctx)
   {
      remove(ctx);
   }

   public void setInjectors(Injector[] injectors)
   {
      this.injectors = injectors;
   }
}
