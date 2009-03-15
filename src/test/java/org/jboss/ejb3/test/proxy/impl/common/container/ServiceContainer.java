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
package org.jboss.ejb3.test.proxy.impl.common.container;

import java.io.Serializable;
import java.util.UUID;

import org.jboss.ejb3.proxy.impl.objectstore.ObjectStoreBindings;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * A simple @Service container that binds proxies and can be invoked.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ServiceContainer extends SessionSpecContainer implements InvokableContext
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ServiceContainer.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Singleton instance
    */
   //GuardedBy=this
   private volatile Object beanInstance;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public ServiceContainer(JBossSessionBeanMetaData metaData, ClassLoader classLoader) throws ClassNotFoundException
   {
      super(metaData, classLoader);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Creates a unique name for this container
    * 
    * @return
    */
   @Override
   protected final String createContainerName()
   {
      return ObjectStoreBindings.OBJECTSTORE_NAMESPACE_EJBCONTAINER_SERVICE + this.getMetaData().getEjbName() + "/"
            + UUID.randomUUID();
   }

   /**
    * Returns the name under which the JNDI Registrar for this container is bound
    * 
    * @return
    */
   @Override
   protected String getJndiRegistrarBindName()
   {
      return ObjectStoreBindings.OBJECTSTORE_BEAN_NAME_JNDI_REGISTRAR_SERVICE;
   }

   /**
    * Obtains the appropriate bean instance for invocation.
    * Specified Session ID will be ignored
    * 
    * @param sessionId
    * @return
    */
   @Override
   //FIXME: @Service has no Session ID
   protected Object getBeanInstance(Serializable sessionId)
   {
      // Check if bean instance is not yet created
      if (this.beanInstance == null)
      {
         try
         {
            // Double-check
            synchronized (this)
            {
               if (this.beanInstance == null)
               {
                  // Create and set the instance
                  beanInstance = this.createInstance();
                  this.setBeanInstance(beanInstance);
                  log.info("Set bean (Singleton) instance: " + beanInstance);
               }
            }
         }
         catch (Throwable t)
         {
            throw new RuntimeException("Error in creating new @Service Bean Instance", t);
         }
      }

      // Return
      return this.beanInstance;
   }

   /**
    * Requests of the container that the underlying target be removed.
    * Most frequently used in SFSB, but not necessarily supported 
    * by SLSB/Singleton/@Service Containers
    * 
    * @throws UnsupportedOperationException If the bean type 
    * does not honor client requests to remove the target
    * 
    * @param target
    * @throws UnsupportedOperationException
    */
   public void removeTarget(Object target) throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("@Service");
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected Object getBeanInstance()
   {
      return beanInstance;
   }

   protected void setBeanInstance(Object beanInstance)
   {
      this.beanInstance = beanInstance;
   }

}
