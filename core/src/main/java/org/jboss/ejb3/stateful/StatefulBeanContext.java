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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJBContext;
import javax.persistence.EntityManager;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.ThreadLocalStack;
import org.jboss.ejb3.cache.CacheItem;
import org.jboss.ejb3.cache.Optimized;
import org.jboss.ejb3.interceptor.InterceptorInfo;
import org.jboss.ejb3.session.SessionSpecBeanContext;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.serial.io.MarshalledObject;
import org.jboss.tm.TxUtils;
import org.jboss.util.id.GUID;

/**
 * BeanContext for a stateful session bean.
 * 
 * Either bean or beanMO are always filled.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 * 
 * @version $Revision$
 */
public class StatefulBeanContext 
   extends SessionSpecBeanContext<StatefulContainer> 
   implements CacheItem, Externalizable, org.jboss.ejb3.tx.container.StatefulBeanContext<Object>
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -102470788178912606L;
   
   protected Object id;

   protected boolean txSynchronized = false;

   protected boolean inInvocation = false;

   protected MarshalledObject beanMO;

   protected ReentrantLock lock = new ReentrantLock();

   protected boolean discarded;

   // these two are needed for propagated extended persistence contexts when one
   // SFSB injects another.
//   public static ThreadLocalStack<StatefulBeanContext> propagatedContainedIn = new ThreadLocalStack<StatefulBeanContext>();

   public static ThreadLocalStack<StatefulBeanContext> currentBean = new ThreadLocalStack<StatefulBeanContext>();

//   protected StatefulBeanContext containedIn;

//   protected List<StatefulBeanContext> contains;

//   protected HashMap<String, EntityManager> persistenceContexts;
   protected Set<String> persistenceContextNames;

   protected boolean removed;

   protected String containerClusterUid;
   protected String containerGuid;
   protected boolean isClustered = false;
   
   protected boolean replicationIsPassivation = true;
   
   protected transient boolean passivated = false;
   
   protected Map<Object, Object> sharedState;

   /**
    * An incoming context from serialization.
    * 
    * @param container
    * @param beanMO
    */
   protected StatefulBeanContext(StatefulContainer container, MarshalledObject beanMO)
   {
      super(container);
      
      assert beanMO != null : "beanMO is null";
      
      this.containerClusterUid = Ejb3Registry.clusterUid(container);
      this.containerGuid = Ejb3Registry.guid(container);
      this.isClustered = container.isClustered();
      this.beanMO = beanMO;
   }
   
   /**
    * A brand new stateful session bean.
    * 
    * @param container
    * @param bean
    */
   protected StatefulBeanContext(StatefulContainer container, Object bean)
   {
      super(container, bean);
      
      this.containerClusterUid = Ejb3Registry.clusterUid(container);
      this.containerGuid = Ejb3Registry.guid(container);
      this.isClustered = container.isClustered();
      this.id = new GUID();
   }
   
   /**
    * Only for use by externalization; do not use elsewhere.
    *
    * @deprecated
    */
   public StatefulBeanContext()
   {
      
   }
   
   /**
    * FIXME this should be an invariant set in the constructor.
    * 
    * @param sharedState
    */
   protected void setSharedState(Map<Object, Object> sharedState)
   {
      assert this.sharedState == null : "Cannot re-set shared state";
      assert sharedState != null : "sharedState is null";
      
      this.sharedState = sharedState;
   }

//   public List<StatefulBeanContext> getContains()
//   {
//      if (bean == null)
//         extractBeanAndInterceptors();
//      return contains;
//   }
   
   /**
    * Makes a copy of the contains list so nested callback iterators
    * can iterate over it without concern that another thread will
    * remove the context.
    * 
    * TODO replace contains list with a concurrent collection
    */
