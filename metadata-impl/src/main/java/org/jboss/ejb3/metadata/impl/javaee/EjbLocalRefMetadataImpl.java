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
import org.jboss.ejb3.metadata.spi.javaee.EjbLocalRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.EjbRefType;
import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceType;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * EjbLocalRefMetadataImpl
 * 
 * Represents the metadata for a ejb-local-ref
 * 
 * @see EjbLocalRefMetaData
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EjbLocalRefMetadataImpl extends IdMetadataImpl implements EjbLocalRefMetaData
{

   /**
    * Delegate from which this {@link EjbLocalRefMetadataImpl} was constructed
    */
   private EJBLocalReferenceMetaData delegate;

   /**
    * ejb-link
    */
   private String ejbLink;

   /**
    * The ejb-ref name
    */
   private String ejbRefName;

   /**
    * The ejb-ref type
    */
   private EjbRefType ejbRefType;

   /**
    * Injection targets
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * Fully qualified classname of the local interface
    */
   private String local;

   /**
    * Fully qualified classname of the localhome interface
    */
   private String localHome;

   /**
    * Mapped name
    */
   private String mappedName;

   /**
    * Constructs an {@link EjbLocalRefMetadataImpl} from an {@link EJBLocalReferenceMetaData}
    * 
    * @param ejbLocalRef
    */
   public EjbLocalRefMetadataImpl(EJBLocalReferenceMetaData ejbLocalRef)
   {
      super(ejbLocalRef.getId());
      initialize(ejbLocalRef);
   }

   /**
    * Initializes this {@link EjbLocalRefMetadataImpl} from the state in {@link EJBLocalReferenceMetaData}
    * 
    * @param ejblocalRef
    */
   private void initialize(EJBLocalReferenceMetaData ejblocalRef)
   {
      // set the delegate
      this.delegate = ejblocalRef;

      this.ejbLink = this.delegate.getLink();
      this.ejbRefName = this.delegate.getEjbRefName();
      this.local = this.delegate.getLocal();
      this.localHome = this.delegate.getLocalHome();
      this.mappedName = this.delegate.getMappedName();

      // set the ejb-ref-type
      EJBReferenceType delegateEjbRefType = this.delegate.getEjbRefType();
      if (delegateEjbRefType != null)
      {
         this.ejbRefType = delegateEjbRefType == EJBReferenceType.Entity ? EjbRefType.ENTITY : EjbRefType.SESSION;
      }

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
    */
   public String getEjbLink()
   {
      return this.ejbLink;
   }

   /**
    * {@inheritDoc}
    */
   public String getEjbRefName()
   {
      return this.ejbRefName;
   }

   /**
    * {@inheritDoc}
    */
   public EjbRefType getEjbRefType()
   {
      return this.ejbRefType;
   }

   /**
    * {@inheritDoc}
    */
   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      return this.injectionTargets;
   }

   /**
    * {@inheritDoc}
    */
   public String getLocal()
   {
      return this.local;
   }

   /**
    * {@inheritDoc}
    */
   public String getLocalHome()
   {
      return this.localHome;
   }

   /**
    * {@inheritDoc}
    */
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * {@inheritDoc}
    */
   public void setEjbLink(String ejbLink)
   {
      this.ejbLink = ejbLink;
   }

   /**
    * {@inheritDoc}
    */
   public void setEjbRefName(String ejbRefName)
   {
      this.ejbRefName = ejbRefName;
   }

   /**
    * {@inheritDoc}
    */
   public void setEjbRefType(EjbRefType ejbRefType)
   {
      this.ejbRefType = ejbRefType;
   }

   /**
    * {@inheritDoc}
    */
   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      this.injectionTargets = injectionTargets;
   }

   /**
    * {@inheritDoc}
    */
   public void setLocal(String value)
   {
      this.local = value;

   }

   /**
    * {@inheritDoc}
    */
   public void setLocalHome(String localHome)
   {
      this.localHome = localHome;
   }

   /**
    * {@inheritDoc}
    */
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;
   }

}
