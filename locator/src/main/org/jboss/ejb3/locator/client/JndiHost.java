/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.locator.client;

import java.io.Serializable;

/**
 * Represents a JNDI Host location on which remote and local services may be
 * deployed.
 * 
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
 */
public class JndiHost implements Serializable
{
   // Class Members
   private static final long serialVersionUID = 4367726854123681529L;

   // Instance Members
   private String id;

   private String address;

   private int port;

   // Constructors
   public JndiHost()
   {
   }

   public JndiHost(String name, String address, int port)
   {
      this.setId(name);
      this.setAddress(address);
      this.setPort(port);
   }

   // Acccessors/Mutators
   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }

   // Overridden Implementations

   /**
    * Equals implementation to return true if and only if the 
    * address (non-case-specific) and ports are both equal
    * 
    * @param obj
    * @return Whether the specified object is equal to this one by value
    */
   @Override
   public boolean equals(Object obj)
   {
      // Ensure types are same
      if (!(obj instanceof JndiHost))
      {
         return false;
      }

      // Compare addresses and ports
      JndiHost comp = (JndiHost) obj;
      if (!comp.getAddress().toLowerCase().equals(this.getAddress().toLowerCase()) || comp.getPort() != this.getPort())
      {
         return false;
      }

      // Equal
      return true;
   }

   /**
    * Overridden hashCode to fit contract that two "equal" 
    * objects must always have the same hash code
    */
   @Override
   public int hashCode()
   {
      return this.getAddress().hashCode() + new Integer(this.getPort()).hashCode();
   }
}