//   private List<StatefulBeanContext> getThreadSafeContains()
//   {
//      // Call getContains() to ensure unmarshalling
//      List<StatefulBeanContext> orig = getContains();
//      List<StatefulBeanContext> copy = null;
//      if (orig != null)
//      {
//         synchronized (orig)
//         {
//            copy = new ArrayList<StatefulBeanContext>(orig);
//         }
//      }
//      return copy;
//   }

   @Override
   public EJBContext getEJBContext()
   {
      if(ejbContext == null)
         ejbContext = new StatefulSessionContextImpl(this);
      return ejbContext;
   }
   
//   public EntityManager getExtendedPersistenceContext(String id)
//   {
//      EntityManager found = null;
//      Map<String, EntityManager> extendedPCS = getExtendedPersistenceContexts();
//      if (extendedPCS != null)
//      {
//         found = extendedPCS.get(id);
//      }
//      if (found != null)
//         return found;
//      if (containedIn != null)
//      {
//         found = containedIn.getExtendedPersistenceContext(id);
//      }
//      return found;
//   }
//
//   public void addExtendedPersistenceContext(String id, EntityManager pc)
//   {
//      Map<String, EntityManager> extendedPCS = getExtendedPersistenceContexts();
//      if (extendedPCS == null)
//      {
//         extendedPCS = persistenceContexts = new HashMap<String, EntityManager>();
//      }
//      extendedPCS.put(id, pc);
//   }
   
   public EntityManager getExtendedPersistenceContext(String id)
   {      
      SharedXPC shared = (SharedXPC) sharedState.get(id);
      return (shared == null ? null : shared.getXPC());
   }

   public void addExtendedPersistenceContext(String id, EntityManager pc)
   {
      Set<String> ourPCs = getPersistenceContextNames();
      if (ourPCs == null)
      {
         ourPCs = persistenceContextNames = new HashSet<String>();
      }
      
      // getPersistenceContextNames() will result in deserializing sharedState
      // so now it's safe to assert it isn't null
      
      assert sharedState != null : "sharedState is null; must be provided by container";
      
      SharedXPC shared = (SharedXPC) sharedState.get(id);
      if (shared == null)
      {
         shared = new SharedXPC(pc);
         sharedState.put(id, shared);
      }
      
      shared.addSharedUser();
      
      ourPCs.add(id);
   }
   
//   public boolean scanForExtendedPersistenceContext(String id, StatefulBeanContext ignore)
//   {
//      if (this.equals(ignore))
//         return false;
//      
//      if (!removed)
//      {
//         Map<String, EntityManager> extendedPCS = getExtendedPersistenceContexts();
//         if (extendedPCS != null && extendedPCS.containsKey(id))
//            return true;
//      }
//      
//      if (getContains() != null)
//      {
//         synchronized (contains)
//         {
//            for (StatefulBeanContext contained : contains)
//            {
//               if (!contained.equals(ignore))
////               {
//                  if (contained.scanForExtendedPersistenceContext(id, ignore))
//                     return true;
//               }
//            }
//         }
//      }
//      
//      return false;
//   }
   
   public void removeExtendedPersistenceContext(String id)
   {
      Map<String, EntityManager> extendedPCS = getExtendedPersistenceContexts();
      if (extendedPCS != null)
      {
         extendedPCS.remove(id);
      }
      
      // getPersistenceContextNames() will result in deserializing sharedState
      // so now it's safe to assert it isn't null
      
      assert sharedState != null : "sharedState is null; must be provided by container";
      
      
      synchronized (sharedState)
      {
         SharedXPC xpc = (SharedXPC) sharedState.get(id);
         if (xpc.removeSharedUser() == 0)
         {
            sharedState.remove(id);
         }
      }
   }

   public Map<String, EntityManager> getExtendedPersistenceContexts()
   {
      if (persistenceContextNames == null)
      {
         if (bean == null)
            extractBeanAndInterceptors(); // unmarshall
      }
      
      Map<String, EntityManager> result = null; // TODO always return empty Map?
      if (persistenceContextNames != null)
      {
         result = new HashMap<String, EntityManager>();
         for (String id : persistenceContextNames)
         {
            result.put(id, ((SharedXPC) sharedState.get(id)).getXPC());
         }
      }
      return result;
   }
   
   protected Set<String> getPersistenceContextNames()
   {
      if (persistenceContextNames == null)
      {
         if (bean == null)
            extractBeanAndInterceptors(); // unmarshall
      }
      return persistenceContextNames;      
   }

