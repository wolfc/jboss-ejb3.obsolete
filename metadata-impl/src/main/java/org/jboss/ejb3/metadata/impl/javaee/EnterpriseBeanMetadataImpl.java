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

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.DisplayNameMetaData;
import org.jboss.ejb3.metadata.spi.javaee.EjbLocalRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.EjbRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.EnterpriseBeanMetaData;
import org.jboss.ejb3.metadata.spi.javaee.EnvEntryMetaData;
import org.jboss.ejb3.metadata.spi.javaee.IconType;
import org.jboss.ejb3.metadata.spi.javaee.LifecycleCallbackMetaData;
import org.jboss.ejb3.metadata.spi.javaee.PersistenceContextRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.PersistenceUnitRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ResourceRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.SecurityIdentityMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ServiceRefMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.javaee.spec.PersistenceContextReferenceMetaData;
import org.jboss.metadata.javaee.spec.PersistenceContextReferencesMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferencesMetaData;

/**
 * EnterpriseBeanMetadataImpl
 * 
 * Represents the metadata for an enterprise bean
 * 
 * @see EnterpriseBeanMetaData
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EnterpriseBeanMetadataImpl extends IdMetadataImpl implements EnterpriseBeanMetaData
{
   /**
    * Delegate from which this {@link EnterpriseBeanMetadataImpl} was 
    * constructed
    */
   private org.jboss.metadata.ejb.spec.EnterpriseBeanMetaData delegate;

   /**
    * Fully qualified ejb classname
    */
   private String ejbClass;

   /**
    * ejb-local-refs of this bean
    */
   private List<EjbLocalRefMetaData> ejbLocalRefs;

   /**
    * EJB name
    */
   private String ejbName;

   /**
    * ejb-refs of this bean
    */
   private List<EjbRefMetaData> ejbRefs;

   /**
    * env-entries of this bean
    */
   private List<EnvEntryMetaData> envEntries;

   /**
    * mapped name
    */
   private String mappedName;

   /**
    * persistence-context-refs of this bean
    */
   private List<PersistenceContextRefMetaData> persistenceContextRefs;

   /**
    * persistence-unit-refs of this bean
    */
   private List<PersistenceUnitRefMetaData> persistenceUnitRefs;

   /**
    * Postconstructs of this bean
    */
   private List<LifecycleCallbackMetaData> postConstructs;

   /**
    * PreDestroys of this bean
    */
   private List<LifecycleCallbackMetaData> preDestroys;

   /**
    * resource-env-refs of this bean
    */
   private List<ResourceEnvRefMetaData> resourceEnvRefs;

   /**
    * resource-refs of this bean
    */
   private List<ResourceRefMetaData> resourceRefs;

   /**
    * Security identity
    */
   private SecurityIdentityMetaData securityIdentity;

   /**
    * service-refs of this bean
    */
   private List<ServiceRefMetaData> serviceRefs;

   /**
    * Constructs a {@link EnterpriseBeanMetadataImpl} out of a 
    * {@link org.jboss.metadata.ejb.spec.EnterpriseBeanMetaData}
    * 
    * @param enterpriseBean
    */
   public EnterpriseBeanMetadataImpl(org.jboss.metadata.ejb.spec.EnterpriseBeanMetaData enterpriseBean)
   {
      super(enterpriseBean.getId());
      this.initialize(enterpriseBean);
   }

   /**
    * Initializes the state of this {@link EnterpriseBeanMetadataImpl} from
    * the {@link org.jboss.metadata.ejb.spec.EnterpriseBeanMetaData}
    * 
    * @param enterpriseBean
    */
   private void initialize(org.jboss.metadata.ejb.spec.EnterpriseBeanMetaData enterpriseBean)
   {
      // set the delegate
      this.delegate = enterpriseBean;

      this.ejbClass = this.delegate.getEjbClass();
      this.ejbName = this.delegate.getEjbName();
      this.mappedName = this.delegate.getMappedName();

      // initialize security identity
      org.jboss.metadata.ejb.spec.SecurityIdentityMetaData delegateSecurityIdentity = this.delegate
            .getSecurityIdentity();
      this.securityIdentity = delegateSecurityIdentity == null ? null : new SecurityIdentityMetadataImpl(
            delegateSecurityIdentity);

      // initialize ejb local refs
      EJBLocalReferencesMetaData localRefs = this.delegate.getEjbLocalReferences();
      if (localRefs != null)
      {
         this.ejbLocalRefs = new ArrayList<EjbLocalRefMetaData>(localRefs.size());
         for (EJBLocalReferenceMetaData localRef : localRefs)
         {
            this.ejbLocalRefs.add(new EjbLocalRefMetadataImpl(localRef));
         }
      }

      // initialize ejb refs
      EJBReferencesMetaData refs = this.delegate.getEjbReferences();
      if (refs != null)
      {
         this.ejbRefs = new ArrayList<EjbRefMetaData>(refs.size());
         for (EJBReferenceMetaData ref : refs)
         {
            this.ejbRefs.add(new EjbRefMetadataImpl(ref));
         }
      }

      // initialize evn entries
      EnvironmentEntriesMetaData envEntriesMD = this.delegate.getEnvironmentEntries();
      if (envEntriesMD != null)
      {
         this.envEntries = new ArrayList<EnvEntryMetaData>(envEntriesMD.size());
         for (EnvironmentEntryMetaData envEntryMD : envEntriesMD)
         {
            this.envEntries.add(new EnvEntryMetadataImpl(envEntryMD));
         }
      }

      // persistence context refs
      PersistenceContextReferencesMetaData persistenceCtxRefs = this.delegate.getPersistenceContextRefs();
      if (persistenceCtxRefs != null)
      {
         this.persistenceContextRefs = new ArrayList<PersistenceContextRefMetaData>(persistenceCtxRefs.size());
         for (PersistenceContextReferenceMetaData persistenceCtxRef : persistenceCtxRefs)
         {
            this.persistenceContextRefs.add(new PersistenceContextRefMetadataImpl(persistenceCtxRef));
         }
      }

      // persistence unit refs
      PersistenceUnitReferencesMetaData pUnitRefs = this.delegate.getPersistenceUnitRefs();
      if (pUnitRefs != null)
      {
         this.persistenceUnitRefs = new ArrayList<PersistenceUnitRefMetaData>(pUnitRefs.size());
         for (PersistenceUnitReferenceMetaData pUnitRef : pUnitRefs)
         {
            this.persistenceUnitRefs.add(new PersistenceUnitRefMetadataImpl(pUnitRef));
         }
      }

      // post constructs
      LifecycleCallbacksMetaData postConstructCallbacks = this.delegate.getPostConstructs();
      if (postConstructCallbacks != null)
      {
         this.postConstructs = new ArrayList<LifecycleCallbackMetaData>(postConstructCallbacks.size());
         for (org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData callback : postConstructCallbacks)
         {
            this.postConstructs.add(new LifecycleCallbackMetadataImpl(callback));
         }
      }

      // pre destroys
      LifecycleCallbacksMetaData preDestroyCallbacks = this.delegate.getPreDestroys();
      if (preDestroyCallbacks != null)
      {
         this.preDestroys = new ArrayList<LifecycleCallbackMetaData>(preDestroyCallbacks.size());
         for (org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData callback : preDestroyCallbacks)
         {
            this.preDestroys.add(new LifecycleCallbackMetadataImpl(callback));
         }
      }

      // resource env refs
      ResourceEnvironmentReferencesMetaData resEnvRefsMD = this.delegate.getResourceEnvironmentReferences();
      if (resEnvRefsMD != null)
      {
         this.resourceEnvRefs = new ArrayList<ResourceEnvRefMetaData>(resEnvRefsMD.size());
         for (ResourceEnvironmentReferenceMetaData resEnvRef : resEnvRefsMD)
         {
            this.resourceEnvRefs.add(new ResourceEnvRefMetadataImpl(resEnvRef));
         }
      }

      // resource refs
      ResourceReferencesMetaData resRefsMD = this.delegate.getResourceReferences();
      if (resRefsMD != null)
      {
         this.resourceRefs = new ArrayList<ResourceRefMetaData>(resRefsMD.size());
         for (ResourceReferenceMetaData resRef : resRefsMD)
         {
            this.resourceRefs.add(new ResourceRefMetadataImpl(resRef));
         }
      }

      // service refs
      ServiceReferencesMetaData serviceRefsMD = this.delegate.getServiceReferences();
      if (serviceRefsMD != null)
      {
         this.serviceRefs = new ArrayList<ServiceRefMetaData>(serviceRefsMD.size());
         for (ServiceReferenceMetaData serviceRef : serviceRefsMD)
         {
            this.serviceRefs.add(new ServiceRefMetadataImpl(serviceRef));
         }
      }
   }

   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<DisplayNameMetaData> getDisplayName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * {@inheritDoc} 
    */
   public String getEjbClass()
   {
      return this.ejbClass;
   }

   /**
    * {@inheritDoc}
    */
   public List<EjbLocalRefMetaData> getEjbLocalRefs()
   {
      return this.ejbLocalRefs;
   }

   /**
    * {@inheritDoc}
    */
   public String getEjbName()
   {
      return this.ejbName;

   }

   /**
    * {@inheritDoc}
    */
   public List<EjbRefMetaData> getEjbRefs()
   {
      return this.ejbRefs;
   }

   /**
    * {@inheritDoc}
    */
   public List<EnvEntryMetaData> getEnvEntries()
   {
      return this.envEntries;
   }

   public List<IconType> getIcon()
   {
      // TODO Auto-generated method stub
      return null;
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
   public List<PersistenceContextRefMetaData> getPersistenceContextRefs()
   {
      return this.persistenceContextRefs;
   }

   /**
    * {@inheritDoc}
    */
   public List<PersistenceUnitRefMetaData> getPersistenceUnitRefs()
   {
      return this.persistenceUnitRefs;
   }

   /**
    * {@inheritDoc}
    */
   public List<LifecycleCallbackMetaData> getPostConstructs()
   {
      return this.postConstructs;
   }

   /**
    * {@inheritDoc}
    */
   public List<LifecycleCallbackMetaData> getPreDestroys()
   {
      return this.preDestroys;
   }

   /**
    * {@inheritDoc}
    */
   public List<ResourceEnvRefMetaData> getResourceEnvRefs()
   {
      return this.resourceEnvRefs;
   }

   /**
    * {@inheritDoc}
    */
   public List<ResourceRefMetaData> getResourceRefs()
   {
      return this.resourceRefs;
   }

   /**
    * {@inheritDoc}
    */
   public SecurityIdentityMetaData getSecurityIdentity()
   {
      return this.securityIdentity;
   }

   /**
    * {@inheritDoc}
    */
   public List<ServiceRefMetaData> getServiceRefs()
   {
      return this.serviceRefs;
   }

   /**
    * {@inheritDoc}
    * @throws IllegalArgumentException If <code>beanClass</code> is null
    */
   public void setEjbClass(String beanClass)
   {
      if (beanClass == null)
      {
         throw new IllegalArgumentException("ejb-class cannot be set to null in " + EnterpriseBeanMetadataImpl.class);
      }
      this.ejbClass = beanClass;
   }

   /**
    * {@inheritDoc}
    */
   public void setEjbLocalRefs(List<EjbLocalRefMetaData> ejbLocalRefs)
   {
      this.ejbLocalRefs = ejbLocalRefs;

   }

   /**
    * {@inheritDoc}
    */
   public void setEjbName(String name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("ejb-name cannot be set to null in " + EnterpriseBeanMetadataImpl.class);
      }
      this.ejbName = name;
   }

   /**
    * {@inheritDoc}
    */
   public void setEjbRefs(List<EjbRefMetaData> ejbRefs)
   {
      this.ejbRefs = ejbRefs;
   }

   /**
    * {@inheritDoc}
    */
   public void setEnvEntries(List<EnvEntryMetaData> envEntries)
   {
      this.envEntries = envEntries;
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
   public void setPeristenceContextRefs(List<PersistenceContextRefMetaData> persistenceContextRefs)
   {
      this.persistenceContextRefs = persistenceContextRefs;
   }

   /**
    * {@inheritDoc}
    */
   public void setPersistenceUnitRefs(List<PersistenceUnitRefMetaData> persistenceUnitRefs)
   {
      this.persistenceUnitRefs = persistenceUnitRefs;

   }

   /**
    * {@inheritDoc}
    */
   public void setPostConstructs(List<LifecycleCallbackMetaData> postConstructs)
   {
      this.postConstructs = postConstructs;
   }

   /**
    * {@inheritDoc}
    */
   public void setPreDestroys(List<LifecycleCallbackMetaData> preDestroys)
   {
      this.preDestroys = preDestroys;
   }

   /**
    * {@inheritDoc}
    */
   public void setResourceEnvRefs(List<ResourceEnvRefMetaData> resourceEnvRefs)
   {
      this.resourceEnvRefs = resourceEnvRefs;
   }

   /**
    * {@inheritDoc}
    */
   public void setResourceRefs(List<ResourceRefMetaData> resourceRefs)
   {
      this.resourceRefs = resourceRefs;
   }

   /**
    * {@inheritDoc}
    */
   public void setSecurityIdentity(SecurityIdentityMetaData securityIdentity)
   {
      this.securityIdentity = securityIdentity;
   }

   /**
    * {@inheritDoc}
    */
   public void setServiceRefs(List<ServiceRefMetaData> serviceRefs)
   {
      this.serviceRefs = serviceRefs;
   }

}
