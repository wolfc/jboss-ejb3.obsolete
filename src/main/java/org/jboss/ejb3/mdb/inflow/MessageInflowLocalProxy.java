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
package org.jboss.ejb3.mdb.inflow;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.ejb3.mdb.MessagingContainer;
import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MessageInflowLocalProxy implements InvocationHandler
{
   private static final Logger log = Logger.getLogger(MessageInflowLocalProxy.class);
   
   /** The key for the factory */
   public static final String MESSAGE_ENDPOINT_FACTORY = "MessageEndpoint.Factory";

   /** The key for the xa resource */
   public static final String MESSAGE_ENDPOINT_XARESOURCE = "MessageEndpoint.XAResource";
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled(); 
   
   /** Cached version of our proxy string */
   private String cachedProxyString = null;
   
   /** Whether this proxy has been released */
   protected AtomicBoolean released = new AtomicBoolean(false);
   
   /** Whether we have delivered a message */
   protected boolean delivered = false;
   
   /** The in use thread */
   protected Thread inUseThread = null;
   
   /** The old classloader of the thread */
   protected ClassLoader oldClassLoader = null;
   
   /** Any transaction we started */
   protected Transaction transaction = null;
   
   /** Any suspended transaction */
   protected Transaction suspended = null;

   /** The message endpoint factory */
   private JBossMessageEndpointFactory endpointFactory;
   
   private XAResource resource;
   private MessageEndpointFactory messageEndpointFactory;
   
   MessagingContainer container;

   protected MessageInflowLocalProxy(MessagingContainer container)
   {
      this.container = container;
   }

   public void setMessageEndpointFactory(MessageEndpointFactory messageEndpointFactory)
   {
      this.messageEndpointFactory = messageEndpointFactory;
   }
   
   public void setXaResource(XAResource resource)
   {
      this.resource = resource;
   }

   public Object invoke(Object proxy, Method method, Object[] args)
           throws Throwable
   {
      // EJBTHREE-1209: if a proxy is used in debug statements return something useful
      if(method.getName().equals("toString") && method.getParameterTypes().length == 0)
         return container.toString();
      
      // Are we still useable?
      if (released.get())
         throw new IllegalStateException("This message endpoint + " + getProxyString(proxy) + " has been released");

      // Concurrent invocation?
      Thread currentThread = Thread.currentThread();
      if (inUseThread != null && inUseThread.equals(currentThread) == false)
         throw new IllegalStateException("This message endpoint + " + getProxyString(proxy) + " is already in use by another thread " + inUseThread);
      inUseThread = currentThread;
      
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " in use by " + method + " " + inUseThread);
      
      // Remember the return value
      final Object returnValue;

      // Which operation?
      if (method.getName().equals("release"))
      {
         release(proxy);
         returnValue = null;
      }
      else if (method.getName().equals("beforeDelivery"))
      {
         before(proxy, container, method, args);
         returnValue = null;
      }
      else if (method.getName().equals("afterDelivery"))
      {
         after(proxy);
         returnValue = null;
      }
      // Real inflow invocation (ie. from MessageListener.onMessage())
      else
      {
         // Tell invoke stats we're starting
         final InvocationStatistics invokeStats = container.getInvokeStats();
         invokeStats.callIn();
         try
         {
            final long start = System.currentTimeMillis();
            returnValue = delivery(proxy, container, method, args);
            final long elapsed = System.currentTimeMillis() - start;
            invokeStats.updateStats(method, elapsed);
            if(log.isTraceEnabled())
            {
               log.trace("Invocation took " + elapsed + "ms: " + method);
            }
         }
         finally
         {
            // Tell invoke stats we're done
            invokeStats.callOut();
         }

      }

      // Return
      return returnValue;
   }

   public String toString()
   {
      return container.getEjbName().toString();
   }
   
   // -----------------------------------------------------------
   
   /**
    * Release this message endpoint.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void release(Object proxy) throws Throwable
   {
      // We are now released
      released.set(true);

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " release " + Thread.currentThread());
      
      // Tidyup any outstanding delivery
      if (oldClassLoader != null)
      {
         try
         {
            finish("release", proxy, false);
         }
         catch (Throwable t)
         {
            log.warn("Error in release ", t);
         }
      }
   }
   
   /**
    * Before delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void before(Object proxy, MessagingContainer container, Method method, Object[] args) throws Throwable
   {
      // Called out of sequence
      if (oldClassLoader != null)
         throw new IllegalStateException("Missing afterDelivery from the previous beforeDelivery for message endpoint " + getProxyString(proxy));

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " before");

      // Set the classloader
      oldClassLoader = GetTCLAction.getContextClassLoader(inUseThread);
      SetTCLAction.setContextClassLoader(inUseThread, container.getClassloader());
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " set context classloader to " + container.getClassloader());

      // start any transaction
      try
      {
         // Is the delivery transacted?
         MethodInfo methodInfo = container.getMethodInfo((Method)args[0]);
         boolean isTransacted = messageEndpointFactory.isDeliveryTransacted(methodInfo.getAdvisedMethod());

         startTransaction("beforeDelivery", proxy, container, method, args, isTransacted);
      }
      catch (Throwable t)
      {
         resetContextClassLoader(proxy);
         throw new ResourceException(t);
      }
   }
   
   /**
    * After delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void after(Object proxy) throws Throwable
   {
      // Called out of sequence
      if (oldClassLoader == null)
         throw new IllegalStateException("afterDelivery without a previous beforeDelivery for message endpoint " + getProxyString(proxy));

      // Finish this delivery committing if we can
      try
      {
         finish("afterDelivery", proxy, true);
      }
      catch (Throwable t)
      {
         throw new ResourceException(t);
      }
   }
   
   /**
    * Delivery.
    * 
    * @param mi the invocation
    * @return the result of the delivery
    * @throws Throwable for any error
    */
   protected Object delivery(Object proxy, MessagingContainer container, Method method, Object[] args) throws Throwable
   {
      // Have we already delivered a message?
      if (delivered)
         throw new IllegalStateException("Multiple message delivery between before and after delivery is not allowed for message endpoint " + getProxyString(proxy));

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " delivering");
      
      // Mark delivery if beforeDelivery was invoked
      if (oldClassLoader != null)
         delivered = true;

      boolean commit = true;
      // Is the delivery transacted?
      MethodInfo methodInfo = container.getMethodInfo(method);

      try
      {
         // Check for starting a transaction
         if (oldClassLoader == null)
         {
            boolean isTransacted = messageEndpointFactory.isDeliveryTransacted(methodInfo.getAdvisedMethod());
            startTransaction("delivery", proxy, container, method, args, isTransacted);
         }
         return container.localInvoke(methodInfo, args);
      }
      catch (Throwable t)
      {
         if (trace)
            log.trace("MessageEndpoint " + getProxyString(proxy) + " delivery error", t);
         if (t instanceof Error || t instanceof RuntimeException)
         {
            if (transaction != null)
               transaction.setRollbackOnly();
            commit = false;
         }
         throw t;
      }
      finally
      {
         // No before/after delivery, end any transaction and release the lock
         if (oldClassLoader == null)
         {
            try
            {
               // Finish any transaction we started
               endTransaction(proxy, commit);
            }
            finally
            {
               releaseThreadLock(proxy);
            }
         }
      }
   }
   
   /**
    * Finish the current delivery
    * 
    * @param context the lifecycle method
    * @param mi the invocation
    * @param commit whether to commit
    * @throws Throwable for any error
    */
   protected void finish(String context, Object proxy, boolean commit) throws Throwable
   {
      try
      {
         endTransaction(proxy, commit);
      }
      finally
      {
         // Reset delivered flag
         delivered = false;
         // Change back to the original context classloader
         resetContextClassLoader(proxy);
         // We no longer hold the lock
         releaseThreadLock(proxy);
      }
   }

   /**
    * Start a transaction
    *  
    * @param context the lifecycle method
    * @param mi the invocation
    * @param container the container
    * @throws Throwable for any error
    */
   protected void startTransaction(String context, Object proxy, MessagingContainer container, Method m, Object[] args, boolean isTransacted) throws Throwable
   { 
      Method method;
      
      // Normal delivery      
      if ("delivery".equals(context))
         method = m;
      // Before delivery
      else
         method = (Method)args[0];

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " " + context + " method=" + method + " xaResource=" + resource + " transacted=" + isTransacted);

      // Get the transaction status
      TransactionManager tm = TxUtil.getTransactionManager(); //container.getTransactionManager();
      suspended = tm.suspend();

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " " + context + " currentTx=" + suspended);

      // Delivery is transacted
      if (isTransacted)
      {
         // No transaction means we start a new transaction and enlist the resource
         if (suspended == null)
         {
            tm.begin();
            transaction = tm.getTransaction();
            if (trace)
               log.trace("MessageEndpoint " + getProxyString(proxy) + " started transaction=" + transaction);
      
            // Enlist the XAResource in the transaction
            if (resource != null)
            {
               transaction.enlistResource(resource);
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(proxy) + " enlisted=" + resource);
            }
         }
         else
         {
            // If there is already a transaction we ignore the XAResource (by spec 12.5.9)
            try
            {
               tm.resume(suspended);
            }
            finally
            {
               suspended = null;
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(proxy) + " transaction=" + suspended + " already active, IGNORED=" + resource);
            }
         }
      }
   }
   
   /**
    * End the transaction
    * 
    * @param mi the invocation
    * @param commit whether to try to commit
    * @throws Throwable for any error
    */
   protected void endTransaction(Object proxy, boolean commit) throws Throwable
   {
      TransactionManager tm = null;
      Transaction currentTx = null;
      try
      {
         // If we started the transaction, commit it
         if (transaction != null)
         {
            tm = TxUtil.getTransactionManager(); //getContainer(mi).getTransactionManager();
            currentTx = tm.getTransaction();
            
            // Suspend any bad transaction - there is bug somewhere, but we will try to tidy things up
            if (currentTx != null && currentTx.equals(transaction) == false)
            {
               log.warn("Current transaction " + currentTx + " is not the expected transaction.");
               tm.suspend();
               tm.resume(transaction);
            }
            else
            {
               // We have the correct transaction
               currentTx = null;
            }
            
            // Commit or rollback depending on the status
            if (commit == false || transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(proxy) + " rollback");
               tm.rollback();
            }
            else
            {
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(proxy) + " commit");
               tm.commit();
            }
         }

         // If we suspended the incoming transaction, resume it
         if (suspended != null)
         {
            try
            {
               tm = TxUtil.getTransactionManager(); //getContainer(mi).getTransactionManager();
               tm.resume(suspended);
            }
            finally
            {
               suspended = null;
            }
         }
      }
      finally
      {
         // EJBTHREE-1142: We're done with the transaction
         transaction = null;
         
         // Resume any suspended transaction
         if (currentTx != null)
         {
            try
            {
               tm.resume(currentTx);
            }
            catch (Throwable t)
            {
               log.warn("MessageEndpoint " + getProxyString(proxy) + " failed to resume old transaction " + currentTx);
               
            }
         }
      }
   }
   
   /**
    * Reset the context classloader
    * 
    * @param mi the invocation
    */
   protected void resetContextClassLoader(Object proxy)
   {
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " reset classloader " + oldClassLoader);
      SetTCLAction.setContextClassLoader(inUseThread, oldClassLoader);
      oldClassLoader = null;
   }

   /**
    * Release the thread lock
    * 
    * @param mi the invocation
    */
   protected void releaseThreadLock(Object proxy)
   {
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(proxy) + " no longer in use by " + inUseThread);
      inUseThread = null;
   }
   
   /**
    * Get our proxy's string value.
    * 
    * @param mi the invocation
    * @return the string
    */
   protected String getProxyString(Object proxy)
   {
      if (cachedProxyString == null)
         cachedProxyString = container.getEjbName();
      return cachedProxyString;
   }

   /**
    * Get the message endpoint factory
    *
    * @return the message endpoint factory
    */
   protected JBossMessageEndpointFactory getMessageEndpointFactory(Invocation invocation)
   {
      if (endpointFactory == null)
      {
         MethodInvocation mi = (MethodInvocation)invocation;
         endpointFactory = (JBossMessageEndpointFactory) mi.getResponseAttachment(MESSAGE_ENDPOINT_FACTORY);
      }
      return endpointFactory;
   }
   
   /**
    * Get the container
    *
    * @return the container
    */
   protected MessagingContainer getContainer(Invocation mi)
   {
      return getMessageEndpointFactory(mi).getContainer();
   }
}
