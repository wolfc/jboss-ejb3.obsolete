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
package org.jboss.ejb3.resource.adaptor.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.transaction.xa.XAResource;

import org.jboss.ejb3.resource.adaptor.socket.inflow.SocketActivationSpec;

/**
 * A Socket Server Resource Adaptor
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SocketResourceAdaptor implements ResourceAdapter, Work
{
   // Class Members

   /*
    * Server to handle Socket Requests
    */
   private static ResourceAdaptorNonBlockingSocketServer server;

   // Instance Members
   /*
    * Bootstrap Context
    */
   private BootstrapContext ctx;

   /*
    * ActivationSpec / MessageEndpoint Factories 
    */
   private ConcurrentMap<ActivationSpec, MessageEndpointFactory> messageEndpointFactories = new ConcurrentHashMap<ActivationSpec, MessageEndpointFactory>();

   // Accessors / Mutators
   public BootstrapContext getCtx()
   {
      return ctx;
   }

   public void setCtx(BootstrapContext ctx)
   {
      this.ctx = ctx;
   }

   public Map<ActivationSpec, MessageEndpointFactory> getMessageEndpointFactories()
   {
      return messageEndpointFactories;
   }

   // ResourceAdaptor Required Implementations

   public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec)
         throws ResourceException
   {
      // Initialize
      Class<?> activationSpecType = SocketActivationSpec.class;

      // Ensure assignable
      if (!(activationSpecType.isAssignableFrom(activationSpec.getClass())))
      {
         throw new ResourceException("Supplied ActivationSpec must be of type " + activationSpecType.getName()
               + "; is instead of type " + activationSpec.getClass().getName());
      }

      // Place ActivationSpec/Factory
      this.getMessageEndpointFactories().put(activationSpec, messageEndpointFactory);
   }

   public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec)
   {
      // Remove ActivationSpec/Factory
      this.getMessageEndpointFactories().remove(activationSpec);
   }

   public XAResource[] getXAResources(ActivationSpec[] activationSpec) throws ResourceException
   {
      // Null
      return null;
   }

   public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException
   {
      // Set Context
      this.setCtx(bootstrapContext);

      // If the Server has not yet been created
      if (SocketResourceAdaptor.server == null)
      {
         // Create new Server
         SocketResourceAdaptor.server = new ResourceAdaptorNonBlockingSocketServer(this);

         // Start the Server in own Thread
         try
         {
            this.getCtx().getWorkManager().startWork(this);
         }
         catch (WorkException e)
         {
            throw new ResourceAdapterInternalException(e);
         }
      }
   }

   public void stop()
   {
      // Delegate to Work's "release"
      this.release();
   }

   // Work Required Implementations

   public void run()
   {
      // Start the Server
      SocketResourceAdaptor.server.start();
   }

   public void release()
   {
      // Shutdown the Server
      SocketResourceAdaptor.server.shutdown();
   }
}