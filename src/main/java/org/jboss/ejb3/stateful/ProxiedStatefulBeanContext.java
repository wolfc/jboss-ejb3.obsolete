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
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.EJBContext;
import javax.persistence.EntityManager;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.ejb3.interceptor.InterceptorInfo;

/**
 * Proxy to a NestedStatefulBeanContext that can be independently passivated,
 * activated and replicated without disturbing the object it is proxying.
 * 
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Brian Stansberry
 * 
 * @version $Revision$
 */
public class ProxiedStatefulBeanContext extends StatefulBeanContext implements Externalizable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5156610459343035743L;

   private transient StatefulBeanContext delegate;

   private Object oid;

   private String containerId;
   
   private StatefulBeanContextReference parentRef;

   public ProxiedStatefulBeanContext(StatefulBeanContext delegate)
   {
      super(delegate.getContainer(), delegate.getInstance());
      
      this.delegate = delegate;
      oid = delegate.getId();
      containerId = delegate.getContainer().getObjectName().getCanonicalName();
      parentRef = new StatefulBeanContextReference(delegate.getContainedIn());
      
      // HACK!! Clear superclass fields
      this.container = null;
      this.bean = null;
   }

   protected StatefulBeanContext getDelegate()
   {
      if (delegate == null)
      {
         for (StatefulBeanContext ctx : parentRef.getBeanContext().getContains())
         {
            Object matchingOid = ctx.getId();
            if (oid.equals(matchingOid)
                  && ctx.getContainer().getObjectName().getCanonicalName()
                        .equals(containerId))
            {
               delegate = ctx;
               break;
            }
         }
         if (delegate == null)
            throw new RuntimeException("Failed to read delegate");
         
         // If we just read our delegate, it's possible there's been a 
         // failover and a remote node still has a ref to a stale delegate.
         // So, we should be replicated to invalidate the remote node.
         this.markedForReplication = true;
      }
      return delegate;
   }

   public void writeExternal(ObjectOutput out) throws IOException
//   private void writeObject(java.io.ObjectOutputStream out)
//         throws IOException
   {
      out.writeObject(oid);
      out.writeUTF(containerId);
      out.writeObject(parentRef);
   }

   public void readExternal(ObjectInput in) throws IOException,
         ClassNotFoundException
//   private void readObject(java.io.ObjectInputStream in)
//         throws IOException, ClassNotFoundException
   {
      oid = in.readObject();
      containerId = in.readUTF();
      parentRef = (StatefulBeanContextReference) in.readObject();
      
      assert parentRef != null : "parentRef is null";
   }

   @Override
   public List<StatefulBeanContext> getContains()
   {
      return getDelegate().getContains();
   }

   @Override
   public EntityManager getExtendedPersistenceContext(String id)
   {
      return getDelegate().getExtendedPersistenceContext(id);
   }

   @Override
   public void addExtendedPersistenceContext(String id, EntityManager pc)
   {
      getDelegate().addExtendedPersistenceContext(id, pc);
   }

   @Override
   public Map<String, EntityManager> getExtendedPersistenceContexts()
   {
      return getDelegate().getExtendedPersistenceContexts();
   }

   @Override
   public void removeExtendedPersistenceContext(String id)
   {
      getDelegate().removeExtendedPersistenceContext(id);
   }

   @Override
   public boolean scanForExtendedPersistenceContext(String id, StatefulBeanContext ignore)
   {
      return getDelegate().scanForExtendedPersistenceContext(id, ignore);
   }

   @Override
   public StatefulBeanContext getContainedIn()
   {
      return getDelegate().getContainedIn();
   }

   @Override
   public void addContains(StatefulBeanContext ctx)
   {
      getDelegate().addContains(ctx);
   }

   @Override
   public void removeContains(StatefulBeanContext ctx)
   {
      getDelegate().removeContains(ctx);
   }

   @Override
   public StatefulBeanContext pushContainedIn()
   {
      return getDelegate().pushContainedIn();
   }

   @Override
   public void popContainedIn()
   {
      getDelegate().popContainedIn();
   }

   @Override
   public StatefulBeanContext getUltimateContainedIn()
   {
      return getDelegate().getUltimateContainedIn();
   }

   @Override
   public boolean isInUse()
   {
      // Don't call delegate
      return super.isInUse();
   }

   @Override
   public void setInUse(boolean inUse)
   {
      super.setInUse(inUse);
      // delegate needs to know this for getCanPassivate()
      getDelegate().setInUse(inUse);
      
      if (!inUse)
      {
         // drop ref to delegate, as the delegate can be passivated/activated
         // without our knowledge, and if that happens we have a ref to a
         // stale delegate.
         delegate = null;
      }
   }

   @Override
   public boolean isDiscarded()
   {
      return getDelegate().isDiscarded();
   }

   @Override
   public void setDiscarded(boolean discarded)
   {
      getDelegate().setDiscarded(discarded);
   }

   @Override
   public boolean isRemoved()
   {
      return getDelegate().isRemoved();
   }

   @Override
   public ReentrantLock getLock()
   {
      return getDelegate().getLock();
   }

   @Override
   public boolean isInInvocation()
   {
      return getDelegate().isInInvocation();
   }

   @Override
   public void setInInvocation(boolean inInvocation)
   {
      getDelegate().setInInvocation(inInvocation);
   }

   @Override
   public Object getId()
   {
      return getDelegate().getId();
   }

