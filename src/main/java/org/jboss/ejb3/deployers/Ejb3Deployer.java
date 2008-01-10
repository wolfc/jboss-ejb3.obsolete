/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.deployers;

import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.security.JaccPolicyUtil;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.metadata.jpa.spec.PersistenceUnitsMetaData;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.ejb3.remoting.RemoteProxyFactoryRegistry;
import org.jboss.kernel.Kernel;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * Deployes EJB 3 components based on meta data coming from JBossEjbParsingDeployer.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class Ejb3Deployer //extends AbstractSimpleVFSRealDeployer<JBossMetaData>
   extends AbstractVFSRealDeployer
{
   private Set<String> allowedSuffixes;
   
   private Properties defaultPersistenceProperties;
   
   /** EJBTHREE-1040: mandate a deployment descriptor to actually deploy */
   private boolean deploymentDescriptorRequired;
   
   private Set<String> ignoredJarsSet;
   
   private Kernel kernel;
   
   private MBeanServer mbeanServer;
   
   private CacheFactoryRegistry cacheFactoryRegistry;
   
   private PoolFactoryRegistry poolFactoryRegistry;
   
   private RemoteProxyFactoryRegistry remoteProxyFactoryRegistry;
   
   private PersistenceManagerFactoryRegistry persistenceManagerFactoryRegistry;
   
   public Ejb3Deployer()
   {
      // TODO: when the annotation scanner deployer comes on, we will always have JBossMetaData
      //super(JBossMetaData.class);
      addInput(JBossMetaData.class);
      // TODO: PersistenceUnits will have it's own component deployer
      addInput(PersistenceUnitsMetaData.class);
      // TODO: should we really output this
      setOutput(Ejb3Deployment.class);
   }

   @Override
   public void deploy(VFSDeploymentUnit unit) throws DeploymentException
   {
      deploy(unit, unit.getAttachment(JBossMetaData.class), unit.getAttachment(PersistenceUnitsMetaData.class));
   }
   
   public void deploy(VFSDeploymentUnit unit, JBossMetaData metaData, PersistenceUnitsMetaData persistenceUnitsMetaData) throws DeploymentException
   {
      try
      {
         // Pickup any deployment which doesn't have metaData or metaData with ejbVersion unknown or 3
         if(metaData != null && (metaData.isEJB2x() || metaData.isEJB1x()))
         {
            assert persistenceUnitsMetaData == null : "Found persistence units in legacy deployment";
            
            log.debug("Ignoring legacy EJB deployment " + unit);
            return;
         }
         
         VirtualFile jar = unit.getRoot();
         if (jar.isLeaf() || ignoredJarsSet.contains(jar.getName()))
         {
            log.trace(this.getClass().getName() + " ignoring: " + jar.getName());
            return;
         }
         if(!hasAllowedSuffix(jar.getName()))
         {
            log.trace(this.getClass().getName() + " suffix not allowed: " + jar.getName());
            return;
         }
         
         // If DDs are required and none are present, skip deployment
         // EJBTHREE-1040
         if (this.isDeploymentDescriptorRequired() && (metaData == null) && persistenceUnitsMetaData == null)
         {
            log.trace(this.getClass().getSimpleName() + " skipping deployment \"" + unit.getSimpleName()
                  + "\", jar: \"" + jar.getName()
                  + "\" - either EJB3 Deployment Descriptor or \"jboss.xml\" is required and neither were found.");
            return;
         }
            
         log.debug("********* " + this.getClass().getSimpleName() + " Begin Unit: " + unit.getSimpleName() + " jar: "
               + jar.getName());
         DeploymentScope scope = null;
         VFSDeploymentUnit parent = unit.getParent();
         if (parent != null && parent.getSimpleName().endsWith(".ear")) // todo should look for metadata instead of ".ear"
         {
            scope = parent.getAttachment(DeploymentScope.class);
            if (scope == null)
            {
               scope = new JBoss5DeploymentScope(unit.getParent());
               parent.addAttachment(DeploymentScope.class, scope);
            }
         }
         JBoss5DeploymentUnit du = new JBoss5DeploymentUnit(unit);
         du.setDefaultPersistenceProperties(defaultPersistenceProperties);
         Ejb3JBoss5Deployment deployment = new Ejb3JBoss5Deployment(du, kernel, mbeanServer, unit, scope, metaData, persistenceUnitsMetaData,
               this);
         if (scope != null) scope.register(deployment);
         // create() creates initial EJB containers and initializes metadata.
         deployment.create();
         if (deployment.getEjbContainers().size() == 0 && deployment.getPersistenceUnitDeployments().size() == 0)
         {
            log.trace("Found no containers in scanned jar, consider adding it to the ignore list: " + jar.getName() + " url: " + jar.toURL() + " unit: " + unit.getSimpleName());
            deployment.destroy();
            return;
         }
         unit.addAttachment(Ejb3Deployment.class, deployment);
         // TODO: temporarily disable the security deployment
         unit.addAttachment(JaccPolicyUtil.IGNORE_ME_NAME, true, Boolean.class);
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Error deploying " + unit.getSimpleName() + ": " + t.getMessage(), t);
      }
   }

   public Set<String> getAllowedSuffixes()
   {
      return allowedSuffixes;
   }
   
   public CacheFactoryRegistry getCacheFactoryRegistry()
   {
      return cacheFactoryRegistry;
   }
   
   public void setCacheFactoryRegistry(CacheFactoryRegistry cacheFactoryRegistry)
   {
      this.cacheFactoryRegistry = cacheFactoryRegistry;
   }

   public PoolFactoryRegistry getPoolFactoryRegistry()
   {
      return poolFactoryRegistry;
   }

   public void setPoolFactoryRegistry(PoolFactoryRegistry poolFactoryRegistry)
   {
      this.poolFactoryRegistry = poolFactoryRegistry;
   }

   public RemoteProxyFactoryRegistry getRemoteProxyFactoryRegistry()
   {
      return remoteProxyFactoryRegistry;
   }

   public void setRemoteProxyFactoryRegistry(RemoteProxyFactoryRegistry remoteProxyFactoryRegistry)
   {
      this.remoteProxyFactoryRegistry = remoteProxyFactoryRegistry;
   }

   public PersistenceManagerFactoryRegistry getPersistenceManagerFactoryRegistry()
   {
      return persistenceManagerFactoryRegistry;
   }

   public void setPersistenceManagerFactoryRegistry(PersistenceManagerFactoryRegistry persistenceManagerFactoryRegistry)
   {
      this.persistenceManagerFactoryRegistry = persistenceManagerFactoryRegistry;
   }

   private boolean hasAllowedSuffix(String name)
   {
      if(allowedSuffixes == null)
         return true;
      
      for (String suffix : allowedSuffixes)
      {
         if (name.endsWith(suffix))
         {
            return true;
         }
      }
      return false;
   }
   
   public boolean isDeploymentDescriptorRequired()
   {
      return deploymentDescriptorRequired;
   }
   
   public void setAllowedSuffixes(Set<String> s)
   {
      this.allowedSuffixes = s;
   }
   
   public void setDefaultPersistenceProperties(Properties p)
   {
      this.defaultPersistenceProperties = p;
   }
   
   public void setDeploymentDescriptorRequired(boolean b)
   {
      this.deploymentDescriptorRequired = b;
   }
   
   public void setIgnoredJarsSet(Set<String> s)
   {
      this.ignoredJarsSet = s;
   }
   
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }
   
   public void setMbeanServer(MBeanServer server)
   {
      this.mbeanServer = server;
   }
   
   @Override
   public void undeploy(VFSDeploymentUnit unit)
   {
      Ejb3Deployment deployment = unit.getAttachment(Ejb3Deployment.class);
      if(deployment == null) return;
      
      try
      {
         deployment.destroy();
      }
      catch(Exception e)
      {
         log.warn("Failed to destroy deployment " + deployment, e);
      }
   }
}
