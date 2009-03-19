/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.remoting2;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.spi.Remotable;
import org.jboss.ejb3.remoting2.client.RemoteInvocationHandler;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EJB3ServerInvocationHandler implements ServerInvocationHandler
{
   private static final Logger log = Logger.getLogger(EJB3ServerInvocationHandler.class);

   public static final Object OID = RemoteInvocationHandler.OID;
   
   private Map<Object, Remotable> remotables = new ConcurrentHashMap<Object, Remotable>();
   
   private ServerInvoker invoker;
   private MBeanServer mbeanServer;
   
   /* (non-Javadoc)
    * @see org.jboss.remoting.ServerInvocationHandler#addListener(org.jboss.remoting.callback.InvokerCallbackHandler)
    */
   public void addListener(InvokerCallbackHandler callbackHandler)
   {
      // TODO Auto-generated method stub
      //
      throw new RuntimeException("NYI");
   }

   public void addRemotable(Remotable remotable)
   {
      log.debug("addRemotable " + remotable);
      Remotable previous = remotables.put(remotable.getId(), remotable);
      assert previous == null : "there was a previous remotable under " + remotable.getId();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.remoting.ServerInvocationHandler#invoke(org.jboss.remoting.InvocationRequest)
    */
   public Object invoke(InvocationRequest invocation) throws Throwable
   {
      Serializable key = (Serializable) invocation.getRequestPayload().get(OID);
      Remotable remotable = remotables.get(key);
      
      Object parameters[] = (Object[]) invocation.getParameter();
      SerializableMethod method = (SerializableMethod) parameters[0];
      Object args[] = (Object[]) parameters[1];
      ClassLoader loader = remotable.getClassLoader();
      Method realMethod = method.toMethod(loader);
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(loader);
         return realMethod.invoke(remotable.getTarget(), args);
      }
      catch(InvocationTargetException e)
      {
         throw e.getCause();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.remoting.ServerInvocationHandler#removeListener(org.jboss.remoting.callback.InvokerCallbackHandler)
    */
   public void removeListener(InvokerCallbackHandler callbackHandler)
   {
      // TODO Auto-generated method stub
      //
      throw new RuntimeException("NYI");
   }

   public void removeRemotable(Remotable remotable)
   {
      log.debug("removeRemotable " + remotable);
      Remotable previous = remotables.remove(remotable.getId());
      assert previous == remotable : "there was a different remotable under " + remotable.getId() + " (" + previous + " != " + remotable + ")";
   }
   
   /* (non-Javadoc)
    * @see org.jboss.remoting.ServerInvocationHandler#setInvoker(org.jboss.remoting.ServerInvoker)
    */
   public void setInvoker(ServerInvoker invoker)
   {
      this.invoker = invoker;
   }

   /* (non-Javadoc)
    * @see org.jboss.remoting.ServerInvocationHandler#setMBeanServer(javax.management.MBeanServer)
    */
   public void setMBeanServer(MBeanServer server)
   {
      this.mbeanServer = server;
   }
}
