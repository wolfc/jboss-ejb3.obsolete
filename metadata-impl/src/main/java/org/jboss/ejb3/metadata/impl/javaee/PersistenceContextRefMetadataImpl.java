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

import javax.persistence.PersistenceContextType;

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.ejb3.metadata.spi.javaee.PersistenceContextRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.PropertyMetaData;
import org.jboss.metadata.javaee.spec.PersistenceContextReferenceMetaData;
import org.jboss.metadata.javaee.spec.PropertiesMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * PersistenceContextRefMetadataImpl
 * 
 * Represents the metadata for a persistence-context-ref
 *  
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PersistenceContextRefMetadataImpl extends IdMetadataImpl implements PersistenceContextRefMetaData
{

   /**
    * The {@link PersistenceContextReferenceMetaData} from which this {@link PersistenceContextRefMetadataImpl}
    * was constructed
    */
   private PersistenceContextReferenceMetaData delegate;

   /**
    * Injection targets
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * mapped-name
    */
   private String mappedName;

   /**
    * Name of the persistence-context-ref
    */
   private String persistenceContextRefName;

   /**
    * Type of persistence context
    */
   private PersistenceContextType persistenceContextType;

   /**
    * Properties associated with this persistence-context ref
    */
   private List<PropertyMetaData> persistenceProperties;

   /**
    * Name of the persistence unit
    */
   private String persistenceUnitName;

   /**
    * Constructs a {@link PersistenceContextRefMetadataImpl} from a {@link PersistenceContextReferenceMetaData}
    * 
    * @param persistenceContextRef
    */
   public PersistenceContextRefMetadataImpl(PersistenceContextReferenceMetaData persistenceContextRef)
   {
      super(persistenceContextRef.getId());
      initialize(persistenceContextRef);

   }

   /**
    * Initializes this {@link PersistenceContextRefMetadataImpl} from the state in <code>persistenceContextRef</code>
    * 
    * @param persistenceContextRef
    */
   private void initialize(PersistenceContextReferenceMetaData persistenceContextRef)
   {
      // set the delegate
      this.delegate = persistenceContextRef;

      this.mappedName = this.delegate.getMappedName();
      this.persistenceContextRefName = this.delegate.getPersistenceContextRefName();
      this.persistenceContextType = this.delegate.getPersistenceContextType();
      this.persistenceUnitName = this.delegate.getPersistenceUnitName();

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

      // persistence properties
      PropertiesMetaData delegatePersistenceProperties = this.delegate.getProperties();
      if (delegatePersistenceProperties != null)
      {
         this.persistenceProperties = new ArrayList<PropertyMetaData>(delegatePersistenceProperties.size());
         for (org.jboss.metadata.javaee.spec.PropertyMetaData property : delegatePersistenceProperties)
         {
            this.persistenceProperties.add(new PropertyMetadataImpl(property));
         }
      }
   }

   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see PersistenceContextRefMetaData#getInjectionTargets()
    */
   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      return this.injectionTargets;
   }

   /**
    * @see PersistenceContextRefMetaData#getMappedName()
    */
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * @see PersistenceContextRefMetaData#getPersistenceContextRefName()
    */
   public String getPersistenceContextRefName()
   {
      return this.persistenceContextRefName;
   }

   /**
    * @see PersistenceContextRefMetaData#getPersistenceContextType()
    */
   public PersistenceContextType getPersistenceContextType()
   {
      return this.persistenceContextType;
   }

   /**
    * @see PersistenceContextRefMetaData#getPersistenceProperties()
    */
   public List<PropertyMetaData> getPersistenceProperties()
   {
      return this.persistenceProperties;
   }

   /**
    * @see PersistenceContextRefMetaData#getPersistenceUnitName()
    */
   public String getPersistenceUnitName()
   {
      return this.persistenceUnitName;
   }

   /**
    * @see PersistenceContextRefMetaData#setInjectionTargets(List)
    */
   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      this.injectionTargets = injectionTargets;

   }

   /**
    * @see PersistenceContextRefMetaData#setMappedName(String)
    */
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;
   }

   /**
    * @see PersistenceContextRefMetaData#setPersistenceContextRefName(String)
    */
   public void setPersistenceContextRefName(String persistenceContextRefName)
   {
      this.persistenceContextRefName = persistenceContextRefName;
   }

   /**
    * @see PersistenceContextRefMetaData#setPersistenceContextType(PersistenceContextType)
    */
   public void setPersistenceContextType(PersistenceContextType persistenceContextType)
   {
      this.persistenceContextType = persistenceContextType;

   }

   /**
    * @see PersistenceContextRefMetaData#setPersistenceProperties(List)
    */
   public void setPersistenceProperties(List<PropertyMetaData> properties)
   {
      this.persistenceProperties = properties;
   }

   /**
    * @see PersistenceContextRefMetaData#setPersistenceUnitName(String)
    */
   public void setPersistenceUnitName(String value)
   {
      this.persistenceUnitName = value;
   }

}
