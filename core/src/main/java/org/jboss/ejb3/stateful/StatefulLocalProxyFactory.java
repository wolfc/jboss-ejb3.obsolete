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
import java.rmi.dgc.VMID;

import javax.ejb.EJBLocalObject;
import javax.ejb.LocalHome;
import javax.naming.NamingException;

import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.session.SessionContainer;
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
   
   public StatefulLocalProxyFactory(SessionContainer container, LocalBinding binding)
   {
      super(container, binding.jndiBinding());
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
   
   protected boolean bindHomeAndBusinessTogether(SessionContainer container)
   {
      return ProxyFactoryHelper.getLocalHomeJndiName(container).equals(jndiName);
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
      if (localHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Class<?>[] interfaces =
         {localHome.value()};
         Object homeProxy = java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(),
               interfaces, new StatefulLocalHomeProxy(getContainer()));
         Util.rebind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()),
               homeProxy);
      }
   }

   public void stop() throws Exception
   {
      super.stop();
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      SessionContainer statefulContainer = (SessionContainer) getContainer();
      LocalHome localHome = this.getContainer().getAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()));
      }
   }

   public Object createProxy()
   {
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession();
      return this.createProxy(id);
   }

   public EJBLocalObject createProxyEjb21()
   {
      Object id = getContainer().createSession();
      return this.createProxyEjb21(id);
   }

   public Object createProxy(Object id)
   {
      return this.createProxy(id, SpecificationInterfaceType.EJB30_BUSINESS);
   }

   @SuppressWarnings("unchecked")
   public <T extends EJBLocalObject> T createProxyEjb21(Object id)
   {
      return (T)this.createProxy(id, SpecificationInterfaceType.EJB21);
   }

   private Object createProxy(Object id, SpecificationInterfaceType type)
   {
      StatefulLocalProxy proxy = new StatefulLocalProxy(this.getContainer(), id, vmid);
      return type.equals(SpecificationInterfaceType.EJB30_BUSINESS) ? this.constructBusinessProxy(proxy) : this
            .constructEjb21Proxy(proxy);
   }
   
   public Object createProxy(Class<?>[] initTypes, Object[] initValues)
   {
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession(initTypes, initValues);
      return this.createProxy(id, SpecificationInterfaceType.EJB30_BUSINESS);
   }
   
   public Object createEjb21Proxy(Class<?>[] initTypes, Object[] initValues){
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession(initTypes, initValues);
      return this.createProxy(id, SpecificationInterfaceType.EJB21);
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