//   public StatefulBeanContext getContainedIn()
//   {
//      return containedIn;
//   }

//   public StatefulBeanContext getUltimateContainedIn()
//   {
//      StatefulBeanContext child = this;
//      StatefulBeanContext parent = containedIn;
//      
//      while (parent != null)
//      {
//         child = parent;
//         parent = parent.getContainedIn();
//      }
//      
//      if (parent == null && this != child)
//      {
//         // Don't hand out a ref to our parent obtained by walking the
//         // tree. Rather, get it from its cache.  This gives the cache
//         // a chance to activate it if it hasn't been.  We don't want
//         // to mark the parent as in use though.
//         Cache<StatefulBeanContext> ultimateCache = ((StatefulContainer)child.getContainer()).getCache();
//         child = ultimateCache.get(child.getId(), false);
//      }
//      
//      return child;
//   }

//   public void addContains(StatefulBeanContext ctx)
//   {
//      if (getContains() == null)
//         contains = new ArrayList<StatefulBeanContext>();
//      
//      synchronized (contains)
//      {
//         contains.add(ctx);
//         ctx.containedIn = this;
//      }      
//   }
   
//   public void removeContains(StatefulBeanContext ctx)
//   {
//      if (getContains() != null) // call getContains() to ensure unmarshalling
//      {
//         // Need to be thread safe
//         synchronized (contains)
//         {
//            if (contains.remove(ctx))
//            {             
//               ctx.containedIn = null;
//            }
//         }
//         
//         if (removed)
//         {
//            // Close out any XPCs that are no longer referenced
//            cleanExtendedPCs();
//         }
//         
//         if (getCanRemoveFromCache())
//         {  
//            if (containedIn != null)
//            {
//               containedIn.removeContains(this);               
//            }
//            
//            //  Notify our cache to remove us as we no longer have children            
//            ((StatefulContainer) getContainer()).getCache().remove(getId());
//         }
//      }
//   }

