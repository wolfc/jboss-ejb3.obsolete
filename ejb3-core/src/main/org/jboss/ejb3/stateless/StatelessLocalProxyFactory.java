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
package org.jboss.ejb3.stateless;

import java.lang.reflect.InvocationTargetException;
import javax.ejb.LocalHome;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.JBossProxy;
import org.jboss.ejb3.NonSerializableFactory;
import org.jboss.ejb3.ProxyFactoryHelper;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessLocalProxyFactory extends BaseStatelessProxyFactory
{
   protected Class[] getInterfaces()
   {
      Class[] interfaces;
      
      EJBContainer statelessContainer = (EJBContainer) container;
      LocalHome localHome = (LocalHome) statelessContainer.resolveAnnotation(LocalHome.class);
      
      boolean bindTogether = false;
      
      if (localHome != null && bindHomeAndBusinessTogether(statelessContainer))
         bindTogether = true;
      
      Class[] localInterfaces = ProxyFactoryHelper.getLocalInterfaces(container);
      
      if (bindTogether)
         interfaces = new Class[localInterfaces.length + 3];
      else
         interfaces = new Class[localInterfaces.length + 2];

      System.arraycopy(localInterfaces, 0, interfaces, 0, localInterfaces.length);
      interfaces[localInterfaces.length] = JBossProxy.class;
      interfaces[localInterfaces.length + 1] = javax.ejb.EJBLocalObject.class;
      if (bindTogether)
         interfaces[localInterfaces.length + 2] = localHome.value();

      return interfaces;

   }
   
   protected boolean bindHomeAndBusinessTogether(EJBContainer container)
   {
      return ProxyFactoryHelper.getLocalHomeJndiName(container).equals(ProxyFactoryHelper.getLocalJndiName(container));
   }

   protected void initializeJndiName()
   {
      jndiName = ProxyFactoryHelper.getLocalJndiName(container);
   }

   @Override
   public void start() throws Exception
   {
      super.start();
      EJBContainer statelessContainer = (EJBContainer) container;
      LocalHome localHome = (LocalHome) statelessContainer.resolveAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         Class[] interfaces = {localHome.value()};
         Object homeProxy = java.lang.reflect.Proxy.newProxyInstance(container.getBeanClass().getClassLoader(),
                                                                     interfaces, new StatelessLocalProxy(container));
         NonSerializableFactory.rebind(container.getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(container), homeProxy);
      }
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();
      EJBContainer statelessContainer = (EJBContainer) container;
      LocalHome localHome = (LocalHome) statelessContainer.resolveAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether(statelessContainer))
      {
         NonSerializableFactory.unbind(container.getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(container));
      }
   }


   public Object createProxy()
   {
      /*
      try
      {
         Object[] args = {new StatelessLocalProxy(container)};
         return proxyConstructor.newInstance(args);
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());  //To change body of catch statement use Options | File Templates.
      }
      */
      return constructProxy(new StatelessLocalProxy(container));
   }

   protected StatelessHandleImpl getHandle()
   {
      StatelessHandleImpl handle = new StatelessHandleImpl();
      LocalBinding remoteBinding = (LocalBinding) advisor.resolveAnnotation(LocalBinding.class);
      if (remoteBinding != null)
         handle.jndiName = remoteBinding.jndiBinding();

      return handle;
   }

}
