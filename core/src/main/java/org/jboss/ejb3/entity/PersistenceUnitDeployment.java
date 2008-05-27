/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ejb3.entity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.ejb.HibernatePersistence;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.NonSerializableFactory;
import org.jboss.ejb3.javaee.AbstractJavaEEComponent;
import org.jboss.ejb3.javaee.SimpleJavaEEModule;
import org.jboss.logging.Logger;
import org.jboss.metadata.jpa.spec.PersistenceUnitMetaData;
import org.jboss.metadata.jpa.spec.TransactionType;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class PersistenceUnitDeployment extends AbstractJavaEEComponent
{
   private static final Logger log = Logger.getLogger(PersistenceUnitDeployment.class);

   protected InitialContext initialContext;
   protected DeploymentUnit di;
   protected List<String> explicitEntityClasses = new ArrayList<String>();
   protected ManagedEntityManagerFactory managedFactory;
   protected EntityManagerFactory actualFactory;
   protected PersistenceUnitMetaData metaData;
   protected String kernelName;
   protected Ejb3Deployment deployment;
   protected boolean scoped;

   public PersistenceUnitDeployment(InitialContext initialContext, Ejb3Deployment deployment, List<String> explicitEntityClasses, PersistenceUnitMetaData metadata, String ear, String jar, boolean isScoped)
   {
      super(new SimpleJavaEEModule((deployment.getEar() != null ? deployment.getEar().getShortName() : null), deployment.getDeploymentUnit().getShortName()));
      
      this.scoped = isScoped;
      this.deployment = deployment;
      this.initialContext = initialContext;
      this.di = deployment.getDeploymentUnit();
      this.explicitEntityClasses = explicitEntityClasses;
      this.metaData = metadata;
      kernelName = "persistence.units:";
      String name = getEntityManagerName();
      if (name == null || name.length() == 0)
         throw new RuntimeException("Null string is not allowed for a persistence unit name.  Fix your persistence.xml file");
      
      if (ear != null)
      {
         kernelName += "ear=" + ear;
         if (!ear.endsWith(".ear")) kernelName += ".ear";
         kernelName += ",";
      }
      if (isScoped)
      {
         kernelName += "jar=" + jar;
         if (!jar.endsWith(".jar")) kernelName += ".jar";
         kernelName += ",";
      }
      kernelName += "unitName=" + name;
   }

   public static String getDefaultKernelName(String unitName)
   {
      int hashIndex = unitName.indexOf('#');
      if (hashIndex != -1)
      {
         String relativePath = unitName.substring(3, hashIndex);
         String name = unitName.substring(hashIndex + 1);
         return "persistence.units:jar=" + relativePath + "," + "unitName=" + name;
      }
      return "persistence.units:unitName=" + unitName;
   }

   public boolean isScoped()
   {
      return scoped;
   }

   public Ejb3Deployment getDeployment()
   {
      return deployment;
   }

   protected String getJaccContextId()
   {
      return di.getShortName();
   }

   public EntityManagerFactory getActualFactory()
   {
      return actualFactory;
   }

   public PersistenceUnitMetaData getXml()
   {
      return metaData;
   }

   public String getKernelName()
   {
      return kernelName;
   }

   public String getEntityManagerName()
   {
      return metaData.getName();
   }

   public ManagedEntityManagerFactory getManagedFactory()
   {
      if(managedFactory == null)
         log.warn("managed factory is null, persistence unit " + kernelName + " has not yet been started");
      return managedFactory;
   }

   protected Map<String, String> getProperties()
   {
      Map<String, String> properties = metaData.getProperties();
      return (properties != null) ? properties : Collections.<String, String>emptyMap();
   }

   public void addDependencies(DependencyPolicy policy)
   {
      Map<String, String> props = getProperties();
      if (!props.containsKey("jboss.no.implicit.datasource.dependency"))
      {
         if (metaData.getJtaDataSource() != null)
         {
            String ds = metaData.getJtaDataSource();
            policy.addDatasource(ds);
         }
         if (metaData.getNonJtaDataSource() != null)
         {
            String ds = metaData.getNonJtaDataSource();
            policy.addDatasource(ds);
         }
      }
      for (Object prop : props.keySet())
      {
         String property = (String)prop;
         if (property.startsWith("jboss.depends"))
         {
            policy.addDependency(props.get(property));
         }
      }

   }

   protected PersistenceUnitTransactionType getJPATransactionType()
   {
      TransactionType type = metaData.getTransactionType();
      if (type == TransactionType.RESOURCE_LOCAL)
         return PersistenceUnitTransactionType.RESOURCE_LOCAL;
      else // default or actually being JTA
         return PersistenceUnitTransactionType.JTA;
   }

   private List<String> safeList(Set<String> set)
   {
      return (set == null || set.isEmpty()) ? Collections.<String>emptyList() : new ArrayList<String>(set);
   }

   public void start() throws Exception
   {
      log.info("Starting persistence unit " + kernelName);
      
      Properties props = new Properties();
      props.putAll(di.getDefaultPersistenceProperties());
      props.put(HibernatePersistence.JACC_CONTEXT_ID, getJaccContextId());

      PersistenceUnitInfoImpl pi = new PersistenceUnitInfoImpl();
      log.debug("Using class loader " + di.getClassLoader());
      pi.setClassLoader(di.getClassLoader());

      ArrayList<URL> jarFiles = new ArrayList<URL>();
      pi.setJarFiles(jarFiles);
      pi.setPersistenceProviderClassName(HibernatePersistence.class.getName());
      log.debug("Found persistence.xml file in EJB3 jar");
      props.putAll(getProperties());
      pi.setManagedClassnames(safeList(metaData.getClasses()));
      pi.setPersistenceUnitName(metaData.getName());
      pi.setMappingFileNames(safeList(metaData.getMappingFiles()));
      pi.setExcludeUnlistedClasses(metaData.isExcludeUnlistedClasses());
      log.debug("Persistence root url " + di.getRootFile());
      pi.setPersistenceUnitRootUrl(di.getRootFile().toURL());
//      PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.JTA;
//      if ("RESOURCE_LOCAL".equals(xml.getTransactionType()))
//         transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
      PersistenceUnitTransactionType transactionType = getJPATransactionType();
      pi.setTransactionType(transactionType);

      Set<String> files = metaData.getJarFiles();
      if (files != null)
      {
         for (String jar : files)
         {
            jarFiles.add(deployment.getDeploymentUnit().getRelativeURL(jar));
         }
      }

      if (metaData.getProvider() != null) pi.setPersistenceProviderClassName(metaData.getProvider());
      if (explicitEntityClasses.size() > 0)
      {
         List<String> classes = pi.getManagedClassNames();
         if (classes == null) classes = explicitEntityClasses;
         else classes.addAll(explicitEntityClasses);
         pi.setManagedClassnames(classes);
      }
      if (metaData.getJtaDataSource() != null)
      {
         pi.setJtaDataSource((javax.sql.DataSource) initialContext.lookup(metaData.getJtaDataSource()));
      }
      else if (transactionType == PersistenceUnitTransactionType.JTA)
      {
         throw new RuntimeException("Specification violation [EJB3 JPA 6.2.1.2] - "
               + "You have not defined a jta-data-source for a JTA enabled persistence context named: " + metaData.getName());
      }
      if (metaData.getNonJtaDataSource() != null)
      {
         pi.setNonJtaDataSource((javax.sql.DataSource) initialContext.lookup(metaData.getNonJtaDataSource()));
      }
      else if (transactionType == PersistenceUnitTransactionType.RESOURCE_LOCAL)
      {
         throw new RuntimeException("Specification violation [EJB3 JPA 6.2.1.2] - "
               + "You have not defined a non-jta-data-source for a RESOURCE_LOCAL enabled persistence context named: "
               + metaData.getName());
      }
      pi.setProperties(props);

      if (pi.getPersistenceUnitName() == null)
      {
         throw new RuntimeException("you must specify a name in persistence.xml");
      }

      Class providerClass = Thread.currentThread().getContextClassLoader().loadClass(pi.getPersistenceProviderClassName());

      // EJBTHREE-893
      if(!pi.getProperties().containsKey("hibernate.session_factory_name"))
      {
         pi.getProperties().put("hibernate.session_factory_name", kernelName);
      }
      
      PersistenceProvider pp = (PersistenceProvider) providerClass.newInstance();
      actualFactory = pp.createContainerEntityManagerFactory(pi, null);

      managedFactory = new ManagedEntityManagerFactory(actualFactory, kernelName);

      String entityManagerJndiName = (String) props.get("jboss.entity.manager.jndi.name");
      if (entityManagerJndiName != null)
      {
         EntityManager injectedManager = new TransactionScopedEntityManager(managedFactory);
         NonSerializableFactory.rebind(initialContext, entityManagerJndiName, injectedManager);
      }
      String entityManagerFactoryJndiName = (String) props.get("jboss.entity.manager.factory.jndi.name");
      if (entityManagerFactoryJndiName != null)
      {
         EntityManagerFactory injectedFactory = new InjectedEntityManagerFactory(managedFactory);
         NonSerializableFactory.rebind(initialContext, entityManagerFactoryJndiName, injectedFactory);
      }
   }

   public void stop() throws Exception
   {
      log.info("Stopping persistence unit " + kernelName);
      
      String entityManagerJndiName = getProperties().get("jboss.entity.manager.jndi.name");
      if (entityManagerJndiName != null)
      {
         NonSerializableFactory.unbind(initialContext, entityManagerJndiName);
      }
      String entityManagerFactoryJndiName = getProperties().get("jboss.entity.manager.factory.jndi.name");
      if (entityManagerFactoryJndiName != null)
      {
         NonSerializableFactory.unbind(initialContext, entityManagerFactoryJndiName);
      }
      managedFactory.destroy();
   }
}