//   public StatefulBeanContext pushContainedIn()
//   {
//      StatefulBeanContext thisPtr = this;
//      if (propagatedContainedIn.getList() != null)
//      {
//         // This is a nested stateful bean, within another stateful bean.
//         // We need to create a nested bean context. The nested one will 
//         // be put in the parent's list and owned by it. It is a special 
//         // class because we do not want to put its state in a separate
//         // marshalled object as we want to maintain object references 
//         // between it and its parent. 
//         
//         // We also do not want to put the nested context within its container's
//         // cache. If placed in the cache, it could be independently passivated,
//         // activated and replicated, again breaking object references due to
//         // independent marshalling. Instead, we return a proxy to it that will 
//         // be stored in its container's cache         
//         containedIn = propagatedContainedIn.get();
//         NestedStatefulBeanContext nested = new NestedStatefulBeanContext(getContainer(), bean);
//         nested.id = id;
//         nested.container = getContainer();
//         nested.containerClusterUid = containerClusterUid;
//         nested.containerGuid = containerGuid;
//         nested.isClustered = isClustered;
//         nested.replicationIsPassivation = replicationIsPassivation;
//         containedIn.addContains(nested);
//         thisPtr = new ProxiedStatefulBeanContext(nested);
//         
//         if (log.isTraceEnabled())
//         {
//            log.trace("Created ProxiedStatefulBeanContext for " + 
//                      containerGuid + "/" + id + " contained in " + 
//                      containedIn.getContainer().getIdentifier() + "/" + 
//                      containedIn.getId());
//         }
//      }
//      propagatedContainedIn.push(thisPtr);
//      return thisPtr;
//   }
   
   /**
    * Checks whether this context or any of its children are in use.
    */
   public boolean getCanPassivate()
   {
      boolean canPassivate = (removed || !inUse);
      
      // Just check contains directly; don't call getContains() since
      // getContains() will deserialize the beanMO. If the beanMO isn't 
      // deserialized it's safe to assume the children aren't in use
//      if (canPassivate && contains != null)
//      {
//         synchronized (contains)
//         {
//            for (StatefulBeanContext contained : contains)
//            {
//               if (!contained.getCanPassivate())
//               {
//                  canPassivate = false;
//                  break;
//               }
//            }
//         }
//      }
      
      return canPassivate;
   }

   /**
    * Notification from a non-clustered StatefulCache to inform
    * that we are about to be passivated.
    */
   public void prePassivate()
   {
      if (!removed && !passivated)
      {   
         // make sure we're unmarshalled
         if (bean == null)
            extractBeanAndInterceptors(); 
         getContainer().invokePrePassivate(this);
         passivated = true;
      }
      
//      // Pass the call on to any nested children
//      List<StatefulBeanContext> children = getThreadSafeContains();
//      if (children != null)
//      {
//         for (StatefulBeanContext contained : children)
//         {
//            contained.prePassivate();
//         }
//      }
   }

   /**
    * Notification from a non-clustered StatefulCache to inform
    * that we have been activated.
    */
   public void postActivate()
   {
      if (!removed && passivated)
      {  
         // make sure we're unmarshalled
         if (bean == null)
            extractBeanAndInterceptors(); 
         getContainer().invokePostActivate(this);
         passivated = false;
      }
      
//      // Pass the call on to any nested children
//      List<StatefulBeanContext> children = getThreadSafeContains();
//      if (children != null)
//      {  
//         for (StatefulBeanContext contained : children)
//         {
//            contained.postActivate();
//         }
//      }
   }
   
   /**
    * Notification from a ClusteredStatefulCache to inform
    * that a bean that is stored in the distributed cache is now
    * being passivated as well. Something of a misnomer
    * as it is possible the bean wasn't replicated (if it implements
    * Optimized it may have been activated and then a reference left
    * in the cache without the bean ever being replicated).
    */
   public void passivateAfterReplication()
   {
      if (!removed && !passivated)
      {  
         // make sure we're unmarshalled
         if (bean == null)
            extractBeanAndInterceptors(); 
         getContainer().invokePrePassivate(this);
         passivated = true;
      }
      
//      // Only bother informing children if we aren't already serialized.
//      // If we're serialized, so are they and there's no point.
//      // Notifying them would cause us to deserialize a beanMO to no purpose.
//      if (contains != null)
//      {
//         // Pass the call on to any nested children
//         List<StatefulBeanContext> children = getThreadSafeContains();
//         if (children != null)
//         {
//            for (StatefulBeanContext contained : children)
//            {
//               contained.passivateAfterReplication();
//            }
//         }
//      }
   }
   
   public void activateAfterReplication()
   {
      if (!removed && passivated)
      {  
         // make sure we're unmarshalled
         if (bean == null)
            extractBeanAndInterceptors(); 
         getContainer().invokePostActivate(this);
         passivated = false;
      }
      
//      // Pass the call on to any nested children
//      List<StatefulBeanContext> children = getThreadSafeContains();
//      if (children != null)
//      {
//         for (StatefulBeanContext contained : children)
//         {
//            contained.activateAfterReplication();
//         }
//      }
   }

   public boolean getReplicationIsPassivation()
   {
      return replicationIsPassivation;
   }

   public void setReplicationIsPassivation(boolean replicationIsPassivation)
   {
      this.replicationIsPassivation = replicationIsPassivation;
   }

   /**
    * Notification from a ClusteredStatefulCache before a bean is
    * replicated.
    */
   public void preReplicate()
   {
      if (!removed && replicationIsPassivation && !passivated)
      {  
         // make sure we're unmarshalled
         if (bean == null)
            extractBeanAndInterceptors(); 
         getContainer().invokePrePassivate(this);
         passivated = true;
      }
      
//      // Pass the call on to any nested children
//      List<StatefulBeanContext> children = getThreadSafeContains();
//      if (children != null)
//      {
//         for (StatefulBeanContext contained : children)
//         {
//            contained.preReplicate();
//         }
//      }
   }

   /**
    * Notification from a ClusteredStatefulCache after the bean
    * is fetched from the distributed cache. Something of a misnomer
    * as it is possible the bean wasn't replicated (if it implements
    * Optimized it can be fetched from the cache twice without ever
    * being replicated).
    */
   public void postReplicate()
   {
      // We may not have been replicated, so only invoke @PostActivate
      // if we are marked as passivated
      if (!removed && passivated)
      {  
         // make sure we're unmarshalled
         if (bean == null)
            extractBeanAndInterceptors(); 
         getContainer().invokePostActivate(this);
         passivated = false;
      }
      
//      // Pass the call on to any nested children
//      List<StatefulBeanContext> children = getThreadSafeContains();
//      if (children != null)
//      {
//         for (StatefulBeanContext contained : children)
//         {
//            contained.postReplicate();
//         }
//      }
   }

