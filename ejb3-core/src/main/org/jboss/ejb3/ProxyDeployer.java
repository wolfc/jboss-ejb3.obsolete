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
package org.jboss.ejb3;

import java.util.ArrayList;
import java.util.List;

import org.jboss.annotation.ejb.Clustered;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.LocalBindingImpl;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindingImpl;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.ejb.RemoteBindingsImpl;
import org.jboss.aop.Advisor;
import org.jboss.ejb3.remoting.RemoteProxyFactory;
import org.jboss.ejb3.service.ServiceContainer;
import org.jboss.ejb3.service.ServiceLocalProxyFactory;
import org.jboss.ejb3.service.ServiceRemoteProxyFactory;
import org.jboss.ejb3.stateful.StatefulClusterProxyFactory;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.stateful.StatefulLocalProxyFactory;
import org.jboss.ejb3.stateful.StatefulRemoteProxyFactory;
import org.jboss.ejb3.stateless.StatelessClusterProxyFactory;
import org.jboss.ejb3.stateless.StatelessLocalProxyFactory;
import org.jboss.ejb3.stateless.StatelessRemoteProxyFactory;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ProxyDeployer
{
   private static final Logger log = Logger.getLogger(ProxyDeployer.class);
   private Container container;
   private Advisor advisor;
   private ArrayList<ProxyFactory> proxyFactories = new ArrayList<ProxyFactory>();
   private RemoteBindings remoteBindings;
   private LocalBinding localBinding;

   public ProxyDeployer(Container container)
   {
      this.container = container;
      this.advisor = (Advisor) container;
   }

   public List<ProxyFactory> getProxyFactories() { return proxyFactories; }

   public void start() throws Exception
   {
      if (remoteBindings != null)
      {
         RemoteBinding[] list = remoteBindings.value();
         for (int i = 0; i < list.length; i++)
         {
            Class factoryClass = list[i].factory();
            if (factoryClass.equals(RemoteProxyFactory.class)) factoryClass = getDefaultRemoteProxyFactory();
            RemoteProxyFactory factory = (RemoteProxyFactory) factoryClass.newInstance();
            factory.setRemoteBinding(list[i]);
            factory.setContainer(container);
            factory.start();
            proxyFactories.add(factory);
         }
      }

      if (localBinding != null)
      {
         ProxyFactory factory = null;
         if (container instanceof StatefulContainer)
         {
            factory = new StatefulLocalProxyFactory();
         }
         else if (container instanceof ServiceContainer)
         {
            factory = new ServiceLocalProxyFactory();
         }
         else
         {
            factory = new StatelessLocalProxyFactory();
         }

         factory.setContainer(container);
         factory.start();
         proxyFactories.add(factory);
      }
   }

   public void initializeLocalBindingMetadata()
   {
      localBinding = (LocalBinding) advisor.resolveAnnotation(LocalBinding.class);
      if (localBinding == null)
      {
         if (ProxyFactoryHelper.getLocalInterfaces(container) != null)
         {
            localBinding = new LocalBindingImpl(ProxyFactoryHelper.getLocalJndiName(container));
            advisor.getAnnotations().addClassAnnotation(LocalBinding.class, localBinding);
         }
      }
   }

   public void initializeRemoteBindingMetadata()
   {
      remoteBindings = (RemoteBindings) advisor.resolveAnnotation(RemoteBindings.class);
      if (remoteBindings == null)
      {
         RemoteBinding binding = (RemoteBinding) advisor.resolveAnnotation(RemoteBinding.class);
         if (binding == null)
         {
            log.debug("no declared remote bindings for : " + container.getEjbName());
            if (ProxyFactoryHelper.getRemoteInterfaces(container) != null)
            {
               log.debug("there is remote interfaces for " + container.getEjbName());
               String jndiName = ProxyFactoryHelper.getDefaultRemoteJndiName(container);
               log.debug("default remote binding has jndiName of " + jndiName);
               String uri = ""; // use the default
               Class factory = null;
               factory = getDefaultRemoteProxyFactory();
               RemoteBinding[] list = {new RemoteBindingImpl(jndiName, "", uri, factory)};
               remoteBindings = new RemoteBindingsImpl(list);
               advisor.getAnnotations().addClassAnnotation(RemoteBindings.class, remoteBindings);
            }
         }
         else
         {
            RemoteBinding[] list = {binding};
            remoteBindings = new RemoteBindingsImpl(list);
            advisor.getAnnotations().addClassAnnotation(RemoteBindings.class, remoteBindings);
         }
      }
   }

   private Class getDefaultRemoteProxyFactory()
   {
      Class factory;
      if (container instanceof StatefulContainer)
      {
         if (advisor.resolveAnnotation(Clustered.class) != null)
         {
            factory = StatefulClusterProxyFactory.class;
         }
         else
         {
            factory = StatefulRemoteProxyFactory.class;
         }
      }
      else if (container instanceof ServiceContainer)
      {
         //TODO Implement clustering
         factory = ServiceRemoteProxyFactory.class;
      }
      else
      {
         if (advisor.resolveAnnotation(Clustered.class) != null)
         {
            factory = StatelessClusterProxyFactory.class;
         }
         else
         {
            factory = StatelessRemoteProxyFactory.class;
         }
      }
      return factory;
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
