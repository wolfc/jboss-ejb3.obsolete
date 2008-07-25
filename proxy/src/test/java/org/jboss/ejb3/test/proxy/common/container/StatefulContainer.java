/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.common.container;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jboss.ejb3.interceptors.container.StatefulSessionContainerMethodInvocation;
import org.jboss.ejb3.proxy.container.StatefulSessionInvokableContext;
import org.jboss.ejb3.proxy.handler.session.stateful.StatefulProxyInvocationHandlerBase;
import org.jboss.ejb3.proxy.objectstore.ObjectStoreBindings;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * StatefulContainer
 * 
 * A Mock SFSB Container for use in Testing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulContainer extends SessionSpecContainer
      implements
         StatefulSessionInvokableContext<StatefulSessionContainerMethodInvocation>
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Cache of SFSB instances in key = sessionId and value = instance
    */
   private Map<Object, Object> cache;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public StatefulContainer(JBossSessionBeanMetaData metaData, ClassLoader classLoader) throws ClassNotFoundException
   {
      // Call super
      super(metaData, classLoader);

      // Instanciate Cache
      this.setCache(new HashMap<Object, Object>());

   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a new user session and returns the unique Session ID
    * 
    * @return
    */
   public Object createSession()
   {
      // Create a new Session ID
      Object sessionId = UUID.randomUUID();

      // Create a new Instance
      Object instance = null;
      try
      {
         instance = this.createInstance();
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error in creating new instance of " + this.getBeanClass(), t);
      }

      // Place in cache
      this.getCache().put(sessionId, instance);

      // Return
      return sessionId;
   }

   /**
    * Creates a unique name for this container
    * 
    * @return
    */
   protected final String createContainerName()
   {
      return ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJBCONTAINER_STATEFUL + this.getMetaData().getEjbName() + "/"
            + UUID.randomUUID();
   }

   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   protected String getJndiRegistrarBindName()
   {
      return ObjectStoreBindings.OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SFSB;
   }

   /**
    * Obtains the appropriate bean instance for invocation
    * as called from the specified proxy
    * 
    * @param proxy
    * @return
    */
   protected Object getBeanInstance(Object proxy)
   {
      // Obtain the InvocationHandler
      InvocationHandler handler = Proxy.getInvocationHandler(proxy);
      assert handler instanceof StatefulProxyInvocationHandlerBase : "SFSB Proxy must be of type "
            + StatefulProxyInvocationHandlerBase.class.getName();
      StatefulProxyInvocationHandlerBase sHandler = (StatefulProxyInvocationHandlerBase) handler;

      // Get the Session ID
      Object sessionId = sHandler.getSessionId();
      assert sessionId != null : "Proxy has no Session ID set, and this is required for SFSB Invocation";

      // Get the corresponding instance from the cache
      Object bean = this.getCache().get(sessionId);
      assert bean != null : "SFSB Proxy claims Session ID of " + sessionId
            + ", but no corresponding bean instance could be found";

      // Return
      return bean;
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected Map<Object, Object> getCache()
   {
      return cache;
   }

   protected void setCache(Map<Object, Object> cache)
   {
      this.cache = cache;
   }

}
