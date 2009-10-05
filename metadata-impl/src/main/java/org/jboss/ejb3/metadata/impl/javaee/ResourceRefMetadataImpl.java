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
import org.jboss.ejb3.metadata.spi.javaee.ResourceAuthenticationType;
import org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ResourceSharingScopeType;
import org.jboss.metadata.javaee.spec.ResourceAuthorityType;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;

/**
 * ResourceRefMetadataImpl
 * 
 * Represents metadata for resource-ref
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ResourceRefMetadataImpl extends IdMetadataImpl implements ResourceRefMetaData
{

   /**
    * The {@link ResourceReferenceMetaData} from which this {@link ResourceRefMetadataImpl}
    * was constructed 
    */
   private ResourceReferenceMetaData delegate;

   /**
    * Injection targets
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * mapped-name
    */
   private String mappedName;

   /**
    * Resource authentication type
    */
   private ResourceAuthenticationType resAuthenticationType;

   /**
    * Name of this resource-ref
    */
   private String resRefName;

   /**
    * Resource sharing scope
    */
   private ResourceSharingScopeType resSharingScope;

   /**
    * Resource type
    */
   private String resType;

   /**
    * Constructs a {@link ResourceRefMetadataImpl} from a {@link ResourceRefMetaData}
    * 
    * @param resRef
    * @throws NullPointerException If the passed <code>resRef</code> is null
    */
   public ResourceRefMetadataImpl(ResourceReferenceMetaData resRef)
   {
      super(resRef.getId());
      this.initialize(resRef);
   }

   /**
    * Initializes this {@link ResourceRefMetadataImpl} from the state in <code>resRef</code>
    * 
    * @param resRef
    * @throws NullPointerException If the passed <code>resRef</code> is null
    */
   private void initialize(ResourceReferenceMetaData resRef)
   {
      // set the delegate
      this.delegate = resRef;

      this.mappedName = this.delegate.getMappedName();
      this.resRefName = this.delegate.getResourceRefName();
      this.resType = this.delegate.getType();

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

      // resource authentication type
      ResourceAuthorityType delegateResAuthType = this.delegate.getResAuth();
      if (delegateResAuthType != null)
      {
         if (delegateResAuthType == ResourceAuthorityType.Application)
         {
            this.resAuthenticationType = ResourceAuthenticationType.APPLICATION;
         }
         else if (delegateResAuthType == ResourceAuthorityType.Container)
         {
            this.resAuthenticationType = ResourceAuthenticationType.CONTAINER;
         }
      }

      // resource sharing scope
      org.jboss.metadata.javaee.spec.ResourceSharingScopeType delegateResSharingScope = this.delegate
            .getResSharingScope();
      if (delegateResSharingScope != null)
      {
         if (delegateResSharingScope == org.jboss.metadata.javaee.spec.ResourceSharingScopeType.Shareable)
         {
            this.resSharingScope = ResourceSharingScopeType.SHAREABLE;
         }
         else if (delegateResSharingScope == org.jboss.metadata.javaee.spec.ResourceSharingScopeType.Unshareable)
         {
            this.resSharingScope = ResourceSharingScopeType.UNSHAREABLE;
         }
      }
   }

   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see ResourceRefMetaData#getInjectionTargets()
    */
   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      return this.injectionTargets;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#getMappedName()
    */
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#getResAuth()
    */
   public ResourceAuthenticationType getResAuth()
   {
      return this.resAuthenticationType;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#getResRefName()
    */
   public String getResRefName()
   {
      return this.resRefName;
   }

   /**
    *
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#getResSharingScope()
    */
   public ResourceSharingScopeType getResSharingScope()
   {
      return this.resSharingScope;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#getResType()
    */
   public String getResType()
   {
      return this.resType;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#setInjectionTargets(java.util.List)
    */
   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      this.injectionTargets = injectionTargets;

   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#setMappedName(java.lang.String)
    */
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#setResAuth(org.jboss.ejb3.metadata.spi.javaee.ResourceAuthenticationType)
    */
   public void setResAuth(ResourceAuthenticationType resAuthType)
   {
      this.resAuthenticationType = resAuthType;

   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#setResRefName(java.lang.String)
    */
   public void setResRefName(String resourceRefName)
   {
      this.resRefName = resourceRefName;

   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#setResSharingScope(org.jboss.ejb3.metadata.spi.javaee.ResourceSharingScopeType)
    */
   public void setResSharingScope(ResourceSharingScopeType resourceSharingScope)
   {
      this.resSharingScope = resourceSharingScope;

   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData#setResType(java.lang.String)
    */
   public void setResType(String resourceRefType)
   {
      this.resType = resourceRefType;

   }

}
