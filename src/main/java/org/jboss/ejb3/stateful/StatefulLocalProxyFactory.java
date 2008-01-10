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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import java.rmi.dgc.VMID;

import javax.ejb.LocalHome;
import javax.naming.NamingException;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.naming.Util;


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

   protected Class<?>[] getInterfaces()
   {      
      SessionContainer statefulContainer = (SessionContainer) getContainer();
      LocalHome localHome = (LocalHome) statefulContainer.resolveAnnotation(LocalHome.class);

      boolean bindTogether = false;

      if (localHome != null && bindHomeAndBusinessTogether(statefulContainer))
         bindTogether = true;

      // Obtain all local interfaces      
      Set<Class<?>> localInterfaces = new HashSet<Class<?>>();
      localInterfaces.addAll(Arrays.asList(ProxyFactoryHelper.getLocalAndBusinessLocalInterfaces(getContainer())));
      
      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.ensureEjb21ViewComplete(localHome == null ? null : localHome.value(), ProxyFactoryHelper
            .getLocalInterfaces(getContainer()));

      // Add JBossProxy
      localInterfaces.add(JBossProxy.class);

      // If binding along w/ home, add home
      if (bindTogether)
      {
         localInterfaces.add(localHome.value());
      }

      // Return
      return localInterfaces.toArray(new Class<?>[]
      {});

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
         NamingException namingException = new NamingException("Could not bind stateful local proxy with ejb name " + getContainer().getEjbName() + " into JNDI under jndiName: " + getContainer().getInitialContext().getNameInNamespace() + "/" + jndiName + PROXY_FACTORY_NAME);
         namingException.setRootCause(e);
         throw namingException;
      }

      SessionContainer statefulContainer = (SessionContainer) getContainer();
      LocalHome localHome = (LocalHome) ((EJBContainer) getContainer()).resolveAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Class[] interfaces = {localHome.value()};
         Object homeProxy = java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(),
                                                                     interfaces, new StatefulLocalHomeProxy(getContainer()));
         Util.rebind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()), homeProxy);
      }
   }

   public void stop() throws Exception
   {
      super.stop();
      Util.unbind(getContainer().getInitialContext(), jndiName + PROXY_FACTORY_NAME);
      SessionContainer statefulContainer = (SessionContainer) getContainer();
      LocalHome localHome = (LocalHome) ((EJBContainer) getContainer()).resolveAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether(statefulContainer))
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()));
      }
   }

   public Object createProxy()
   {
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession();
      return constructProxy(new StatefulLocalProxy(getContainer(), id, vmid));
   }

   public Object createProxy(Object id)
   {
      return constructProxy(new StatefulLocalProxy(getContainer(), id, vmid));
   }
   
   public Object createProxy(Class[] initTypes, Object[] initValues)
   {
      SessionContainer sfsb = (SessionContainer) getContainer();
      Object id = sfsb.createSession(initTypes, initValues);
      return constructProxy(new StatefulLocalProxy(getContainer(), id, vmid));
   }

   protected StatefulHandleImpl getHandle()
   {
      StatefulHandleImpl handle = new StatefulHandleImpl();
      LocalBinding remoteBinding = (LocalBinding) ((Advisor)getContainer()).resolveAnnotation(LocalBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding();

      return handle;
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
