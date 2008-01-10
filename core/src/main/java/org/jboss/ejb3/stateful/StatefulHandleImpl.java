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

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.security.AccessControlException;
import java.util.Hashtable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.naming.InitialContext;

import org.jboss.ejb3.InitialContextFactory;
import org.jboss.invocation.Invoker;
import org.jboss.logging.Logger;
import org.jboss.naming.NamingContextFactory;

/**
 * An EJB stateful session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public class StatefulHandleImpl
   implements Handle
{
   private static final Logger log = Logger.getLogger(StatefulHandleImpl.class);
   
   /** Serial Version Identifier. */
   static final long serialVersionUID = -6324520755180597156L;

   /** A reference to {@link Handle#getEJBObject}. */
   protected static final Method GET_EJB_OBJECT;

   /** The value of our local Invoker.ID to detect when we are local. */
   private Object invokerID = null;

   /**
    * Initialize <tt>Handle</tt> method references.
    */
   static
   {
      try
      {
         GET_EJB_OBJECT = Handle.class.getMethod("getEJBObject", new Class[0]);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   public StatefulHandleImpl()
   {
      
   }
   
   /** The identity of the bean. */
   public int objectName;
   public String jndiName;
   public String invokerProxyBinding;
   public Invoker invoker;
   public Object id;

   /** The JNDI env in effect when the home handle was created */
   protected Hashtable jndiEnv;

   /** Create an ejb handle for a stateful session bean.
    * @param objectName - the session container jmx name
    * @param jndiName - the session home ejb name
    * @param invoker - the invoker to request the EJBObject from
    * @param invokerProxyBinding - the type of invoker binding
    * @param id - the session id
    */ 
   public StatefulHandleImpl(
      int objectName,
      String jndiName,
      Invoker invoker,
      String invokerProxyBinding,
      Object id,
      Object invokerID)
   {
      this.jndiName = jndiName;
      this.id = id;
      this.jndiEnv = (Hashtable) NamingContextFactory.lastInitialContextEnv.get();
      try
      {
         String property = System.getProperty("org.jboss.ejb.sfsb.handle.V327");
         if (property != null)
         {
            this.invokerProxyBinding = invokerProxyBinding;
            this.invokerID = invokerID;
            this.objectName = objectName;
            this.invoker = invoker;
         }
      }
      catch (AccessControlException ignored)
      {
      }

   }

   /**
    * @return the internal session identifier
    */
   public Object getID()
   {
      return id;
   }

   /**
    * @return the jndi name
    */
   public String getJNDIName()
   {
      return jndiName;
   }

   /**
    * Handle implementation.
    *
    * This differs from Stateless and Entity handles which just invoke
    * standard methods (<tt>create</tt> and <tt>findByPrimaryKey</tt>
    * respectively) on the Home interface (proxy).
    * There is no equivalent option for stateful SBs, so a direct invocation
    * on the container has to be made to locate the bean by its id (the
    * stateful SB container provides an implementation of
    * <tt>getEJBObject</tt>).
    *
    * This means the security context has to be set here just as it would
    * be in the Proxy.
    *
    * @return  <tt>EJBObject</tt> reference.
    *
    * @throws ServerException    Could not get EJBObject.
    */
   public EJBObject getEJBObject() throws RemoteException
   {
      try
      {
         InitialContext ic = InitialContextFactory.getInitialContext(jndiEnv);
    
         Proxy proxy = (Proxy) ic.lookup(jndiName);

         return (EJBObject) proxy;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         throw new RemoteException("Error during getEJBObject", t);
      }
   }
}

