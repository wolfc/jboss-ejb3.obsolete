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
import org.jboss.ejb3.metadata.spi.javaee.EnvEntryMetaData;
import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * EnvEntryMetadataImpl
 * 
 * Represents the metadata for an env-entry
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EnvEntryMetadataImpl extends IdMetadataImpl implements EnvEntryMetaData
{

   /**
    * {@link EnvironmentEntryMetaData} from which this {@link EnvEntryMetadataImpl}
    * was constructed
    */
   private EnvironmentEntryMetaData delegate;

   /**
    * The env-entry-name
    */
   private String envEntryName;

   /**
    * env-entry-type which is a fully qualified classname
    */
   private String envEntryType;

   /**
    * env-entry-value
    */
   private String envEtnryValue;

   /**
    * Injection targets of this env-entry
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * mapped-name
    */
   private String mappedName;

   /**
    * Constructs an {@link EnvEntryMetadataImpl} from an {@link EnvironmentEntryMetaData}
    * 
    * @param envEntry
    */
   public EnvEntryMetadataImpl(EnvironmentEntryMetaData envEntry)
   {
      super(envEntry.getId());
      initialize(envEntry);
   }

   /**
    * Initializes this {@link EnvEntryMetadataImpl} from the state available in 
    * the <code>envEntry</code>
    * 
    * @param envEntry
    */
   private void initialize(EnvironmentEntryMetaData envEntry)
   {
      // set the delegate
      this.delegate = envEntry;

      this.envEntryName = this.delegate.getEnvEntryName();
      this.envEntryType = this.delegate.getType();
      this.envEtnryValue = this.delegate.getValue();
      this.mappedName = this.delegate.getMappedName();

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
    * {@inheritDoc}
    * 
    */
   public String getEnvEntryName()
   {
      return this.envEntryName;
   }

   /**
    * {@inheritDoc}
    */
   public String getEnvEntryType()
   {
      return this.envEntryType;
   }

   /**
    * @see EnvEntryMetaData#getEnvEntryValue()
    */
   public String getEnvEntryValue()
   {
      return this.envEtnryValue;
   }

   /**
    * @see EnvEntryMetaData#getInjectionTargets()
    */
   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      return this.injectionTargets;
   }

   /**
    * @see EnvEntryMetaData#getMappedName()
    */
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * @see EnvEntryMetaData#setEnvEntryName(String)
    */
   public void setEnvEntryName(String name)
   {
      this.envEntryName = name;

   }

   /**
    * @see EnvEntryMetaData#setEnvEntryType(String)
    */
   public void setEnvEntryType(String envEntryType)
   {
      this.envEntryType = envEntryType;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.EnvEntryMetaData#setEnvEntryValue(java.lang.String)
    */
   public void setEnvEntryValue(String value)
   {
      this.envEtnryValue = value;

   }

   /**
    * @see EnvEntryMetaData#setInjectionTargets(List)
    */
   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      this.injectionTargets = injectionTargets;
   }

   /**
    * @see EnvEntryMetaData#setMappedName(String)
    */
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;
   }

}