//   public void popContainedIn()
//   {
//      propagatedContainedIn.pop();
//   }

   public boolean isInUse()
   {
      return inUse;
   }

   public void setInUse(boolean inUse)
   {
      this.inUse = inUse;
   }

   public boolean isDiscarded()
   {
      return discarded;
   }

   public void setDiscarded(boolean discarded)
   {
      this.discarded = discarded;
   }

   public ReentrantLock getLock()
   {
      return lock;
   }

   public boolean isInInvocation()
   {
      return inInvocation;
   }

   public void setInInvocation(boolean inInvocation)
   {
      this.inInvocation = inInvocation;
   }

   public Object getId()
   {
      return id;
   }

   public boolean isTxSynchronized()
   {
      return txSynchronized;
   }

   public void setTxSynchronized(boolean txSynchronized)
   {
      this.txSynchronized = txSynchronized;
   }

   public boolean isRemoved()
   {
      return removed;
   }

   public void remove()
   {
      if (removed)
         return;
      removed = true;
      RuntimeException exceptionThrown = null;
      
      // Close any XPCs that haven't been injected into live 
      // beans in our family
      try
      {
         cleanExtendedPCs();
      }
      catch (RuntimeException e)
      {
         // we still need to remove ourself from any parent, so save
         // the thrown exception and rethrow it after we have cleaned up.
         if (exceptionThrown == null)
            exceptionThrown = e;
      }
      
//      if (containedIn != null && getCanRemoveFromCache())
//      {
//         try
//         {
//            containedIn.removeContains(this);
//         }
//         catch (RuntimeException e)
//         {
//            // we still need to clean internal state, so save the
//            // thrown exception and rethrow it after we have cleaned up.
//            if (exceptionThrown == null)
//               exceptionThrown = e;
//         }
//      }
            
      // Clear out refs to our bean and interceptors, to reduce our footprint
      // in case we are still cached for our refs to any XPCs
      bean = null;
      interceptorInstances = null;
      
      if (exceptionThrown != null) throw new RuntimeException("exception thrown while removing SFSB", exceptionThrown);
   }
   
