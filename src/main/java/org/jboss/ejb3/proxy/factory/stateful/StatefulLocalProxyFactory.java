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
package org.jboss.ejb3.proxy.factory.stateful;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.dgc.VMID;

import javax.ejb.EJBLocalObject;
import javax.ejb.LocalHome;
import javax.naming.NamingException;

import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.handler.stateful.StatefulLocalHomeProxyInvocationHandler;
import org.jboss.ejb3.proxy.handler.stateful.StatefulLocalProxyInvocationHandler;
import org.jboss.ejb3.session.ProxyAccessType;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.proxy.ejb.handle.StatefulHandleImpl;
import org.jboss.util.NotImplementedException;
import org.jboss.util.naming.Util;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatefulLocalProxyFactory extends BaseStatefulProxyFactory
{
   private VMID vmid = Ejb3Registry.getVMID();
   
   /**
    * Do not call, only for externalizable
    */
   public StatefulLocalProxyFactory()
   {
      super();
   }
   
   public StatefulLocalProxyFactory(SessionSpecContainer container, LocalBinding binding)
   {
      super(container, binding.jndiBinding());
   }
   
   /**
    * Returns the interface type for Home
    * 
    * @return
    */
   @Override
   protected Class<?> getHomeType()
   {
      return ProxyFactoryHelper.getLocalHomeInterface(this.getContainer());
   }
   
   /**
    * Returns whether this Proxy Factory is local.  A Hack until EJB3 Proxy 
    * is in place, but this keeps us moving forward easily.
    * 
    * @deprecated Hack
    * @return
    */
   @Deprecated
   protected boolean isLocal()
   {
      return true;
   }

   /**
    * Defines the access type for this Proxies created by this Factory
    * 
    * @return
    */
   @Override
   protected ProxyAccessType getProxyAccessType(){
      return ProxyAccessType.LOCAL;
   }
   
   protected void validateEjb21Views()
   { 
      // Obtain Container
      SessionContainer container = this.getContainer();
      
      // Obtain @LocalHome
      LocalHome localHome = container.getAnnotation(LocalHome.class);

      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.validateEjb21Views(localHome == null ? null : localHome.value(), ProxyFactoryHelper
            .getLocalInterfaces(container));

   }
   
   /**
    * Whether or not to bind the home and business interfaces together
    * 
    * @return
    */
   @Override
   protected boolean bindHomeAndBusinessTogether()
   {
      return ProxyFactoryHelper.getLocalHomeJndiName(this.getContainer()).equals(this.jndiName);
   }

   public void start() throws Exception
   {
      super.start();

      try
      {
         Util.rebind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME, this);
      }
      catch (NamingException e)
      {
         NamingException namingException = new NamingException("Could not bind stateful local proxy with ejb name "
               + getContainer().getEjbName() + " into JNDI under jndiName: "
               + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }

      SessionContainer statefulContainer = (SessionContainer) getContainer();
      LocalHome localHome = this.getContainer().getAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether())
      {
         Class<?>[] interfaces =
         {localHome.value()};
         Object homeProxy = java.lang.reflect.Proxy.newProxyInstance(statefulContainer.getBeanClass().getClassLoader(),
               interfaces, new StatefulLocalHomeProxyInvocationHandler(statefulContainer));
         Util.rebind(statefulContainer.getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(statefulContainer),
               homeProxy);
      }
   }

   public void stop() throws Exception
   {
      super.stop();
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      SessionContainer statefulContainer = this.getContainer();
      LocalHome localHome = this.getContainer().getAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether())
      {
         Util.unbind(statefulContainer.getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(statefulContainer));
      }
   }

   public Object createProxyBusiness()
   {
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession();
      return this.createProxyBusiness(id);
   }
   
   public EJBLocalObject createProxyEjb21(String businessInterfaceType)
   {
      Object id = getContainer().createSession();
      return this.createProxyEjb21(id, businessInterfaceType);
   }
   
   public Object createProxyBusiness(String businessInterfaceType)
   {
      return this.createProxyBusiness(null, businessInterfaceType);
   }

   public Object createProxyBusiness(Object id)
   {
      return this.createProxyBusiness(id, null);
   }

   public Object createProxyBusiness(Object id, String businessInterfaceType)
   {
      return this.createProxy(id, SpecificationInterfaceType.EJB30_BUSINESS, businessInterfaceType);
   }

   @SuppressWarnings("unchecked")
   public <T extends EJBLocalObject> T createProxyEjb21(Object id, String businessInterfaceType)
   {
      return (T) this.createProxy(id, SpecificationInterfaceType.EJB21, null);
   }

   private Object createProxy(Object id, SpecificationInterfaceType type, String businessInterfaceType)
   {
      StatefulLocalProxyInvocationHandler proxy = new StatefulLocalProxyInvocationHandler(this.getContainer(), id,
            vmid, businessInterfaceType);
      return type.equals(SpecificationInterfaceType.EJB30_BUSINESS) ? this.constructProxyBusiness(proxy) : this
            .constructEjb21Proxy(proxy);
   }

   public Object createProxy(Class<?>[] initTypes, Object[] initValues)
   {
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession(initTypes, initValues);
      return this.createProxy(id, SpecificationInterfaceType.EJB30_BUSINESS, null);
   }

   public Object createProxyEjb21(Class<?>[] initTypes, Object[] initValues, String businessInterfaceType)
   {
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession(initTypes, initValues);
      return this.createProxyEjb21(id, businessInterfaceType);
   }

   protected StatefulHandleImpl createHandle()
   {
      throw new NotImplementedException("NYI");
   }
   
   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      vmid = (VMID)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(vmid);
   }
}
