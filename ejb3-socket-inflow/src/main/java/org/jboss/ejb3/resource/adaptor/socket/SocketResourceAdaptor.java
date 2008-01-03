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

import org.jboss.ejb3.resource.adaptor.socket.handler.http.CopyHttpRequestToResponseRequestHandler;
import org.jboss.ejb3.resource.adaptor.socket.inflow.SocketActivationSpec;

public class SocketResourceAdaptor implements ResourceAdapter, Work
{
   // Instance Members
   /*
    * Bootstrap Context
    */
   private BootstrapContext ctx;

   /*
    * Server to handle AJAX Requests
    */
   private NonBlockingSocketServer server;

   /*
    * ActivationSpec / MessageEndpoint Factories 
    */
   private ConcurrentMap<ActivationSpec, MessageEndpointFactory> activationSpecFactories = new ConcurrentHashMap<ActivationSpec, MessageEndpointFactory>();

   // Accessors / Mutators
   public BootstrapContext getCtx()
   {
      return ctx;
   }

   public void setCtx(BootstrapContext ctx)
   {
      this.ctx = ctx;
   }

   private NonBlockingSocketServer getServer()
   {
      return server;
   }

   private void setServer(NonBlockingSocketServer server)
   {
      this.server = server;
   }

   private Map<ActivationSpec, MessageEndpointFactory> getActivationSpecFactories()
   {
      return activationSpecFactories;
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
      // Place Activation Spec into factories
      this.getActivationSpecFactories().put(activationSpec, messageEndpointFactory);
   }

   public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec)
   {
      // Remove ActivationSpec from factories
      this.getActivationSpecFactories().remove(activationSpec);
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

      // Create new Server
      try
      {
         //TODO Handler from ActivationConfig properties
         this.setServer(new NonBlockingSocketServer(CopyHttpRequestToResponseRequestHandler.class.newInstance()));
      }
      catch (InstantiationException e)
      {
         throw new ResourceAdapterInternalException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new ResourceAdapterInternalException(e);
      }

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

   public void stop()
   {
      // Delegate to Work's "release"
      this.release();
   }

   // Work Required Implementations

   public void run()
   {
      // Start the Server
      this.getServer().start();
   }

   public void release()
   {
      // Shutdown the Server
      this.getServer().shutdown();
   }
}