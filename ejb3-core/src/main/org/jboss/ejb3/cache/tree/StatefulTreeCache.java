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
package org.jboss.ejb3.cache.tree;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.annotation.ejb.cache.tree.CacheConfig;
import org.jboss.aop.Advisor;
import org.jboss.cache.AbstractCacheListener;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.CacheSPI;
import org.jboss.cache.Fqn;
import org.jboss.cache.InvocationContext;
import org.jboss.cache.Region;
import org.jboss.cache.config.Option;
import org.jboss.cache.eviction.EvictionPolicyConfig;
import org.jboss.cache.eviction.LRUConfiguration;
import org.jboss.cache.jmx.CacheJmxWrapperMBean;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Pool;
import org.jboss.ejb3.cache.ClusteredStatefulCache;
import org.jboss.ejb3.stateful.NestedStatefulBeanContext;
import org.jboss.ejb3.stateful.ProxiedStatefulBeanContext;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulTreeCache implements ClusteredStatefulCache
{
   private static final int FQN_SIZE = 2; // depth of fqn that we store the session in.
   
   private static Option BYPASS_OPTION = new Option();
   private static Option LOCAL_ONLY_OPTION = new Option();
   private static Option GRAVITATE_OPTION = new Option();
   static
   {
      BYPASS_OPTION.setBypassInterceptorChain(true);
      LOCAL_ONLY_OPTION.setCacheModeLocal(true);
      GRAVITATE_OPTION.setForceDataGravitation(true);
   }
   
   private Logger log = Logger.getLogger(StatefulTreeCache.class);
   private Pool pool;
   private WeakReference<ClassLoader> classloader;
   private Cache cache;
   private Fqn cacheNode;
   private Region region;
   private ClusteredStatefulCacheListener listener;
   
   public static long MarkInUseWaitTime = 15000;
   
   protected long removalTimeout = 0; 
   protected RemovalTimeoutTask removalTask = null;
   protected boolean running = true;
   protected Map<Object, Long> beans = new ConcurrentHashMap<Object, Long>();
   protected Container container;

   public StatefulBeanContext create()
   {
      StatefulBeanContext ctx = null;
      try
      {
         ctx = (StatefulBeanContext) pool.get();
         if (log.isTraceEnabled())
         {
            log.trace("Caching context " + ctx.getId() + " of type " + ctx.getClass());
         }
         putInCache(ctx);
         ctx.setInUse(true);
         ctx.lastUsed = System.currentTimeMillis();
         beans.put(ctx.getId(), ctx.lastUsed);
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

   public StatefulBeanContext create(Class[] initTypes, Object[] initValues)
   {
      StatefulBeanContext ctx = null;
      try
      {
         ctx = (StatefulBeanContext) pool.get(initTypes, initValues);
         if (log.isTraceEnabled())
         {
            log.trace("Caching context " + ctx.getId() + " of type " + ctx.getClass());
         }
         putInCache(ctx);
         ctx.setInUse(true);
         ctx.lastUsed = System.currentTimeMillis();
         beans.put(ctx.getId(), ctx.lastUsed);
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
      Fqn id = new Fqn(cacheNode, key.toString());
      try
      {
         // If need be, gravitate
         InvocationContext ictx = cache.getInvocationContext();
         ictx.setOptionOverrides(getGravitateOption());
         entry = (StatefulBeanContext) cache.get(id, "bean");
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
      
      if (entry == null)
      {
         throw new NoSuchEJBException("Could not find stateful bean: " + key);
      }
      else if (markInUse && entry.isRemoved())
      {
         throw new NoSuchEJBException("Could not find stateful bean: " + key + 
                                      " (bean was marked as removed)");
      }
      
      entry.postReplicate();
      
      if (markInUse)
      {
         entry.setInUse(true);
         
         // Mark the Fqn telling the eviction thread not to passivate it yet.
         // Note the Fqn we use is relative to the region!
         region.markNodeCurrentlyInUse(new Fqn(key.toString()), MarkInUseWaitTime);
         entry.lastUsed = System.currentTimeMillis();
         beans.put(key, entry.lastUsed);
      }
      
      if(log.isTraceEnabled())
      {
         log.trace("get: retrieved bean with cache id " +id.toString());
      }
      
      return entry;
   }

   public void remove(Object key)
   {
      Fqn id = new Fqn(cacheNode, key.toString());
      try
      {
         if(log.isTraceEnabled())
         {
            log.trace("remove: cache id " +id.toString());
         }
         InvocationContext ictx = cache.getInvocationContext();
         ictx.setOptionOverrides(getGravitateOption());
         StatefulBeanContext ctx = (StatefulBeanContext) cache.get(id, "bean"); 
         
         if (ctx != null)
         {
            if (!ctx.isRemoved())
               pool.remove(ctx);
            
            if (ctx.getCanRemoveFromCache())
               cache.removeNode(id);
            
            beans.remove(key);
         }
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void finished(StatefulBeanContext ctx)
   {
      synchronized (ctx)
      {
         ctx.setInUse(false);
         ctx.lastUsed = System.currentTimeMillis();
         beans.put(ctx.getId(), ctx.lastUsed);
         // OK, it is free to passivate now.
         // Note the Fqn we use is relative to the region!
         region.unmarkNodeCurrentlyInUse(new Fqn(ctx.getId().toString()));
      }
   }

   public void replicate(StatefulBeanContext ctx)
   {
      // StatefulReplicationInterceptor should only pass us the ultimate
      // parent context for a tree of nested beans, which should always be
      // a standard StatefulBeanContext
      if (ctx instanceof NestedStatefulBeanContext)
      {
         throw new IllegalArgumentException("Received unexpected replicate call for nested context " + ctx.getId());
      }
      
      try
      {
         putInCache(ctx);
      }
      catch (CacheException e)
      {
         RuntimeException re = convertToRuntimeException(e);
         throw re;
      }
   }

   public void initialize(Container container) throws Exception
   {
      log = Logger.getLogger(getClass().getName() + "." + container.getEjbName());
      
      this.container = container;
      this.pool = container.getPool();
      ClassLoader cl = ((EJBContainer) container).getClassloader();
      this.classloader = new WeakReference<ClassLoader>(cl);
      
      Advisor advisor = (Advisor) container;
      CacheConfig config = (CacheConfig) advisor.resolveAnnotation(CacheConfig.class);
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName cacheON = new ObjectName(config.name());
      CacheJmxWrapperMBean mbean = (CacheJmxWrapperMBean) MBeanProxyExt.create(CacheJmxWrapperMBean.class, cacheON, server);
      cache = mbean.getCache();

      cacheNode = Fqn.fromString("/" + container.getEjbName() + "/");

      // Try to create an eviction region per ejb
      region = cache.getRegion(cacheNode, true);
      EvictionPolicyConfig epc = getEvictionPolicyConfig((int) config.idleTimeoutSeconds(),
            config.maxSize());
      region.setEvictionPolicy(epc);
      
      // Transfer over the state for the region
      region.registerContextClassLoader(cl);
      region.activate();
      
      log.debug("initialize(): created region: " +region + " for ejb: " +container.getEjbName());
   
      removalTimeout = config.removalTimeoutSeconds();
      if (removalTimeout > 0)
         removalTask = new RemovalTimeoutTask("SFSB Removal Thread - " + container.getObjectName().getCanonicalName());
   }
   
   protected EvictionPolicyConfig getEvictionPolicyConfig(int timeToLiveSeconds, int maxNodes)
   {
      LRUConfiguration epc = new LRUConfiguration();
      // Override the standard policy class
      epc.setEvictionPolicyClass(AbortableLRUPolicy.class.getName());
      epc.setTimeToLiveSeconds(timeToLiveSeconds);
      epc.setMaxNodes(maxNodes);
      return epc;
   }

   public void start()
   {
      // register to listen for cache events
      
      // TODO this approach may not be scalable when there are many beans 
      // since then we will need to go thru N listeners to figure out which 
      // one this event belongs to. Consider having a singleton listener
      listener = new ClusteredStatefulCacheListener();
      cache.addCacheListener(listener);
      
      if (removalTask != null)
         removalTask.start();
      
      running = true;
   }

   public void stop()
   {
      running = false;

      // Remove the listener
      cache.removeCacheListener(listener);
      
      try {
         // Remove locally. We do this to clean up the persistent store,
         // which is not affected by the region.deactivate call below.
         InvocationContext ctx = cache.getInvocationContext();
         ctx.setOptionOverrides(getLocalOnlyOption());
         cache.removeNode(cacheNode);
      } 
      catch (CacheException e) 
      {
         log.error("stop(): can't remove bean from the underlying distributed cache");
      }
      
      if (region != null)
      {
         region.deactivate();
         region.unregisterContextClassLoader();      
         
         // FIXME this method needs to be in Cache
         ((CacheSPI) cache).getRegionManager().removeRegion(region.getFqn());
         // Clear any queues
         region.resetEvictionQueues();
         region = null;
      }
      
      classloader = null;
      
      if (removalTask != null)
         removalTask.interrupt();
      
      log.debug("stop(): StatefulTreeCache stopped successfully for " +cacheNode);
   }
   
   private void putInCache(StatefulBeanContext ctx)
   {
      ctx.preReplicate();
      cache.put(new Fqn(cacheNode, ctx.getId().toString()), "bean", ctx);
      ctx.markedForReplication = false;      
   }
   
   /** 
    * Creates a RuntimeException, but doesn't pass CacheException as the cause
    * as it is a type that likely doesn't exist on a client.
    * Instead creates a RuntimeException with the original exception's
    * stack trace.
    */   
   private RuntimeException convertToRuntimeException(CacheException e)
   {
      RuntimeException re = new RuntimeException(e.getClass().getName() + " " + e.getMessage());
      re.setStackTrace(e.getStackTrace());
      return re;
   }

   /**
    * A CacheListener that allows us to get notifications of passivations and
    * activations and thus notify the cached StatefulBeanContext.
    */
   public class ClusteredStatefulCacheListener extends AbstractCacheListener
   {
      // BES 11/18/2006 using nodeActivated callback was causing stack overflow; 
      // switched to nodeLoaded, which gives direct access to the data      
      @Override
      public void nodeLoaded(Fqn fqn, boolean pre, Map nodeData)
      {
         // Ignore everything but "post" events for nodes in our region
         if(pre) return;
         if(fqn.size() != FQN_SIZE) return;
         if(!fqn.isChildOrEquals(cacheNode)) return;
         if (nodeData == null) return;
         
         StatefulBeanContext bean = (StatefulBeanContext) nodeData.get("bean");
         
         if(bean == null)
         {
            throw new IllegalStateException("nodeLoaded(): null bean instance.");
         }

         if(log.isTraceEnabled())
         {
            log.trace("nodeLoaded(): send postActivate event to bean at fqn: " +fqn);
         }
         
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         try
         {  
            ClassLoader cl = classloader.get();
            if (cl != null)
            {
               Thread.currentThread().setContextClassLoader(cl);
            }
            
            bean.activateAfterReplication();
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
         
      }

      @Override
      public void nodePassivated(Fqn fqn, boolean pre) 
      {
         // Ignore everything but "pre" events for nodes in our region
         if(!pre) return;
         if(fqn.size() != FQN_SIZE) return;
         if(!fqn.isChildOrEquals(cacheNode)) return;

         StatefulBeanContext bean = null;
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         try 
         {
            InvocationContext ctx = cache.getInvocationContext();
            ctx.setOptionOverrides(getBypassOption());
            bean = (StatefulBeanContext) cache.get(fqn, "bean");
            if (bean != null)
            {
               ClassLoader cl = classloader.get();
               if (cl != null)
               {
                  Thread.currentThread().setContextClassLoader(cl);
               }
               
               if (!bean.getCanPassivate())
               {
                  // Abort the eviction
                  throw new ContextInUseException("Cannot passivate bean " + fqn + 
                        " -- it or one if its children is currently in use");
               }
               
               if(log.isTraceEnabled())
               {
                  log.trace("nodePassivated(): send prePassivate event to bean at fqn: " +fqn);
               }
               
               bean.passivateAfterReplication();
            }
         } 
         catch (CacheException e) 
         {
            log.error("nodePassivate(): can't retrieve bean instance from: " +fqn + " with exception: " +e);
            return;
         }
         catch (NoSuchEJBException e)
         {
            // TODO is this still necessary? Don't think we
            // should have orphaned proxies any more
            if (bean instanceof ProxiedStatefulBeanContext)
            {
               // This is probably an orphaned proxy; double check and remove it
               try
               {
                  bean.getContainedIn();
                  // If that didn't fail, it's not an orphan
                  throw e;
               }
               catch (NoSuchEJBException n)
               {
                  log.debug("nodePassivated(): removing orphaned proxy at " + fqn);
                  try
                  {
                     cache.removeNode(fqn);
                  }
                  catch (CacheException c)
                  {
                     log.error("nodePassivated(): could not remove orphaned proxy at " + fqn, c);
                     // Just fall through and let the eviction try
                  }
               }
            }
            else
            {
               throw e;
            }
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
      }
   }
   
   private static Option getBypassOption()
   {
      try
      {
         return BYPASS_OPTION.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private static Option getLocalOnlyOption()
   {
      try
      {
         return LOCAL_ONLY_OPTION.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private static Option getGravitateOption()
   {
      try
      {
         return GRAVITATE_OPTION.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private class RemovalTimeoutTask extends Thread
   {
      public RemovalTimeoutTask(String name)
      {
         super(name);
      }

      public void run()
      {
         while (running)
         {
            try
            {
               Thread.sleep(removalTimeout * 1000);
            }
            catch (InterruptedException e)
            {
               running = false;
               return;
            }
            try
            {
               long now = System.currentTimeMillis();
               
               Iterator<Map.Entry<Object, Long>> it = beans.entrySet().iterator();
               while (it.hasNext())
               {
                  Map.Entry<Object, Long> entry = it.next();
                  long lastUsed = entry.getValue();
                  if (now - lastUsed >= removalTimeout * 1000)
                  {
                     remove(entry.getKey());
                  }
               }
            }
            catch (Exception ex)
            {
               log.error("problem removing SFSB thread", ex);
            }
         }
      }
   }
}
