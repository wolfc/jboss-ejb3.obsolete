/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.metadata.impl.javaee;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * ResourceEnvRefMetadataImpl
 * 
 * Represents the metadata for a resource-env-ref
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ResourceEnvRefMetadataImpl implements ResourceEnvRefMetaData
{
   /**
    * The {@link ResourceEnvironmentReferenceMetaData} from which this {@link ResourceEnvRefMetadataImpl}
    * was constructed
    */
   private ResourceEnvironmentReferenceMetaData delegate;

   /**
    * Injection targets
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * mapped-name
    */
   private String mappedName;

   /**
    * Name of the resource-env-ref
    */
   private String resEnvRefName;

   /**
    * Fully qualified classname of the resource-env-ref type
    */
   private String resEnvRefType;

   /**
    * Constructs a {@link ResourceEnvRefMetadataImpl} from a {@link ResourceEnvironmentReferenceMetaData}
    * 
    * @param resEnvRef
    * @throws NullPointerException If the passed <code>resEnvRef</code> is null
    */
   public ResourceEnvRefMetadataImpl(ResourceEnvironmentReferenceMetaData resEnvRef)
   {
      this.initialize(resEnvRef);
   }

   /**
    * Initializes this {@link ResourceEnvRefMetadataImpl} from the state in <code>resEnvRef</code>
    * 
    * @param resEnvRef
    * @throws NullPointerException If the passed <code>resEnvRef</code> is null
    */
   private void initialize(ResourceEnvironmentReferenceMetaData resEnvRef)
   {
      // set the delegate
      this.delegate = resEnvRef;

      this.mappedName = this.delegate.getMappedName();
      this.resEnvRefName = this.delegate.getResourceEnvRefName();
      this.resEnvRefType = this.delegate.getType();

      // injection targets
      Set<ResourceInjectionTargetMetaData> injectionTargetsMD = this.delegate.getInjectionTargets();
      if (injectionTargetsMD != null)
      {
         this.injectionTargets = new ArrayList<InjectionTargetMetaData>(injectionTargetsMD.size());
         for (ResourceInjectionTargetMetaData injectionTarget : injectionTargetsMD)
         {
            this.injectionTargets.add(new InjectionTargetMetadataImpl(injectionTarget));
         }
      }

   }

   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see ResourceEnvRefMetaData#getInjectionTargets()
    */
   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      return this.injectionTargets;
   }

   /**
    * @see ResourceEnvRefMetaData#getMappedName()
    */
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * @see ResourceEnvRefMetaData#getResourceEnvRefName()
    */
   public String getResourceEnvRefName()
   {
      return this.resEnvRefName;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData#getResourceEnvRefType()
    */
   public String getResourceEnvRefType()
   {
      return this.resEnvRefType;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData#setInjectionTargets(java.util.List)
    */
   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      this.injectionTargets = injectionTargets;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData#setMappedName(java.lang.String)
    */
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;

   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData#setResourceEnvRefName(java.lang.String)
    */
   public void setResourceEnvRefName(String resEnvRefName)
   {
      this.resEnvRefName = resEnvRefName;

   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData#setResourceEnvRefType(java.lang.String)
    */
   public void setResourceEnvRefType(String resourceEnvRefType)
   {
      this.resEnvRefType = resourceEnvRefType;

   }

}
