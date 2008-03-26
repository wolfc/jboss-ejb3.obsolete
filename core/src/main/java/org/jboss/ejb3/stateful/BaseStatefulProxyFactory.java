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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.JndiProxyFactory;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class BaseStatefulProxyFactory extends org.jboss.ejb3.session.BaseSessionProxyFactory implements ProxyFactory
{
   // Class Members
   
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(BaseStatefulProxyFactory.class);
   
   protected static enum ProxyAccessType{
      REMOTE,LOCAL;
   }

//   protected Class proxyClass;
   
   /**
    * Proxy Constructor for the Business Interfaces' Proxy
    */
   private Constructor<?> businessProxyConstructor;
   
   /**
    * Proxy Constructor for the EJBObject/EJBLocalObject Proxy
    */
   private Constructor<?> ejb21ProxyConstructor; 
   
//   protected Context proxyFactoryContext;
   protected String jndiName;

   public static final String PROXY_FACTORY_NAME = "StatefulProxyFactory";
   
   /**
    * Do not call, only for externalizable
    */
   protected BaseStatefulProxyFactory()
   {
      super();
   }

   public BaseStatefulProxyFactory(SessionContainer container, String jndiName)
   {
      super(container);
      
      assert jndiName != null : "jndiName is null";
      
      this.jndiName = jndiName;
   }
   
   protected Object constructBusinessProxy(InvocationHandler handler)
   {
      // Return
      return this.constructProxy(handler, SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   protected Object constructEjb21Proxy(InvocationHandler handler)
   {
      // Return
      return this.constructProxy(handler, SpecificationInterfaceType.EJB21);
   }
   
   /**
    * Construct a new Proxy of the specified type using the 
    * specified handler as argument to the Constructor
    * 
    * @param handler
    * @param specType
    * @return
    */
   private Object constructProxy(InvocationHandler handler, SpecificationInterfaceType specType)
   {
      // Initialize
      Object obj = null;

      try
      {
         // Business Proxy
         if (specType.equals(SpecificationInterfaceType.EJB30_BUSINESS))
         {
            obj = this.businessProxyConstructor.newInstance(handler);
         }
         // EJBObject/EJBLocalObject
         else if (specType.equals(SpecificationInterfaceType.EJB21))
         {
            obj = this.ejb21ProxyConstructor.newInstance(handler);
         }
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if (t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw new RuntimeException(t);
      }

      // Ensure Proxy object was created
      assert obj != null : "Proxy Object must not be null";

      // Return
      return obj;
   }

   
   public void init() throws Exception
   {
      // Ensure EJB2.1 View is Complete
      this.ensureEjb21ViewComplete();
      
      // Create the Proxy Constructors
      this.createProxyConstructors();
   }
   
   /**
    * Creates the Proxy constructors
    */
   protected void createProxyConstructors() throws Exception
   {
      // Obtain interfaces to be used in the proxies
      Class<?>[] businessInterfaces = this.getInterfacesForBusinessProxy();
      Class<?>[] ejb21Interfaces = this.getInterfacesForEjb21Proxy();
      
      // Obtain this bean class' CL
      ClassLoader cl = this.getContainer().getBeanClass().getClassLoader();
      
      // Create proxy classes
      Class<?> businessProxyClass = java.lang.reflect.Proxy.getProxyClass(cl, businessInterfaces);
      Class<?> ejb21ProxyClass = java.lang.reflect.Proxy.getProxyClass(cl, ejb21Interfaces);
      
      // Obtain and set the proxy constructors 
      this.businessProxyConstructor = businessProxyClass.getConstructor(InvocationHandler.class);
      this.ejb21ProxyConstructor = ejb21ProxyClass.getConstructor(InvocationHandler.class);
   }

   public void start() throws Exception
   {
      init();

      Context ctx = getContainer().getInitialContext();
      Name name = ctx.getNameParser("").parse(jndiName);
      ctx = Util.createSubcontext(ctx, name.getPrefix(name.size() - 1));
      String atom = name.get(name.size() - 1);
      RefAddr refAddr = new StringRefAddr(JndiProxyFactory.FACTORY, jndiName + PROXY_FACTORY_NAME);
      Reference ref = new Reference("java.lang.Object", refAddr, JndiProxyFactory.class.getName(), null);
      try 
      {
         log.debug("Binding reference for " + getContainer().getEjbName() + " in JNDI at " + atom);
         Util.rebind(ctx, atom, ref);
      } catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful proxy with ejb name "
               + getContainer().getEjbName() + " into JNDI under jndiName: " + ctx.getNameInNamespace() + "/" + atom);
         namingException.setRootCause(e);
         throw namingException;
      }
   }
   
   /**
    * Obtains interfaces to be used in the business proxy
    * 
    * @return
    */
   protected Class<?>[] getInterfacesForBusinessProxy()
   {
      return this.getInterfacesForProxy(this.getProxyAccessType(), SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   /**
    * Obtains interfaces to be used in the EJB21 proxy
    * 
    * @return
    */
   protected Class<?>[] getInterfacesForEjb21Proxy()
   {
      return this.getInterfacesForProxy(this.getProxyAccessType(), SpecificationInterfaceType.EJB21);
   }
   
   /**
    * Returns an array of interfaces to be used for the proxy;
    * the proxy type will be dependent on 
    * 
    * @param business
    * @return
    */
   private Class<?>[] getInterfacesForProxy(ProxyAccessType accessType, SpecificationInterfaceType specType)
   {

      // Initialize
      Set<Class<?>> interfaces = new HashSet<Class<?>>();
      SessionContainer container = this.getContainer();

      // Initialize array of interfaces
      Class<?>[] intfs = null;

      // If Local
      if (accessType.equals(ProxyAccessType.LOCAL))
      {

         // If business
         if (specType.equals(SpecificationInterfaceType.EJB30_BUSINESS))
         {
            intfs = ProxyFactoryHelper.getLocalBusinessInterfaces(container);
         }
         // If EJBLocalObject
         else
         {
            intfs = ProxyFactoryHelper.getLocalInterfaces(container);
         }
      }
      // If remote
      else
      {
         // If business
         if (specType.equals(SpecificationInterfaceType.EJB30_BUSINESS))
         {
            intfs = ProxyFactoryHelper.getRemoteBusinessInterfaces(container);
         }
         // If EJBObject
         else
         {
            intfs = ProxyFactoryHelper.getRemoteInterfaces(container);
         }
      }

      // Add all interfaces
      for (Class<?> interfaze : intfs)
      {
         interfaces.add(interfaze);
      }

      // Add JBossProxy
      interfaces.add(JBossProxy.class);

      // Return
      return interfaces.toArray(new Class[]
      {});
   }

   public void stop() throws Exception
   {
      Util.unbind(getContainer().getInitialContext(), jndiName);
   }
   
   /**
    * Defines the access type for this Proxies created by this Factory
    * 
    * @return
    */
   protected abstract ProxyAccessType getProxyAccessType();
   
   protected abstract void ensureEjb21ViewComplete();

   protected final void initializeJndiName() {};
   
   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      try
      {
         init();
      }
      catch(Exception e)
      {
         log.error(e.getMessage(), e);
         throw new IOException(e.getMessage());
      }
      this.jndiName = in.readUTF();
   }
   
   @Override
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeUTF(jndiName);
   }
}
