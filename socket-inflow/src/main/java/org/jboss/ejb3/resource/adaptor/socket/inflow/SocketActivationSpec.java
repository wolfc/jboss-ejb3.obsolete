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
package org.jboss.ejb3.resource.adaptor.socket.inflow;

import java.io.Serializable;
import java.net.InetSocketAddress;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.jboss.ejb3.resource.adaptor.socket.SocketResourceAdaptor;

/**
 * Activation Specification for Socket-based Inflow
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SocketActivationSpec implements ActivationSpec, Serializable
{
   // Class Members

   /*
    * Serial Version UID
    */
   private static final long serialVersionUID = 5427084615487469213L;

   // Instance Members

   /*
    * ResourceAdaptor
    */
   private SocketResourceAdaptor resourceAdaptor;

   /*
    * Address upon which to bind Server to listen for incoming Socket Requests
    */
   private String host;

   /*
    * Port upon which to bind Server to listen for incoming Socket Requests
    */
   private String port;

   // ActivationSpec Required Implementations
   public void validate() throws InvalidPropertyException
   {
      // Ensure Bind Address is Specified
      if (this.getHost() == null || this.getHost().equals(""))
      {
         throw new InvalidPropertyException("ActivationSpec Property 'host' is required");
      }
      // Ensure Port is specified
      if (Integer.parseInt(this.getPort()) == 0)
      {
         throw new InvalidPropertyException("ActivationSpec Property 'port' is required");
      }

      // Create an InetSocketAddress to ensure binding is possible to properties specified
      InetSocketAddress address = new InetSocketAddress(this.getHost(), Integer.parseInt(this.getPort()));
      // Ensure binding succeeds
      if (address.isUnresolved())
      {
         throw new InvalidPropertyException("Cannot bind to " + address.toString());
      }
   }

   public ResourceAdapter getResourceAdapter()
   {
      return this.resourceAdaptor;
   }

   public void setResourceAdapter(ResourceAdapter resourceAdaptor) throws ResourceException
   {
      this.resourceAdaptor = (SocketResourceAdaptor) resourceAdaptor;
   }

   // Accessors / Mutators

   protected SocketResourceAdaptor getResourceAdaptor()
   {
      return resourceAdaptor;
   }

   protected void setResourceAdaptor(SocketResourceAdaptor resourceAdaptor)
   {
      this.resourceAdaptor = resourceAdaptor;
   }

   public String getHost()
   {
      return host;
   }

   public void setHost(String host)
   {
      this.host = host;
   }

   public String getPort()
   {
      return port;
   }

   public void setPort(String port)
   {
      this.port = port;
   }

}