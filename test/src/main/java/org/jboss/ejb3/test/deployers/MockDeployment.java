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
package org.jboss.ejb3.test.deployers;

import java.util.Set;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.Attachments;

/**
 * MockDeployment
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockDeployment implements Deployment
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   private static final String MSG_UNSUPPORTED = "This is a mock deployment only";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private String name;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Sole Constructor
    */
   public MockDeployment(String name)
   {
      this.setName(name);
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.Deployment#getName()
    */
   public String getName()
   {
      return this.name;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.Deployment#getSimpleName()
    */
   public String getSimpleName()
   {
      return this.getName();
   }

   // --------------------------------------------------------------------------------->>>>>>
   // --------------------------------------------------------------------------------->>>>>>
   // --------------------------------------------------------------------------------->>>>>>

   /*
    * Everything below this marker is not supported
    */

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.Deployment#getTypes()
    */
   public Set<String> getTypes()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.client.spi.Deployment#setTypes(java.util.Set)
    */
   public void setTypes(Set<String> types)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments#getPredeterminedManagedObjects()
    */
   public Attachments getPredeterminedManagedObjects()
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments#setPredeterminedManagedObjects(org.jboss.deployers.spi.attachments.Attachments)
    */
   public void setPredeterminedManagedObjects(Attachments predetermined)
   {
      throw new UnsupportedOperationException(MSG_UNSUPPORTED);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------|| 

   protected void setName(String name)
   {
      this.name = name;
   }

}
