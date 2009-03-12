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

import java.lang.reflect.Method;
import java.util.Set;

import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.util.MethodHashing;
import org.jboss.logging.Logger;

/**
 * ProxyTestClassAdvisor
 *
 * An Advisor for SessionContainers used in the EJB3
 * Proxy Test Suite
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyTestClassAdvisor extends ClassAdvisor
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(ProxyTestClassAdvisor.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The backing SessionContainer to be advised
    */
   private SessionContainer container;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * Creates an Advisor targeted for the specified SessionContainer,
    * repopulating the method tables and rebuilding the method chains
    * to include virtual methods defined by the EJB itself.
    * 
    * @param container
    * @param manager
    */
   public ProxyTestClassAdvisor(SessionContainer container, AspectManager manager)
   {
      // Call Super implementation, advising the Bean Implementation class
      super(container.getBeanClass(), manager);

      // Set the Container
      this.setContainer(container);

      // Recreate Method Tables (advisedMethods)
      try
      {
         this.createMethodTables();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      // Reinitialize the method chain (for methodInfos)
      this.initializeMethodChain();

   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Overridden method tables definition to include
    * virtual methods declared by the Container's EJB
    */
   @Override
   protected void createMethodTables() throws Exception
   {
      // Call Super
      super.createMethodTables();

      // Obtain Virtual Methods declared by the EJB
      Set<Method> virtualMethods = getVirtualMethods();

      // If virtual methods exist
      if (virtualMethods != null)
      {
         // For each virtual method
         for (Method virtualMethod : virtualMethods)
         {
            // Calculate the hash
            long hash = MethodHashing.methodHash(virtualMethod);

            // Add to the advised methods
            this.advisedMethods.put(hash, virtualMethod);

            // Log
            log.debug("Added method with hash " + hash + " to those advised for " + this.clazz + ": " + virtualMethod);
         }
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Delegates back to the Container to obtain virtual methods
    * declared by the EJB
    */
   private Set<Method> getVirtualMethods()
   {
      return this.getContainer().getVirtualMethods();
   }

   protected SessionContainer getContainer()
   {
      return container;
   }

   private void setContainer(SessionContainer container)
   {
      this.container = container;
   }

}
