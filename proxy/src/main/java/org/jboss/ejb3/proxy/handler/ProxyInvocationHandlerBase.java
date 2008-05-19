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
package org.jboss.ejb3.proxy.handler;

import org.jboss.ejb3.proxy.lang.SerializableMethod;

/**
 * ProxyInvocationHandlerBase
 * 
 * Abstract base from which all Proxy InvocationHandlers
 * may extend
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class ProxyInvocationHandlerBase implements ProxyInvocationHandler
{
   // ------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /*
    * Method Names
    */
   private static final String METHOD_NAME_TO_STRING = "toString";

   private static final String METHOD_NAME_EQUALS = "equals";

   private static final String METHOD_NAME_HASH_CODE = "hashCode";

   /*
    * Local Methods
    */
   private static final SerializableMethod METHOD_TO_STRING;

   private static final SerializableMethod METHOD_EQUALS;

   private static final SerializableMethod METHOD_HASH_CODE;

   static
   {
      try
      {
         METHOD_TO_STRING = new SerializableMethod(Object.class
               .getDeclaredMethod(ProxyInvocationHandlerBase.METHOD_NAME_TO_STRING));
         METHOD_EQUALS = new SerializableMethod(Object.class.getDeclaredMethod(
               ProxyInvocationHandlerBase.METHOD_NAME_EQUALS, Object.class));
         METHOD_HASH_CODE = new SerializableMethod(Object.class
               .getDeclaredMethod(ProxyInvocationHandlerBase.METHOD_NAME_HASH_CODE));
      }
      catch (NoSuchMethodException nsme)
      {
         throw new RuntimeException(
               "Methods for handling directly by the InvocationHandler were not initialized correctly", nsme);
      }

   }

   // ------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * The invoked method
    */
   private SerializableMethod invokedMethod;

   /**
    * The name under which the target container is registered
    */
   private String containerName;

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param containerName The name under which the target container is registered 
    */
   protected ProxyInvocationHandlerBase(String containerName)
   {
      this.setContainerName(containerName);
   }

   // ------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   /**
    * Handles the current invocation directly in this invocation handler.  Only 
    * a subset of method invocations are eligible for this treatment, else 
    * a NotEligibleForDirectInvocationException will be thrown
    * 
    * @param proxy
    * @param args Arguments of the current invocation
    * @return
    * @throws NotEligibleForDirectInvocationException
    */
   protected Object handleInvocationDirectly(Object proxy, Object[] args)
         throws NotEligibleForDirectInvocationException
   {

      // Obtain the invoked method
      SerializableMethod invokedMethod = this.getInvokedMethod();
      assert invokedMethod != null : "Invoked Method was not set upon invocation of " + this.getClass().getName();

      // equals
      if (invokedMethod.equals(ProxyInvocationHandlerBase.METHOD_EQUALS))
      {
         return new Boolean(this.equals(args[0]));
      }
      // toString
      if (invokedMethod.equals(ProxyInvocationHandlerBase.METHOD_TO_STRING))
      {
         return this.toString();
      }
      // hashCode
      if (invokedMethod.equals(ProxyInvocationHandlerBase.METHOD_HASH_CODE))
      {
         return new Integer(this.hashCode());
      }

      // If no eligible methods were invoked
      throw new NotEligibleForDirectInvocationException("Current invocation \"" + this.getInvokedMethod()
            + "\" is not eligible for direct handling by " + this);
   }

   // ------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public SerializableMethod getInvokedMethod()
   {
      return invokedMethod;
   }

   protected void setInvokedMethod(SerializableMethod invokedMethod)
   {
      this.invokedMethod = invokedMethod;
   }

   public String getContainerName()
   {
      return containerName;
   }

   public void setContainerName(String containerName)
   {
      this.containerName = containerName;
   }
}
