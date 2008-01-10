/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.clientmodule;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferencesMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationUsageType;
import org.jboss.metadata.javaee.spec.ResourceAuthorityType;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metamodel.descriptor.EjbLocalRef;
import org.jboss.metamodel.descriptor.EjbRef;
import org.jboss.metamodel.descriptor.EnvEntry;
import org.jboss.metamodel.descriptor.EnvironmentRefGroup;
import org.jboss.metamodel.descriptor.InjectionTarget;
import org.jboss.metamodel.descriptor.JndiRef;
import org.jboss.metamodel.descriptor.MessageDestinationRef;
import org.jboss.metamodel.descriptor.PersistenceContextRef;
import org.jboss.metamodel.descriptor.PersistenceUnitRef;
import org.jboss.metamodel.descriptor.ResourceEnvRef;
import org.jboss.metamodel.descriptor.ResourceRef;
import org.jboss.wsf.spi.serviceref.ServiceRefMetaData;

/**
 * Convert unified metadata RemoteEnvironmentRefsGroupMetaData to a EnvironmentRefGroup
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class EnvironmentRefGroupConveter
   extends EnvironmentRefGroup
{
   private static final Logger log = Logger.getLogger(EnvironmentRefGroupConveter.class);
   private Environment refs;
   private boolean convertedEjbLocalRefs;
   private boolean convertedEjbRefs = false;
   private boolean convertedEnvEntries = false;
   private boolean convertedMessageDestinationRefs = false;
   private boolean convertedResourceEnvRefs = false;
   private boolean convertedResourceRefs = false;
   private boolean convertedServiceRefs = false;

   public EnvironmentRefGroupConveter(Environment refs)
   {
      this.refs = refs;
   }

   @Override
   public Collection<EjbLocalRef> getEjbLocalRefs()
   {
      if(convertedEjbLocalRefs == false)
      {
         convertedEjbLocalRefs = true;
      }
      return super.getEjbLocalRefs();
   }

   @Override
   public Collection<EjbRef> getEjbRefs()
   {
      if(convertedEjbRefs == false)
      {
         EJBReferencesMetaData erefs = refs.getEjbReferences();
         if(erefs != null)
         for(EJBReferenceMetaData ref : erefs)
         {
            EjbRef eref = new EjbRef();
            eref.setEjbLink(ref.getLink());
            eref.setEjbRefName(ref.getEjbRefName());
            eref.setEjbRefType(ref.getType());
            eref.setHome(ref.getHome());
            eref.setRemote(ref.getRemote());
            eref.setIgnoreDependency(ref.isDependencyIgnored());
            eref.setMappedName(ref.getMappedName());
            eref.setInjectionTarget(getInjectionTarget(ref.getInjectionTargets()));
            ejbRefs.put(ref.getEjbRefName(), eref);
         }
         log.info("Converted "+ejbRefs.size()+" refs: "+ejbRefs.keySet());
         convertedEjbRefs = true;
      }
      return super.getEjbRefs();
   }

   @Override
   public Collection<EnvEntry> getEnvEntries()
   {
      if(convertedEnvEntries == false)
      {
         EnvironmentEntriesMetaData env = refs.getEnvironmentEntries();
         if (env != null)
         {
            for(EnvironmentEntryMetaData entry : env)
            {
               EnvEntry ee = new EnvEntry();
               ee.setEnvEntryName(entry.getEnvEntryName());
               ee.setEnvEntryType(entry.getType());
               ee.setEnvEntryValue(entry.getValue());
               Set<ResourceInjectionTargetMetaData> rits = entry.getInjectionTargets();
               if(rits != null && rits.isEmpty() == false)
               {
                  ResourceInjectionTargetMetaData rit = rits.iterator().next();
                  InjectionTarget it = new InjectionTarget();
                  it.setTargetClass(rit.getInjectionTargetClass());
                  it.setTargetName(rit.getInjectionTargetName());
                  ee.setInjectionTarget(it);
               }
               ee.setIgnoreDependency(entry.isDependencyIgnored());
               envEntries.put(ee.getEnvEntryName(), ee);
            }
            log.info("Converted "+envEntries.size()+" envs: "+envEntries.keySet());
         }
         convertedEnvEntries = true;
      }
      return super.getEnvEntries();
   }

   @Override
   public Collection<JndiRef> getJndiRefs()
   {
      return super.getJndiRefs();
   }

   @Override
   public MessageDestinationRef getMessageDestinationRefForLink(String link)
   {
      if(convertedMessageDestinationRefs == false)
         getMessageDestinationRefs();
      return super.getMessageDestinationRefForLink(link);
   }

   @Override
   public Collection<MessageDestinationRef> getMessageDestinationRefs()
   {
      if(convertedMessageDestinationRefs == false)
      {
         MessageDestinationReferencesMetaData mrefs = refs.getMessageDestinationReferences();
         if(mrefs != null)
         for(MessageDestinationReferenceMetaData mref : mrefs)
         {
            MessageDestinationRef ref = new MessageDestinationRef();
            ref.setIgnoreDependency(mref.isDependencyIgnored());
            ref.setMappedName(mref.getMappedName());
            ref.setJndiName(mref.getMappedName());
            ref.setMessageDestinationLink(mref.getLink());
            ref.setMessageDestinationRefName(mref.getMessageDestinationRefName());
            ref.setMessageDestinationType(mref.getType());
            MessageDestinationUsageType usage = mref.getMessageDestinationUsage();
            if(usage != null)
               ref.setMessageDestinationUsage(usage.name());
            messageDestinationRefs.put(ref.getMessageDestinationRefName(), ref);
         }
         log.info("Converted "+messageDestinationRefs.size()+" msgRefs: "+messageDestinationRefs.keySet());
         convertedMessageDestinationRefs = true;
      }
      return super.getMessageDestinationRefs();
   }

   @Override
   public List<PersistenceContextRef> getPersistenceContextRefs()
   {
      // TODO Auto-generated method stub
      return super.getPersistenceContextRefs();
   }

   @Override
   public List<PersistenceUnitRef> getPersistenceUnitRefs()
   {
      // TODO Auto-generated method stub
      return super.getPersistenceUnitRefs();
   }

   @Override
   public Collection<ResourceEnvRef> getResourceEnvRefs()
   {
      if(convertedResourceEnvRefs == false)
      {
         ResourceEnvironmentReferencesMetaData rrefs = refs.getResourceEnvironmentReferences();
         if(rrefs != null)
         for(ResourceEnvironmentReferenceMetaData rref : rrefs)
         {
            ResourceEnvRef ref = new ResourceEnvRef();
            ref.setResRefName(rref.getResourceEnvRefName());
            ref.setIgnoreDependency(ref.isIgnoreDependency());
            ref.setResType(rref.getType());
            ref.setJndiName(rref.getJndiName());
            ref.setMappedName(rref.getMappedName());
            ref.setResAuth("Container");
            ref.setInjectionTarget(getInjectionTarget(rref.getInjectionTargets()));
            resourceEnvRefs.put(ref.getResRefName(), ref);
         }
         log.info("Converted "+resourceEnvRefs.size()+" envRefs: "+resourceEnvRefs.keySet());
         convertedResourceEnvRefs = true;
      }
      return super.getResourceEnvRefs();
   }

   @Override
   public Collection<ResourceRef> getResourceRefs()
   {
      if(convertedResourceRefs == false)
      {
         ResourceReferencesMetaData rrefs = refs.getResourceReferences();
         if(rrefs != null)
         for(ResourceReferenceMetaData rref : rrefs)
         {
            ResourceRef ref = new ResourceRef();
            ref.setResRefName(rref.getResourceRefName());
            ref.setIgnoreDependency(ref.isIgnoreDependency());
            ref.setResType(rref.getType());
            ref.setJndiName(rref.getJndiName());
            ref.setMappedName(rref.getMappedName());
            ref.setResUrl(rref.getResUrl());
            ResourceAuthorityType authType = rref.getResAuth();
            if(authType != null)
               ref.setResAuth(authType.name());
            ref.setInjectionTarget(getInjectionTarget(rref.getInjectionTargets()));
            resourceRefs.put(ref.getResRefName(), ref);
         }
         log.info("Converted "+resourceRefs.size()+" resRefs: "+resourceRefs.keySet());
         convertedResourceRefs = true;
      }
      return super.getResourceRefs();
   }

   @Override
   public ServiceRefMetaData getServiceRef(String name)
   {
      if(convertedServiceRefs == false)
         getServiceRefs();
      return super.getServiceRef(name);
   }

   @Override
   public Collection<ServiceRefMetaData> getServiceRefs()
   {
      if(convertedServiceRefs == false)
      {
         convertedServiceRefs = true;
      }
      return super.getServiceRefs();
   }

   private InjectionTarget getInjectionTarget(Set<ResourceInjectionTargetMetaData> rits)
   {
      InjectionTarget it = null;
      if(rits != null && rits.isEmpty() == false)
      {
         ResourceInjectionTargetMetaData rit = rits.iterator().next();
         it = new InjectionTarget();
         it.setTargetClass(rit.getInjectionTargetClass());
         it.setTargetName(rit.getInjectionTargetName());
      }
      return it;
   }
}
