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
package org.jboss.ejb3.cache;

import java.util.HashMap;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.Pool;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.util.id.GUID;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class NoPassivationCache implements StatefulCache
{
   private Pool pool;
   private HashMap cacheMap;

   public void initialize(Container container) throws Exception
   {
      this.pool = container.getPool();
      cacheMap = new HashMap();
   }

   public NoPassivationCache()
   {
   }

   public void start()
   {
   }

   public void stop()
   {
      synchronized (cacheMap)
      {
         cacheMap.clear();
      }
   }

   public StatefulBeanContext create()
   {
      StatefulBeanContext ctx = null;
      try
      {
         ctx = (StatefulBeanContext) pool.get();
         synchronized (cacheMap)
         {
            cacheMap.put(ctx.getId(), ctx);
         }
      }
      catch (EJBException e)
      {
         e.printStackTrace();
         throw e;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new EJBException(e);
      }
      return ctx;
   }

   public StatefulBeanContext create(Class[] initTypes, Object[] initValues)
   {
      StatefulBeanContext ctx = null;
      try
      {
         ctx = (StatefulBeanContext) pool.get(initTypes, initValues);
         synchronized (cacheMap)
         {
            cacheMap.put(ctx.getId(), ctx);
         }
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      return ctx;
   }

   public StatefulBeanContext get(Object key) throws EJBException
   {
      return get(key, true);
   }
   
   public StatefulBeanContext get(Object key, boolean markInUse) throws EJBException
   {
      StatefulBeanContext entry = null;
      synchronized (cacheMap)
      {
         entry = (StatefulBeanContext) cacheMap.get(key);
      }
      
      if (entry == null)
      {
         throw new NoSuchEJBException("Could not find Stateful bean: " + key);
      }      
      
      if (markInUse)
      {   
         if (entry.isRemoved())
         {
            throw new NoSuchEJBException("Could not find stateful bean: " + key +
                                         " (bean was marked as removed");
         }      
      
         entry.setInUse(true);
         entry.lastUsed = System.currentTimeMillis();
      }
      
      return entry;
   }

   public void finished(StatefulBeanContext ctx)
   {
      synchronized (ctx)
      {
         ctx.setInUse(false);
         ctx.lastUsed = System.currentTimeMillis();
      }
   }

   public void remove(Object key)
   {
      StatefulBeanContext ctx = null;
      synchronized (cacheMap)
      {
         ctx = (StatefulBeanContext) cacheMap.remove(key);
      }
      if (ctx != null) pool.remove(ctx);
   }


}
