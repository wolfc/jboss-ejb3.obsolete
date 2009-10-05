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
import org.jboss.ejb3.metadata.spi.javaee.PersistenceUnitRefMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * PersistenceUnitRefMetadataImpl
 * 
 * Represents the metadata for a persistence-unit-ref
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PersistenceUnitRefMetadataImpl extends IdMetadataImpl implements PersistenceUnitRefMetaData
{

   /**
    * {@link PersistenceUnitReferenceMetaData} from which this {@link PersistenceUnitRefMetadataImpl}
    * was constructed
    */
   private PersistenceUnitReferenceMetaData delegate;

   /**
    * Injection targets
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * mapped-name
    */
   private String mappedName;

   /**
    * Name of the persistence unit
    */
   private String persistenceUnitName;

   /**
    * Name of the persistence-unit-ref
    */
   private String persistenceUnitRefName;

   /**
    * Constructs a {@link PersistenceUnitRefMetadataImpl} from a {@link PersistenceUnitReferenceMetaData}
    * 
    * @param persistenceUnitRef
    * @throws NullPointerException If the passed <code>persistenceUnitRef</code> is null
    */
   public PersistenceUnitRefMetadataImpl(PersistenceUnitReferenceMetaData persistenceUnitRef)
   {
      super(persistenceUnitRef.getId());
      this.initialize(persistenceUnitRef);
   }

   /**
    * Initializes this {@link PersistenceUnitRefMetadataImpl} from the state in 
    * <code>persistenceUnitRef</code>
    * 
    * @param persistenceUnitRef
    * @throws NullPointerException If the passed <code>persistenceUnitRef</code> is null
    */
   private void initialize(PersistenceUnitReferenceMetaData persistenceUnitRef)
   {
      // set the delegate
      this.delegate = persistenceUnitRef;
      this.mappedName = this.delegate.getMappedName();
      this.persistenceUnitName = this.delegate.getPersistenceUnitName();
      this.persistenceUnitRefName = this.delegate.getPersistenceUnitRefName();

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
    * @see PersistenceUnitRefMetaData#getInjectionTargets()
    */
   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      return this.injectionTargets;
   }

   /**
    * @see PersistenceUnitRefMetaData#getMappedName()
    */
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * @see PersistenceUnitRefMetaData#getPersistenceUnitName()
    */
   public String getPersistenceUnitName()
   {
      return this.persistenceUnitName;
   }

   /**
    * @see PersistenceUnitRefMetaData#getPersistenceUnitRefName()
    */
   public String getPersistenceUnitRefName()
   {
      return this.persistenceUnitRefName;
   }

   /**
    * @see PersistenceUnitRefMetaData#setInjectionTargets(List)
    */
   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      this.injectionTargets = injectionTargets;
   }

   /**
    * @see PersistenceUnitRefMetaData#setMappedName(String)
    */
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;
   }

   /**
    * @see PersistenceUnitRefMetaData#setPersistenceUnitName(String)
    */
   public void setPersistenceUnitName(String persistenceUnitName)
   {
      this.persistenceUnitName = persistenceUnitName;
   }

   /**
    * @see PersistenceUnitRefMetaData#setPersistenceUnitRefName(String)
    */
   public void setPersistenceUnitRefName(String persistenceUnitRefName)
   {
      this.persistenceUnitRefName = persistenceUnitRefName;
   }

}
