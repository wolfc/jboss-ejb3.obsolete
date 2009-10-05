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
import org.jboss.ejb3.metadata.spi.javaee.EjbRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.EjbRefType;
import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceType;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * EjbRefMetadataImpl
 * 
 * Represents the metadata for ejb-ref
 * 
 * @see EjbRefMetaData
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EjbRefMetadataImpl extends IdMetadataImpl implements EjbRefMetaData
{

   /**
    * Delegate from which this {@link EjbRefMetadataImpl} was constructed
    */
   private EJBReferenceMetaData delegate;

   /**
    * ejb-link
    */
   private String ejbLink;

   /**
    * The ejb-ref-name
    */
   private String ejbRefName;

   /**
    * ejb-ref-type
    */
   private EjbRefType ejbRefType;

   /**
    * Fully qualified classname of the home interface
    */
   private String home;

   /**
    * injection targets
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * mapped-name specified in this ejb-ref
    */
   private String mappedName;

   /**
    * Fully qualified classname of the remote interface
    */
   private String remote;

   /**
    * Constructs an {@link EjbRefMetadataImpl} from an {@link EJBReferenceMetaData}
    * 
    * @param ejbRef
    */
   public EjbRefMetadataImpl(EJBReferenceMetaData ejbRef)
   {
      super(ejbRef.getId());
      initialize(ejbRef);
   }

   /**
    * Initializes this {@link EjbRefMetadataImpl} from the state in {@link EJBReferenceMetaData}
    * 
    * @param ejbRef The {@link EJBReferenceMetaData} whose state will be used to initialize this
    * {@link EjbRefMetadataImpl}
    */
   private void initialize(EJBReferenceMetaData ejbRef)
   {
      // set the delegate
      this.delegate = ejbRef;

      this.ejbLink = this.delegate.getLink();
      this.ejbRefName = this.delegate.getEjbRefName();
      this.home = this.delegate.getHome();
      this.mappedName = this.delegate.getMappedName();
      this.remote = this.delegate.getRemote();

      // ejb-ref-type
      EJBReferenceType delegateEjbRefType = this.delegate.getEjbRefType();
      if (delegateEjbRefType != null)
      {
         this.ejbRefType = delegateEjbRefType == EJBReferenceType.Session ? EjbRefType.SESSION : EjbRefType.ENTITY;
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
   public String getHome()
   {
      return this.home;
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
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * {@inheritDoc}
    */
   public String getRemote()
   {
      return this.remote;
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
   public void setHome(String home)
   {
      this.home = home;

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
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;

   }

   /**
    * {@inheritDoc}
    */
   public void setRemote(String remote)
   {
      this.remote = remote;

   }

}