//   @Override
//   public void setId(Object id)
//   {
//      this.oid = id;
//      getDelegate().setId(id);
//   }

   @Override
   public boolean isTxSynchronized()
   {
      return getDelegate().isTxSynchronized();
   }

   @Override
   public void setTxSynchronized(boolean txSynchronized)
   {
      getDelegate().setTxSynchronized(txSynchronized);
   }

   @Override
   public void remove()
   {
      getDelegate().remove();
   }

//   @Override
//   public void setContainer(Container container)
//   {
//      getDelegate().setContainer(container);
//   }
//
   @Override
   public StatefulContainer getContainer()
   {
      return getDelegate().getContainer();
   }

   @Override
   public Object getInstance()
   {
      return getDelegate().getInstance();
   }

   @Override
   public SimpleMetaData getMetaData()
   {
      return getDelegate().getMetaData();
   }

   @Override
   public Object[] getInterceptorInstances(InterceptorInfo[] interceptorInfos)
   {
      return getDelegate().getInterceptorInstances(interceptorInfos);
   }

   @Override
   public void extractBeanAndInterceptors()
   {
      getDelegate().extractBeanAndInterceptors();
   }

   /*
   @Override
   public void initialiseInterceptorInstances()
   {
      getDelegate().initialiseInterceptorInstances();
   }
   */

   @Override
   public EJBContext getEJBContext()
   {
      return getDelegate().getEJBContext();
   }

   /**
    *  Ignores the call, as passivation of this proxy context
    *  does not affect the underlying bean (which is passivated
    *  along with its parent context).
    */
   @Override
   public void prePassivate()
   {
   }

   /**
    *  Ignores the call, as activation of this proxy context
    *  does not affect the underlying bean (which is activated
    *  along with its parent context).
    */
   @Override
   public void postActivate()
   {
   }

   /**
    *  Ignores the call, as passivation of this proxy context
    *  does not affect the underlying bean (which is passivated
    *  along with its parent context).
    */
   @Override
   public void passivateAfterReplication()
   {
      // ignore
   }

   /**
    *  Ignores the call, as activation of this proxy context
    *  does not affect the underlying bean (which is activated
    *  along with its parent context).
    */
   @Override
   public void activateAfterReplication()
   {
      // ignore
   }

   @Override
   public boolean getCanPassivate()
   {
      if (delegate == null)
      {
         // If we haven't deserialized our delegate, we're not in use
         // on this node
         return true;
      }
      // Just check if *we* are in use; whether any children are
      // in use doesn't matter, since passivating this proxy
      // doesn't affect children
      return (isInUse() == false);
   }

   @Override
   public boolean getCanRemoveFromCache()
   {
      return getDelegate().getCanRemoveFromCache();
   }

   @Override
   public boolean getReplicationIsPassivation()
   {
      return getDelegate().getReplicationIsPassivation();
   }

   @Override
   public void setReplicationIsPassivation(boolean replicationIsPassivation)
   {
      getDelegate().setReplicationIsPassivation(replicationIsPassivation);
   }

   /**
    * Ignores the call, as replication of this proxy context
    * does not affect the underlying bean (which is replicated
    * along with its parent context).
    */
   @Override
   public void preReplicate()
   {
      // ignore
   }

   /**
    * Ignores the call, as replication of this proxy context
    * does not affect the underlying bean (which is replicated
    * along with its parent context).
    */
   @Override
   public void postReplicate()
   {
      // ignore
   }

   @Override
   public Object getInvokedMethodKey()
   {
      return getDelegate().getInvokedMethodKey();
   }

   @Override
   public Object getInterceptor(Class<?> interceptorClass) throws IllegalArgumentException
   {
      return getDelegate().getInterceptor(interceptorClass);
   }

   @Override
   public void initialiseInterceptorInstances()
   {
      getDelegate().initialiseInterceptorInstances();
   }
   
   

}