//   public boolean getCanRemoveFromCache()
//   {
//      boolean canRemove = removed;
//      
//      if (canRemove && getContains() != null) // call getContains() to ensure unmarshalling
//      {
//         synchronized (contains)
//         {
//            canRemove = (contains.size() == 0);
//         }
//      }
//      
//      return canRemove;
//   }
   
   private void cleanExtendedPCs()
   {    
      try
      {
         Transaction tx = TxUtil.getTransactionManager().getTransaction();
         if (tx != null && TxUtils.isActive(tx))
         {
            tx.registerSynchronization(new XPCCloseSynchronization(this));
         }
         else
         {
            closeExtendedPCs();
         }
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error cleaning PersistenceContexts in SFSB removal", e);
      }
   }

   private void closeExtendedPCs()
   {
      Set<String> extendedPCS = getPersistenceContextNames();
      if (extendedPCS != null)
      {
         RuntimeException exceptionThrown = null;
         
//         List<String> closedXPCs = new ArrayList<String>();
//         StatefulBeanContext topCtx = getUltimateContainedIn();
//         
//         for (Iterator<Map.Entry<String,EntityManager>> iter = extendedPCS.entrySet().iterator(); 
//               iter.hasNext();)
//         {
//            Map.Entry<String,EntityManager> entry = iter.next();
//            String id = entry.getKey();
//            EntityManager xpc = entry.getValue();
//            
//            // Only close the XPC if our live parent(s) or cousins 
//            // don't also have a ref to it
//            boolean canClose = topCtx.scanForExtendedPersistenceContext(id, this);
//            
//            if (canClose && getContains() != null)
//            {
//               // Only close the XPC if our live childrenScan don't have a ref
//               synchronized (contains)
//               {
//                  for (StatefulBeanContext contained : contains)
//                  {
//                     if (contained.scanForExtendedPersistenceContext(id, null))
//                     {
//                        canClose = false;
//                        break;
//                     }
//                  }
//               }
//            }
//            
//            if (canClose)
//            {
//               try
//               {
//                  xpc.close();
//                  closedXPCs.add(id);
//               }
//               catch (RuntimeException e)
//               {
//                  exceptionThrown = e;
//               }
//            }
//         }
         
         // Clean all refs to the closed XPCs from the tree
         for (String id : extendedPCS)
         {
            removeExtendedPersistenceContext(id);
         }            
         
         if (exceptionThrown != null) throw new RuntimeException("Error closing PersistenceContexts in SFSB removal", exceptionThrown);
      }
   }

   @Override
   public StatefulContainer getContainer()
   {
      if (container == null)
      {
         container = (StatefulContainer) Ejb3Registry.findContainer(containerGuid);

         if (isClustered && container == null)
            container = (StatefulContainer) Ejb3Registry.getClusterContainer(containerClusterUid);
      }

      return container;
   }

   @Override
   public Object getInstance()
   {
      if (bean == null)
      {
         extractBeanAndInterceptors();
      }
      assert bean != null : "bean is null";
      return bean;
   }   

   public boolean isModified()
   {
      Object ourBean = getInstance();
      if (ourBean instanceof Optimized)
      {
         return ((Optimized) ourBean).isModified();
      }
      return true;
   }

   @Override
   public SimpleMetaData getMetaData()
   {
      return super.getMetaData();
   }

   // these are public for fast concurrent access/update
   public volatile boolean markedForPassivation = false;
   
   public volatile boolean markedForReplication = false;
   
   // BES 2007/02/16 make private and use a getter/setter as
   // ProxiedStatefulBeanContext needs to pass the value on
   // to its NestedStatefulBeanContext
   private volatile boolean inUse = false;

   public long lastUsed = System.currentTimeMillis();

   @Override
   public Object[] getInterceptorInstances(InterceptorInfo[] interceptorInfos)
   {
      if (bean == null)
      {
         extractBeanAndInterceptors();
      }
      return super.getInterceptorInstances(interceptorInfos);
   }

   protected synchronized void extractBeanAndInterceptors()
   {
      if (beanMO == null)
         return;
      
      try
      {
         // First, ensure we've resolved our container
         getContainer();
         
         Object[] beanAndInterceptors = (Object[]) beanMO.get();
         bean = beanAndInterceptors[0];
         persistenceContextNames = (Set<String>) beanAndInterceptors[1];
         sharedState = (Map<Object, Object>) beanAndInterceptors[2];
         ArrayList list = (ArrayList) beanAndInterceptors[3];
         interceptorInstances = new HashMap<Class<?>, Object>();
         if (list != null)
         {
            for (Object o : list)
            {
               interceptorInstances.put(o.getClass(), o);
            }
         }
//         contains = (List<StatefulBeanContext>) beanAndInterceptors[4];
//         // Reestablish links to our children; if they serialize a link
//         // to us for some reason serialization blows up
//         if (contains != null)
//         {
//            for (StatefulBeanContext contained : contains)
//            {
//               contained.containedIn = this;
//            }
//         }
         
         // Don't hold onto the beanMo, as its contents are mutable
         // and we don't want to serialize a stale version of them
         beanMO = null;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public void writeExternal(ObjectOutput out) throws IOException
   {         
      out.writeUTF(containerClusterUid);
      out.writeUTF(containerGuid);
      out.writeBoolean(isClustered);
      out.writeObject(id);
      out.writeLong(lastUsed);
      out.writeObject(metadata);      
      out.writeBoolean(removed);
      out.writeBoolean(replicationIsPassivation);
      
      if (beanMO == null)
      {
         Object[] beanAndInterceptors = new Object[4];
         beanAndInterceptors[0] = bean;
         beanAndInterceptors[1] = persistenceContextNames;
         beanAndInterceptors[2] = sharedState;
         if (interceptorInstances != null && interceptorInstances.size() > 0)
         {
            ArrayList list = new ArrayList();
            list.addAll(interceptorInstances.values());
            beanAndInterceptors[3] = list;
         }
//         beanAndInterceptors[3] = contains;
         
         // BES 2007/02/12 Previously we were trying to hold a ref to
         // beanMO after we created it, but that exposes the risk of
         // two different versions of the constituent state that
         // can fall out of sync.  So now we just write a local variable.
         
         MarshalledObject mo = new MarshalledObject(beanAndInterceptors);
         out.writeObject(mo);         
      }
      else
      {
         // We've been deserialized and are now being re-serialized, but
         // extractBeanAndInterceptors hasn't been called in between.
         // This can happen if a passivated session is involved in a 
         // JBoss Cache state transfer to a newly deployed node
         // or in buddy replication data gravitation after failover.
         out.writeObject(beanMO);
      }
   }

   public void readExternal(ObjectInput in) throws IOException,
           ClassNotFoundException
   {
      containerClusterUid = in.readUTF();
      containerGuid = in.readUTF();
      isClustered = in.readBoolean();
      
      // Don't resolve the container in readExternal as it's possible it
      // doesn't exist, but deserialization still needs to work (e.g. we're 
      // being deserialized in a backup cache on a node where this bean 
      // isn't deployed).  Wait to resolve the container until we get
      // a postReplicate/postActivate callback      
//      container = (StatefulContainer)Ejb3Registry.findContainer(containerGuid);    
//      if (isClustered && container == null)
//         container = (StatefulContainer)Ejb3Registry.getClusterContainer(containerClusterUid);
//   
//      if(container == null)
//         throw new IllegalStateException("Can't find container " + containerGuid);
      
      id = in.readObject();
      lastUsed = in.readLong();
      metadata = (SimpleMetaData) in.readObject();
      removed = in.readBoolean();
      replicationIsPassivation = in.readBoolean();
      
      beanMO = (MarshalledObject) in.readObject();
      
      // If we've just been deserialized, we are passivated
      passivated = true;
   }

   public Object getInvokedMethodKey()
   {
      return this.getId();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      
      // Don't use instanceof check here as subclasses w/ same id are not equal
      if (obj != null && obj.getClass() == getClass())
      {
         StatefulBeanContext other = (StatefulBeanContext) obj;
         return (containerClusterUid.equals(other.containerClusterUid) && id.equals(other.id));
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      int result = 11;
      result = 29 * result + containerClusterUid.hashCode();
      result = 29 * result + id.hashCode();
      return result;
   }
   
   private static class XPCCloseSynchronization implements Synchronization
   {
      private StatefulBeanContext ctx;
      
      private XPCCloseSynchronization(StatefulBeanContext context)
      {
         ctx = context;
      }
      
      public void beforeCompletion()
      {
      }

      public void afterCompletion(int status)
      {
         ctx.closeExtendedPCs();
         // Clean ref to ctx, as some TMs leak Synchronization refs
         ctx = null;
      }
   }
}
