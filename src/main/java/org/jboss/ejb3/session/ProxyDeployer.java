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
package org.jboss.ejb3.session;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;
import org.jboss.ejb3.annotation.defaults.RemoteBindingDefaults;
import org.jboss.ejb3.annotation.impl.LocalBindingImpl;
import org.jboss.ejb3.annotation.impl.RemoteBindingImpl;
import org.jboss.ejb3.annotation.impl.RemoteBindingsImpl;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.logging.Logger;

/**
 * Delegatee of a SessionContainer for managing proxy factories.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ProxyDeployer
{
   private static final Logger log = Logger.getLogger(ProxyDeployer.class);
   private SessionContainer container;
   private ArrayList<ProxyFactory> proxyFactories = new ArrayList<ProxyFactory>();
   private RemoteBindings remoteBindings;
   private LocalBinding localBinding;

   public ProxyDeployer(SessionContainer container)
   {
      assert container != null : "container is null";
      
      this.container = container;
   }

   public List<ProxyFactory> getProxyFactories() { return proxyFactories; }

   public void start() throws Exception
   {
      if (remoteBindings != null)
      {
         RemoteBinding[] list = remoteBindings.value();
         for(RemoteBinding binding : list)
         {
            assert binding.jndiBinding().length() != 0 : "jndiBinding not set on binding " + binding;
            
            RemoteProxyFactory factory;
            String factoryImplementationRegistryKey = binding.factory();
            if (factoryImplementationRegistryKey.equals(RemoteBindingDefaults.PROXY_FACTORY_DEFAULT))
            {
               factory = container.createRemoteProxyFactory(binding);
            }
            else
            {
               Class<? extends RemoteProxyFactory> remoteFactoryClass = container.getDeployment().getRemoteProxyFactoryRegistry().getProxyFactoryClass(binding.factory());
               Constructor<? extends RemoteProxyFactory> constructor = remoteFactoryClass.getConstructor(SessionContainer.class, RemoteBinding.class);
               factory = constructor.newInstance(container, binding);
            }
            factory.start();
            proxyFactories.add(factory);
         }
      }

      if (localBinding != null)
      {
         ProxyFactory factory = container.createProxyFactory(localBinding);
         factory.start();
         proxyFactories.add(factory);
      }
   }

   protected boolean hasJNDIBinding(String jndiName)
   {
      assert jndiName != null : "jndiName is null";
      
      if(localBinding != null)
      {
         if(localBinding.jndiBinding().equals(jndiName))
            return true;
      }
      
      if(remoteBindings != null)
      {
         for(RemoteBinding binding : remoteBindings.value())
         {
            if(binding.jndiBinding().equals(jndiName))
               return true;
         }
      }
      
      return false;
   }
   
   public void initializeLocalBindingMetadata()
   {
      localBinding = (LocalBinding) container.resolveAnnotation(LocalBinding.class);
      if (localBinding == null)
      {
         if (ProxyFactoryHelper.getLocalAndBusinessLocalInterfaces(container).length > 0)
         {
            localBinding = new LocalBindingImpl(ProxyFactoryHelper.getLocalJndiName(container));
            container.getAnnotations().addClassAnnotation(LocalBinding.class, localBinding);
         }
      }
   }
   
   private RemoteBinding initializeRemoteBinding(RemoteBinding binding)
   {
      if(binding.jndiBinding().length() == 0)
      {
         return new RemoteBindingImpl(ProxyFactoryHelper.getDefaultRemoteBusinessJndiName(container), binding
               .interceptorStack(), binding.clientBindUrl(), binding.factory());
      }
      return binding;
   }
   
   public void initializeRemoteBindingMetadata()
   {
      remoteBindings = (RemoteBindings) container.resolveAnnotation(RemoteBindings.class);
      if (remoteBindings == null)
      {
         RemoteBinding binding = (RemoteBinding) container.resolveAnnotation(RemoteBinding.class);
         if (binding == null)
         {
            log.debug("no declared remote bindings for : " + container.getEjbName());
            if (ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(container).length > 0)
            {
               log.debug("there is remote interfaces for " + container.getEjbName());
               String jndiName = ProxyFactoryHelper.getDefaultRemoteBusinessJndiName(container);
               log.debug("default remote binding has jndiName of " + jndiName);
               String uri = ""; // use the default
               RemoteBinding[] list = {new RemoteBindingImpl(jndiName, "", uri, RemoteBindingDefaults.PROXY_FACTORY_DEFAULT)};
               remoteBindings = new RemoteBindingsImpl(list);
               container.getAnnotations().addClassAnnotation(RemoteBindings.class, remoteBindings);
            }
         }
         else
         {
            RemoteBinding[] list = {initializeRemoteBinding(binding)};
            remoteBindings = new RemoteBindingsImpl(list);
            container.getAnnotations().addClassAnnotation(RemoteBindings.class, remoteBindings);
         }
      }
      else
      {
         List<RemoteBinding> list = new ArrayList<RemoteBinding>();
         for(RemoteBinding binding : remoteBindings.value())
         {
            list.add(initializeRemoteBinding(binding));
         }
         remoteBindings = new RemoteBindingsImpl(list);
         container.getAnnotations().addClassAnnotation(RemoteBindings.class, remoteBindings);
      }
   }

   public void stop() throws Exception
   {
      for (int i = 0; i < proxyFactories.size(); i++)
      {
         ProxyFactory factory = (ProxyFactory) proxyFactories.get(i);
         factory.stop();
      }
   }
}
