/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.common;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.plugins.AbstractDemandMetaData;
import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.SupplyMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.ClassLoaderFactory;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.MCDependencyPolicy;
import org.jboss.ejb3.MCKernelAbstraction.AlreadyInstantiated;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.common.deployers.spi.AttachmentNames;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.ejb3.core.resolvers.ScopedEJBReferenceResolver;
import org.jboss.ejb3.service.ServiceContainer;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.ejb3.test.cachepassivation.MockDeploymentUnit;
import org.jboss.ejb3.test.common.MetaDataHelper;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Create an environment with some basic facilities on which EJB 3 containers
 * depend.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractEJB3TestCase
{
   private static EmbeddedTestMcBootstrap bootstrap;

   private static final Logger log = Logger.getLogger(AbstractEJB3TestCase.class);

   private static final String DOMAIN_NAME_SLSB = "Stateless Bean";

   private static final String DOMAIN_NAME_SFSB = "Stateful Bean";

   private static final String OBJECT_STORE_NAME_PM_FACTORY_REGISTRY = "EJB3PersistenceManagerFactoryRegistry";

   private static final String OBJECT_STORE_NAME_CACHE_FACTORY_REGISTRY = "EJB3CacheFactoryRegistry";

   private static InitialContext initialContext;

   private static Set<SessionContainer> allDeployedContainers = new HashSet<SessionContainer>();

   /**
    * Types of Containers Supported
    */
   enum ContainerType {
      SFSB, SLSB, SERVICE
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      Set<SessionContainer> deployedContainers = new HashSet<SessionContainer>();
      deployedContainers.addAll(allDeployedContainers);
      for (SessionContainer container : deployedContainers)
         undeployEjb(container);
      allDeployedContainers.clear();

      if (initialContext != null)
         initialContext.close();
      initialContext = null;

      URL url = Thread.currentThread().getContextClassLoader().getResource("ejb3-interceptors-aop.xml");
      if (url != null)
         AspectXmlLoader.undeployXML(url);

      if (bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;

      // Unbind Registrar
      Ejb3RegistrarLocator.unbindRegistrar();

   }

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      // FIXME: weirdness in InitialContextFactory (see EJBTHREE-1097)
      InitialContextFactory.close(null, null);

      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();

      // Bind Registrar
      if (!Ejb3RegistrarLocator.isRegistrarBound())
      {
         Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));
      }

      deploy("namingserver-beans.xml");
      deploy("transactionmanager-beans.xml");
      deploy("mocktimerservice-beans.xml");
      deploy("servicecontainer-beans.xml");
      deploy("statefulcontainer-beans.xml");
      deploy("statelesscontainer-beans.xml");
      deploy("connector-beans.xml");
      deploy("container-beans.xml");

      // TODO: AspectDeployment
      URL url = Thread.currentThread().getContextClassLoader().getResource("ejb3-interceptors-aop.xml");
      if (url == null)
         throw new IllegalStateException("Can't find ejb3-interceptors-aop.xml on class loader "
               + Thread.currentThread().getContextClassLoader());
      AspectXmlLoader.deployXML(url);

      initialContext = new InitialContext();
   }

   protected static void deploy(String resourceName)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
      if (url == null)
         throw new IllegalArgumentException("Can't find a resource named '" + resourceName + "'");
      assert bootstrap != null : "Can't deploy a resource, bootstrap is null";
      bootstrap.deploy(url);
   }

   protected static EmbeddedTestMcBootstrap getBootstrap()
   {
      return bootstrap;
   }

   protected static Domain getDomain(String domainName)
   {
      DomainDefinition domainDef = AspectManager.instance().getContainer(domainName);
      if (domainDef == null)
         throw new IllegalArgumentException("No such domain '" + domainName + "'");
      return (Domain) domainDef.getManager();
   }

   protected static InitialContext getInitialContext()
   {
      return initialContext;
   }

   /**
    * Creates and deploys a Session EJB represented by the specified implementation classes
    * 
    * @param beanImplementationClasses
    * @return
    * @throws DeploymentException 
    */
   public static Collection<SessionContainer> deploySessionEjbs(Class<?>... beanImplementationClasses) throws DeploymentException
   {
      // Initialize
      Collection<SessionContainer> containers = new HashSet<SessionContainer>();

      // Obtain TCL
      ClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });

      AbstractDeploymentUnit deploymentUnit = new AbstractDeploymentUnit(new AbstractDeploymentContext("test", ""));
      deploymentUnit.createClassLoader(new ClassLoaderFactory() {
         public ClassLoader createClassLoader(org.jboss.deployers.structure.spi.DeploymentUnit unit) throws Exception
         {
            return Thread.currentThread().getContextClassLoader();
         }

         public void removeClassLoader(org.jboss.deployers.structure.spi.DeploymentUnit unit) throws Exception
         {
         }
      });
      DeploymentUnit unit = new MockDeploymentUnit(deploymentUnit);
      Ejb3Deployment deployment = new MockEjb3Deployment(unit, deploymentUnit);

      deployment.setEJBReferenceResolver(new ScopedEJBReferenceResolver());
      
      /*
       * Create Metadata
       */

      // Create metadata
      JBossMetaData jbossMetaData = MetaDataHelper.getMetaDataFromBeanImplClasses(beanImplementationClasses);

      unit.addAttachment(AttachmentNames.PROCESSED_METADATA, jbossMetaData);
      
      // Iterate through each EJB
      for (JBossEnterpriseBeanMetaData beanMetaData : jbossMetaData.getEnterpriseBeans())
      {

         // Ensure a Session Bean
         assert beanMetaData.isSession() || beanMetaData.isService() : "The specified EJB must be a Session Bean or a Service Bean";

         // Cast 
         JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData) beanMetaData;

         /*
          * Determine type
          */

         // Initialize as SLSB
         ContainerType sessionType = ContainerType.SLSB;

         // Set as SFSB if stateful
         if (smd.isStateful())
         {
            sessionType = ContainerType.SFSB;
         }
         else if (beanMetaData.isService())
            sessionType = ContainerType.SERVICE;

         // Ensure jndi.properties is accessible
         log.info("Found: " + cl.getResource("jndi.properties"));

         // Obtain properties required of container construction
         String beanClassname = smd.getEjbClass();
         Domain domain = getDomain(sessionType.equals(ContainerType.SLSB)
               ? AbstractEJB3TestCase.DOMAIN_NAME_SLSB
               : AbstractEJB3TestCase.DOMAIN_NAME_SFSB);
         Hashtable<?, ?> ctxProperties = null;
         
         // Is SFSB, manually set a PM Factory Registry and Cache Factory
         //TODO C'mon, here?  Much better elsewhere.
         if (sessionType.equals(ContainerType.SFSB))
         {
            // Lookup Factory Registries in MC
            PersistenceManagerFactoryRegistry registry = Ejb3RegistrarLocator.locateRegistrar().lookup(
                  AbstractEJB3TestCase.OBJECT_STORE_NAME_PM_FACTORY_REGISTRY, PersistenceManagerFactoryRegistry.class);
            CacheFactoryRegistry cacheFactoryRegistry = Ejb3RegistrarLocator.locateRegistrar().lookup(
                  AbstractEJB3TestCase.OBJECT_STORE_NAME_CACHE_FACTORY_REGISTRY, CacheFactoryRegistry.class);

            // Set on the deployment
            deployment.setPersistenceManagerFactoryRegistry(registry);
            deployment.setCacheFactoryRegistry(cacheFactoryRegistry);
         }

         // Create a Session Container
         SessionContainer container = instanciateContainer(sessionType, cl, beanClassname, smd.getEjbName(), domain,
               ctxProperties, deployment, smd);

         // Deploy and register
         registerContainer(container);
         containers.add(container);

      }

      // Return
      return containers;
   }

   /**
    * Creates and deploys a Session EJB represented by the specified implementation class
    * 
    * @param beanImplementationClass
    * @return
    * @throws DeploymentException 
    */
   public static SessionContainer deploySessionEjb(Class<?> beanImplementationClass) throws DeploymentException
   {
      Collection<SessionContainer> containers = deploySessionEjbs(new Class<?>[]
      {beanImplementationClass});
      assert containers.size() == 1 : "Was only expected one " + SessionContainer.class.getSimpleName()
            + " from bean impl class: " + beanImplementationClass;
      return containers.iterator().next();
   }

   private static void install(String name, Object service, DependencyPolicy dependencies) throws Exception
   {
      AbstractBeanMetaData bean = new AbstractBeanMetaData(name, service.getClass().getName());
      bean.setConstructor(new AlreadyInstantiated(service));
      MCDependencyPolicy policy = (MCDependencyPolicy) dependencies;
      bean.setDepends(policy.getDependencies());
      bean.setDemands(policy.getDemands());
      bean.setSupplies(policy.getSupplies());
      log.info("installing bean: " + name);
      log.info("  with dependencies:");
      for (Object obj : policy.getDependencies())
      {
         Object msgObject = obj;
         if (obj instanceof AbstractDemandMetaData)
         {
            msgObject = ((AbstractDemandMetaData)obj).getDemand();
         }
         log.info("\t" + msgObject);
      }
      log.info("  and demands:");
      for(DemandMetaData dmd : policy.getDemands())
      {
         log.info("\t" + dmd.getDemand());
      }
      log.info("  and supplies:");
      for(SupplyMetaData smd : policy.getSupplies())
      {
         log.info("\t" + smd.getSupply());
      }
      try
      {
         bootstrap.getKernel().getController().install(bean);
      }
      catch(Throwable t)
      {
         if(t instanceof Error)
            throw (Error) t;
         if(t instanceof RuntimeException)
            throw (RuntimeException) t;
         throw (Exception) t;
      }
   }
   /**
    * Instanciates the appropriate SessionContainer based on the specified arguments and returns it
    *  
    * @param type
    * @param loader
    * @param beanClassName
    * @param ejbName
    * @param domain
    * @param ctxProperties
    * @param deployment
    * @param md
    * @return
    */
   private static SessionContainer instanciateContainer(ContainerType type, ClassLoader loader, String beanClassName,
         String ejbName, Domain domain, Hashtable<?, ?> ctxProperties, Ejb3Deployment deployment,
         JBossSessionBeanMetaData md)
   {
      // Initialize
      SessionContainer container = null;

      /*
       * Instanciate the Container, depending upon the type specified
       */
      switch (type)
      {
         case SERVICE :
            try
            {
               domain = getDomain("Service Bean");
               container = new ServiceContainer(null, loader, beanClassName, ejbName, domain, ctxProperties,
                     deployment, (JBossServiceBeanMetaData) md);
            }
            catch (ClassNotFoundException cnfe)
            {
               throw new RuntimeException("Could not create SLSB Container for " + beanClassName, cnfe);
            }
            break;
         case SFSB :
            try
            {
               container = new StatefulContainer(loader, beanClassName, ejbName, domain, ctxProperties, deployment, md);
            }
            catch (ClassNotFoundException cnfe)
            {
               throw new RuntimeException("Could not create SLSB Container for " + beanClassName, cnfe);
            }
            break;
         case SLSB :
            try
            {
               container = new StatelessContainer(loader, beanClassName, ejbName, domain, ctxProperties, deployment, md);
            }
            catch (ClassNotFoundException cnfe)
            {
               throw new RuntimeException("Could not create SLSB Container for " + beanClassName, cnfe);
            }
            break;
         default :
            throw new UnsupportedOperationException("Only SFSB and SLSB currently supported");
      }

      // Return
      return container;
   }

   protected static <T> T lookup(String name, Class<T> type) throws NamingException
   {
      return type.cast(getInitialContext().lookup(name));
   }

   /**
    * Deploys, registers the specified Session Container
    * 
    * @param beanImplementationClass
    * @return
    * @throws DeploymentException 
    */
   private static SessionContainer registerContainer(SessionContainer container) throws DeploymentException
   {
      //FIXME
      // Typically these steps are done by Ejb3Deployment
      container.instantiated(); //TODO: Wickeness
      container.processMetadata();
      Ejb3Registry.register(container);

      // Add as one of the deployed containers here
      allDeployedContainers.add(container);

      // Register the Container in ObjectStore (MC) - will also invoke lifecycle
      String containerName = container.getObjectName().getCanonicalName();
      try
      {
         install(containerName, container, container.getDependencyPolicy());
      }
      catch(Exception e)
      {
         throw new DeploymentException(e);
      }

      // make sure we're installed
      try
      {
         bootstrap.lookup(containerName, Object.class);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }

      // Return
      return container;
   }

   /**
    * Undeploys the specified Container
    * 
    * @param container
    */
   public static void undeployEjb(SessionContainer container)
   {
      // Igonre a null container (maybe deployment did not succeed)
      if (container == null)
         return;
      if (!allDeployedContainers.contains(container))
      {
         return;
      }

      unregisterContainer(container);

   }

   private static void unregisterContainer(SessionContainer container)
   {
      // Unbind and call appropriate lifecycle events
      try
      {
         Ejb3RegistrarLocator.locateRegistrar().unbind(container.getObjectName().getCanonicalName());
      }
      catch (NotBoundException nbe)
      {
         // Ignore
      }
      Ejb3Registry.unregister(container);
      allDeployedContainers.remove(container);
   }
}
