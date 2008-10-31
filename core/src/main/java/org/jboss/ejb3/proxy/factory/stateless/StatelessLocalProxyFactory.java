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
package org.jboss.ejb3.proxy.factory.stateless;

import javax.ejb.EJBLocalObject;
import javax.ejb.LocalHome;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.SpecificationInterfaceType;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.handler.stateless.StatelessLocalProxyInvocationHandler;
import org.jboss.ejb3.session.ProxyAccessType;
import org.jboss.ejb3.session.SessionSpecContainer;
import org.jboss.ejb3.stateless.StatelessHandleRemoteImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.util.naming.Util;


/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class StatelessLocalProxyFactory extends BaseStatelessProxyFactory
{
   private static final Logger log = Logger.getLogger(StatelessLocalProxyFactory.class);
   
   public StatelessLocalProxyFactory(SessionSpecContainer container, LocalBinding binding)
   {
      super(container, ProxyFactoryHelper.getLocalJndiName(container));
   }
   
   /**
    * Returns the interface type for Home
    * 
    * @param container
    * @return
    */
   @Override
   protected Class<?> getHomeType()
   {
      return ProxyFactoryHelper.getLocalHomeInterface(this.getContainer());
   }
   
   @Override
   protected ProxyAccessType getProxyAccessType()
   {
      return ProxyAccessType.LOCAL;
   }

   
   protected void validateEjb21Views(){
      
      EJBContainer container = this.getContainer();
      
      LocalHome localHome = container.getAnnotation(LocalHome.class);
      
      // Ensure that if EJB 2.1 Components are defined, they're complete
      this.validateEjb21Views(localHome == null ? null : localHome.value(), ProxyFactoryHelper
            .getLocalInterfaces(container));
   }
   
   /**
    * Returns whether this Proxy Factory is local.  A Hack until EJB3 Proxy 
    * is in place, but this keeps us moving forward easily.
    * 
    * @deprecated Hack
    * @return
    */
   @Deprecated
   @Override
   protected boolean isLocal()
   {
      return true;
   }
   
   /**
    * Whether or not to bind the home and business interfaces together
    * 
    * @return
    */
   @Override
   protected boolean bindHomeAndBusinessTogether()
   {
      String localHomeJndiName = ProxyFactoryHelper.getLocalHomeJndiName(this.getContainer());
      if(localHomeJndiName!=null)
      {
         return localHomeJndiName.equals(jndiName);
      }
      return false;
   }

   @Override
   public void start() throws Exception
   {
      super.start();
      SessionSpecContainer statelessContainer = getContainer();
      LocalHome localHome = statelessContainer.getAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether())
      {
         Class<?>[] interfaces =
         {localHome.value()};
         Object homeProxy = java.lang.reflect.Proxy.newProxyInstance(getContainer().getBeanClass().getClassLoader(),
               interfaces, new StatelessLocalProxyInvocationHandler(getContainer(), null));
         Util.rebind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()), homeProxy);
      }
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();
      SessionSpecContainer statelessContainer = this.getContainer();
      LocalHome localHome = statelessContainer.getAnnotation(LocalHome.class);
      if (localHome != null && !bindHomeAndBusinessTogether())
      {
         Util.unbind(getContainer().getInitialContext(), ProxyFactoryHelper.getLocalHomeJndiName(getContainer()));
      }
   }

   public Object createProxyBusiness()
   {
      return this.createProxyBusiness(null);
   }

   public Object createProxyBusiness(String businessInterfaceType)
   {
      return this
            .constructProxyBusiness(new StatelessLocalProxyInvocationHandler(getContainer(), businessInterfaceType));
   }
   
   @SuppressWarnings("unchecked")
   public <T extends EJBLocalObject> T createProxyEjb21(String businessInterfaceType)
   {
      return (T) this.createProxy(SpecificationInterfaceType.EJB21, businessInterfaceType);
   }

   private Object createProxy(SpecificationInterfaceType type, String businessInterfaceType)
   {
      StatelessLocalProxyInvocationHandler proxy = new StatelessLocalProxyInvocationHandler(this.getContainer(),
            businessInterfaceType);
      return type.equals(SpecificationInterfaceType.EJB30_BUSINESS) ? this.constructProxyBusiness(proxy) : this
            .constructEjb21Proxy(proxy);
   }

   @Override
   protected StatelessHandleRemoteImpl createHandle()
   {
      // Local beans have no Handle
      //TODO Rework the contract such that this method does not need to be
      // defined for local proxy factories
      return null;
   }
   
   @Override
   protected String getJndiName()
   {
      SessionSpecContainer container = this.getContainer();
      JBossSessionBeanMetaData md = container.getMetaData();
      String jndiName = md.determineLocalJndiName();
      return jndiName;
   }
}
