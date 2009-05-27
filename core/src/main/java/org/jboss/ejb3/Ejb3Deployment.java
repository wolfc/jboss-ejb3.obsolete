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
package org.jboss.ejb3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.bytecode.ClassFile;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.security.jacc.PolicyConfiguration;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.cache.CacheFactoryRegistry;
import org.jboss.ejb3.cache.persistence.PersistenceManagerFactoryRegistry;
import org.jboss.ejb3.common.lang.ClassHelper;
import org.jboss.ejb3.common.resolvers.spi.EjbReference;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.ejb3.javaee.JavaEEApplication;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.javaee.JavaEEComponentHelper;
import org.jboss.ejb3.javaee.JavaEEModule;
import org.jboss.ejb3.metadata.JBossSessionGenericWrapper;
import org.jboss.ejb3.metrics.spi.SessionMetrics;
import org.jboss.ejb3.pool.PoolFactoryRegistry;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.proxy.factory.RemoteProxyFactoryRegistry;
import org.jboss.ejb3.resolvers.DefaultMessageDestinationReferenceResolver;
import org.jboss.ejb3.resolvers.MessageDestinationReferenceResolver;
import org.jboss.injection.InjectionHandler;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossGenericBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanGenericWrapper;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.MessageDestinationsMetaData;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.virtual.VirtualFile;

/**
 * An EjbModule represents a collection of beans that are deployed as a unit.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision$
 */
public abstract class Ejb3Deployment extends ServiceMBeanSupport
  implements JavaEEModule, Ejb3DeploymentMBean
{
   private static final Logger log = Logger.getLogger(Ejb3Deployment.class);

   public static final String ACTUAL_ENTITY_MANAGER_FACTORY_CONTEXT = "java:/ActualEntityManagerFactories";

   public static final String MANAGED_ENTITY_FACTORY_CONTEXT = "java:/managedEntityFactories";

   private JBossMetaData metaData;
   
   protected DeploymentUnit unit;

   protected LinkedHashMap<ObjectName, Container> ejbContainers = new LinkedHashMap<ObjectName, Container>();

   protected boolean hasEntities;

   protected List<String> explicitEntityClasses = new ArrayList<String>();

   protected String defaultSLSBDomain = "Stateless Bean";

   protected String defaultSFSBDomain = "Stateful Bean";

   protected String defaultMDBDomain = "Message Driven Bean";

   protected String defaultConsumerDomain = "Consumer Bean";

   protected String defaultServiceDomain = "Service Bean";

   protected InitialContext initialContext;

   protected KernelAbstraction kernelAbstraction;

   // used for @Management interfaces
   protected MBeanServer mbeanServer;

   protected DeploymentScope deploymentScope;

   // For backwards compatibility initialize to the default.
   private MessageDestinationReferenceResolver messageDestinationReferenceResolver = new DefaultMessageDestinationReferenceResolver();
   
   protected CacheFactoryRegistry cacheFactoryRegistry;
   protected PersistenceManagerFactoryRegistry persistenceManagerFactoryRegistry;
   protected PoolFactoryRegistry poolFactoryRegistry;
   
   protected ObjectName objectName;
   
   protected boolean reinitialize = false;

   private org.jboss.deployers.structure.spi.DeploymentUnit deploymentUnit;
   
   private EjbReferenceResolver ejbReferenceResolver;

   private PersistenceUnitDependencyResolver persistenceUnitDependencyResolver;
   
   /**
    * Metrics used in Session Beans
    */
   private SessionMetrics metrics;

   /**
    * Do not deploy persistence unit anymore.
    * 
    * @param deploymentUnit
    * @param unit
    * @param deploymentScope
    * @param metaData
    */
   public Ejb3Deployment(org.jboss.deployers.structure.spi.DeploymentUnit deploymentUnit, DeploymentUnit unit, DeploymentScope deploymentScope, JBossMetaData metaData)
   {
      assert unit != null : "unit is null";
      assert deploymentUnit != null : "deploymentUnit is null";
      
      this.unit = unit;
      this.deploymentScope = deploymentScope;
      this.metaData = metaData;
      try
      {
         initialContext = InitialContextFactory.getInitialContext(unit.getJndiProperties());
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      MessageDestinationsMetaData destinations = null;
      if (metaData != null && metaData.getAssemblyDescriptor() != null)
         destinations = metaData.getAssemblyDescriptor().getMessageDestinations();
      this.deploymentUnit = deploymentUnit;
   }

   @Deprecated
   public boolean canResolveEJB()
   {
      return ejbReferenceResolver != null;
   }
   
   public JavaEEApplication getApplication()
   {
      return deploymentScope;
   }

   public DeploymentScope getEar()
   {
      return deploymentScope;
   }

   public KernelAbstraction getKernelAbstraction()
   {
      return kernelAbstraction;
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public DeploymentUnit getDeploymentUnit()
   {
      return unit;
   }

   public String getDefaultSLSBDomain()
   {
      return defaultSLSBDomain;
   }

   public CacheFactoryRegistry getCacheFactoryRegistry()
   {
      return cacheFactoryRegistry;
   }
   public void setCacheFactoryRegistry(CacheFactoryRegistry registry)
   {
      this.cacheFactoryRegistry = registry;
   }

   @Deprecated
   public RemoteProxyFactoryRegistry getRemoteProxyFactoryRegistry()
   {
      log.warn("[EJBTHREE-1641] NoOp getRemoteProxyFactoryRegistry; developers may ignore this message; it will be removed when backwards-compatibility between EJB3 and AS is resolved");
      return null;
   }
   
   @Deprecated
   public void setRemoteProxyFactoryRegistry(RemoteProxyFactoryRegistry registry)
   {
      log.warn("[EJBTHREE-1641] NoOp setRemoteProxyFactoryRegistry; developers may ignore this message; it will be removed when backwards-compatibility between EJB3 and AS is resolved");
   }

   public PersistenceManagerFactoryRegistry getPersistenceManagerFactoryRegistry()
   {
      return persistenceManagerFactoryRegistry;
   }
   
   @Inject
   public void setEJBReferenceResolver(EjbReferenceResolver resolver)
   {
      this.ejbReferenceResolver = resolver;
   }
   
   @Inject
   public void setMessageDestinationReferenceResolver(MessageDestinationReferenceResolver resolver)
   {
      this.messageDestinationReferenceResolver = resolver;
   }
   
   public void setPersistenceManagerFactoryRegistry(PersistenceManagerFactoryRegistry registry)
   {
      this.persistenceManagerFactoryRegistry = registry;
   }
   
   @Inject
   public void setPersistenceUnitDependencyResolver(PersistenceUnitDependencyResolver resolver)
   {
      this.persistenceUnitDependencyResolver = resolver;
   }
   
   public PoolFactoryRegistry getPoolFactoryRegistry()
   {
      return poolFactoryRegistry;
   }
   public void setPoolFactoryRegistry(PoolFactoryRegistry poolFactoryRegistry)
   {
      this.poolFactoryRegistry = poolFactoryRegistry;
   }
   
   /**
    * @return the metrics
    */
   public SessionMetrics getMetrics()
   {
      return metrics;
   }

   /**
    * @param metrics the metrics to set
    */
   public void setMetrics(final SessionMetrics metrics)
   {
      this.metrics = metrics;
   }

   /**
    * @deprecated processing persistence units is no longer supported, use jpa-deployers
    * @param b
    */
   @Deprecated
   public void setProcessPersistenceUnits(boolean b)
   {
      if(b)
         log.warn("EJBTHREE-1508: Processing persistence units is no longer supported");
   }
   
   /**
    * Returns a partial MBean attribute name of the form
    * ",ear=foo.ear,jar=foo.jar"
    *
    * @return
    */
   private String getScopeKernelName()
   {
      String scopedKernelName = "";
      if (deploymentScope != null)
         scopedKernelName += ",ear=" + deploymentScope.getShortName();
      scopedKernelName += ",jar=" + unit.getShortName();
      return scopedKernelName;
   }

   /**
    * The default AOP domain for stateless session beans
    *
    * @param defaultSLSBDomain
    */
   public void setDefaultSLSBDomain(String defaultSLSBDomain)
   {
      this.defaultSLSBDomain = defaultSLSBDomain;
   }

   public String getDefaultSFSBDomain()
   {
      return defaultSFSBDomain;
   }

   public String getDefaultConsumerDomain()
   {
      return defaultConsumerDomain;
   }

   /**
    * The default stateful session bean aspect domain
    *
    * @param defaultSFSBDomain
    */
   public void setDefaultSFSBDomain(String defaultSFSBDomain)
   {
      this.defaultSFSBDomain = defaultSFSBDomain;
   }

   public String getDefaultMDBDomain()
   {
      return defaultMDBDomain;
   }

   /**
    * The default AOP domain for message driven beans.
    *
    * @param defaultMDBDomain
    */
   public void setDefaultMDBDomain(String defaultMDBDomain)
   {
      this.defaultMDBDomain = defaultMDBDomain;
   }

   public String getDefaultServiceDomain()
   {
      return defaultServiceDomain;
   }

   /**
    * default AOP domain for service beans.
    *
    * @param defaultServiceDomain
    */
   public void setDefaultServiceDomain(String defaultServiceDomain)
   {
      this.defaultServiceDomain = defaultServiceDomain;
   }

   protected String getJaccContextId()
   {
      return unit.getShortName();
   }

   /**
    * Get the deployment ejb container for the given ejb name.
    * 
    * @param ejbName the deployment unique ejb name
    * @return the ejb container if found, null otherwise
    * @throws IllegalStateException if the ejbName cannot be used to
    *    for the container name.
    */
   public EJBContainer getEjbContainerForEjbName(String ejbName)
   {
      String ejbObjectName = JavaEEComponentHelper.createObjectName(this, ejbName);
      EJBContainer container = null;
      ObjectName ejbON;
      try
      {
         ejbON = new ObjectName(ejbObjectName);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to ", e);
      }
      container = (EJBContainer) ejbContainers.get(ejbON);
      return container;
   }

   public Container getContainer(ObjectName name)
   {
      return (Container) ejbContainers.get(name);
   }

   public java.util.Map getEjbContainers()
   {
      return ejbContainers;
   }

   @Deprecated
   public List<?> getPersistenceUnitDeployments()
   {
      log.warn("EJBTHREE-1508: Quering Ejb3Deployment for persistence unit deployments is no longer supported");
      return Collections.EMPTY_LIST;
   }

   public EJBContainer getEjbContainer(String ejbLink, Class businessIntf)
   {
      String relativePath = unit.getRelativePath();
      EJBContainer container = deploymentScope.getEjbContainer(ejbLink, businessIntf, relativePath);
      return container;
   }

   public String getEjbJndiName(String ejbLink, Class businessIntf)
   {
      EJBContainer container = getEjbContainer(ejbLink, businessIntf);
      String jndiName = ProxyFactoryHelper.getJndiName(container, businessIntf);
      return jndiName;
   }
   public String getEjbJndiName(Class businessIntf)
      throws NameNotFoundException
   {
      EJBContainer container = getEjbContainer(businessIntf);
      String jndiName = ProxyFactoryHelper.getJndiName(container, businessIntf);
      return jndiName;
   }

   public EJBContainer getEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      String relativePath = unit.getRelativePath();
      return deploymentScope.getEjbContainer(businessIntf, relativePath);
   }

   protected void processEJBContainerMetadata(Container container) throws Exception
   {
      log.trace("Process EJB container metadata " + container);
      ObjectName on = container.getObjectName();
      ejbContainers.put(on, container);
      container.processMetadata();
   }
   
   protected void registerDeployment() throws Exception
   {
      String on = Ejb3Module.BASE_EJB3_JMX_NAME + this.getScopeKernelName();
      if (metaData != null && metaData.getEnterpriseBeans() != null && metaData.getEnterpriseBeans().getEjbJarMetaData() != null)
      {
         String jmxName = metaData.getEnterpriseBeans().getEjbJarMetaData().getJmxName();
         if (jmxName != null && jmxName.trim().length() > 0)
            on = jmxName;
      } 
     
      objectName = new ObjectName(on);
      
      mbeanServer.registerMBean(this, objectName);
   }
   
   protected void unregisterDeployment()
   {
      try
      {
         mbeanServer.unregisterMBean(objectName);
      }
      catch (Exception e)
      {
         log.debug("error trying to stop ejb deployment: " + objectName, e);
      }
   }

   protected void registerEJBContainer(Container container) throws Exception
   {
      ObjectName on = container.getObjectName();
      String name = on.getCanonicalName();
      DependencyPolicy dependsPolicy = container.getDependencyPolicy();
      dependsPolicy.addDependency("jboss.ejb:service=EJBTimerService");
      kernelAbstraction.install(name, dependsPolicy, unit, container);
      mbeanServer.registerMBean(container.getMBean(), on);
      log.debug("Bound ejb3 container " + name);
   }

   protected abstract PolicyConfiguration createPolicyConfiguration() throws Exception;

   protected abstract void putJaccInService(PolicyConfiguration pc, DeploymentUnit unit);

   /**
    * Return the container injection handler collection. If not specified(null)
    * a default handler collection will be created.
    * @return the injection handler collection to use, null if the container
    *    should use a default setup.
    */
   protected Collection<InjectionHandler<Environment>> getHandlers()
   {
      return null;
   }

   /**
    * Create all EJB containers and Persistence Units
    * The only things that should be initialized is metadata that does not need access to any
    * other deployment.  This is because we want the entire EAR to be initialized so that we do not
    * have to guess on dependencies MBean names.  This is because of the silly scoping rules for persistence units
    * and EJBs.
    *
    * @throws Exception
    */
   public void create() throws Exception
   {
      try
      {
         long start = System.currentTimeMillis();

         //pc = createPolicyConfiguration();

         deploy();

         registerDeployment();

         log.debug("EJB3 deployment time took: " + (System.currentTimeMillis() - start));
      }
      catch (Exception e)
      {
         try
         {
            destroy();
         }
         catch (Exception ignored)
         {
            // ignore
         }
         throw e;
      }
   }
   
   protected void reinitialize() throws Exception
   {
      reinitialize = false;
   }

   public void start() throws Exception
   {
     
      if (reinitialize)
         reinitialize();
      
      for (Object o : ejbContainers.values())
      {
         Container con = (Container) o;
         try 
         {
            processEJBContainerMetadata(con);
         }
         catch (Exception e)
         {
            String message = "Exception while processing container metadata for EJB: " + con.getEjbName() + " in unit: " + this.getDeploymentUnit().getShortName();
            // just log the message, no need to dump the stacktrace since we are finally going to
            // throw back the exception
            log.error(message);
            // stop/destroy the container(s) in this deployment
            try 
            {
               stop();
               destroy();
            }
            catch (Exception ignoredException)  
            {
               // we catch this exception during stop/destroy to ensure that this 
               // exception is NOT propagated up, instead of the original exception
               // that forced this stop/destroy
            }
            // now wrap the original exception with a meaningful message and
            // throw back the exception.
            throw new Exception(message, e);
         }
      }

      for (Object o : ejbContainers.values())
      {
         Container con = (Container) o;
         try 
         {
            registerEJBContainer(con);
         }
         catch (Exception e)
         {
            String message = "Exception while registering EJB container for EJB: " + con.getEjbName() + " in unit: " + this.getDeploymentUnit().getShortName();
            // just log the message, no need to dump the stacktrace since we are finally going to
            // throw back the exception
            log.error(message);
            // stop/destroy the container(s) in this deployment
            try 
            {
               stop();
               destroy();
            }
            catch (Exception ignoredException)  
            {
               // we catch this exception during stop/destroy to ensure that this 
               // exception is NOT propagated up, instead of the original exception
               // that forced this stop/destroy
            }
            // now wrap the original exception with a meaningful message and
            // throw back the exception.
            throw new Exception(message, e);

         }
      }

      //putJaccInService(pc, unit);
      
   }
   
   public void stop() //throws Exception
   {
      for (ObjectName on : ejbContainers.keySet())
      {
         try
         {
            mbeanServer.unregisterMBean(on);
            kernelAbstraction.uninstall(on.getCanonicalName());
         }
         catch (Exception e)
         {
            log.debug("error trying to stop ejb container: " + on, e);
         }
      }
      
      reinitialize = true;
   }

   protected void deploy() throws Exception
   {
      if(metaData == null || !metaData.isMetadataComplete())
      {
         Ejb3HandlerFactory factory = Ejb3HandlerFactory.getInstance(this);
         // Scan and deploy
         if (unit.getUrl() != null)
            deployUrl(factory);
   
         // Deploy any classes that have been explicitly marked
         if (unit.getClasses() != null)
         {
            for (Class explicit : unit.getClasses())
            {
               if (explicit.isAnnotationPresent(Entity.class))
               {
                  continue;
               }
               String name = explicit.getName().replace('.', '/') + ".class";
               InputStream stream = explicit.getClassLoader().getResourceAsStream(name);
               deployElement(stream, factory, initialContext);
            }
         }
      }

      // Deploy the beans from the descriptor
      deployBeansFromLib(initialContext);
   }

   protected void deployUrl(Ejb3HandlerFactory factory) throws Exception
   {
      // make sure we are not deploying ejbs from client jar
      List<VirtualFile> clientDescriptors = unit.getResources(new org.jboss.ejb3.ClientDescriptorFileFilter());

      if (clientDescriptors.size() > 0)
         return;

      InitialContext ctx = initialContext;
      // need to look into every entry in the archive to see if anybody has tags
      // defined.
      List<VirtualFile> classes = unit.getResources(new org.jboss.ejb3.ClassFileFilter());
      for (VirtualFile classFile : classes)
      {
         InputStream stream = classFile.openStream();
         deployElement(stream, factory, ctx);
      }
   }

   protected void deployElement(InputStream stream, Ejb3HandlerFactory factory, InitialContext ctx) throws Exception
   {
      DataInputStream dstream = new DataInputStream(new BufferedInputStream(stream));
      ClassFile cf = null;
      try
      {
         cf = new ClassFile(dstream);
      }
      finally
      {
         dstream.close();
         stream.close();
      }

      deployElement(factory, cf, ctx);

   }

   protected void deployBeansFromLib(InitialContext ctx) throws Exception
   {
      JBossMetaData dd = getMetaData();
      if (dd != null)
      {
         Ejb3DescriptorHandler handler = new Ejb3DescriptorHandler(this, dd);
         handler.setCtxProperties(unit.getJndiProperties());

         Map<String, Container> localContainers = new HashMap<String, Container>();
         Iterator<Container> containerIterator = ejbContainers.values().iterator();
         while (containerIterator.hasNext())
         {
            Container container = containerIterator.next();
            localContainers.put(container.getEjbName(), container);
         }

         List<Container> containers = handler.getContainers(this, localContainers);
         for (Container con : containers)
         {
            // EJBContainer has finished with all metadata initialization from XML files and such.
            // this is really a hook to do some processing after XML has been set up and before
            // and processing of dependencies and such.
            try 
            {
               ((EJBContainer) con).instantiated();
               this.ejbContainers.put(con.getObjectName(), con);
               Ejb3Registry.register(con);
               
            } catch (Throwable t)
            {
               throw new DeploymentException(
                     "Error creating ejb container " + con.getEjbName() + ": " + t.getMessage(), t);

            }
         }
      }
   }

   protected void deployElement(Ejb3HandlerFactory factory, ClassFile cf, InitialContext ctx) throws Exception
   {
      Ejb3Handler handler = factory.createHandler(cf);
      handler.setCtxProperties(unit.getJndiProperties());

      if (handler.isEjb() || handler.isJBossBeanType())
      {
         List<Container> containers = handler.getContainers(cf, this);
         for (Container con : containers)
         {
            // EJBContainer has finished with all metadata initialization from XML files and such.
            // this is really a hook to do some processing after XML has been set up and before
            // and processing of dependencies and such.
            try
            {
               ((EJBContainer) con).instantiated();
               this.ejbContainers.put(con.getObjectName(), con);
               Ejb3Registry.register(con);
            }
            catch (Throwable t)
            {
               throw new DeploymentException(
                     "Error creating ejb container " + con.getEjbName() + ": " + t.getMessage(), t);
            }
         }
      }
   }

   public abstract DependencyPolicy createDependencyPolicy(JavaEEComponent component);

   public void destroy() //throws Exception
   {
      try
      {
         undeploy();
         
         unregisterDeployment();
      } 
      catch (Exception e)
      {
         log.debug("error trying to destroy ejb deployment", e);
      }
   }

   private void undeploy()
   {
      for (Container container : ejbContainers.values())
      {
         Ejb3Registry.unregister(container);
      }
   }

   public String resolveEJB(String link, Class<?> beanInterface, String mappedName)
   {
      EjbReference reference = new EjbReference(link, beanInterface.getName(), mappedName);
      return ejbReferenceResolver.resolveEjb(deploymentUnit, reference);
   }
   
   public String resolveMessageDestination(String link)
   {
      return messageDestinationReferenceResolver.resolveMessageDestinationJndiName(deploymentUnit, link);
   }

   protected String resolvePersistenceUnitSupplier(String persistenceUnitName)
   {
      return persistenceUnitDependencyResolver.resolvePersistenceUnitSupplier(deploymentUnit, persistenceUnitName);
   }
   
   /**
    * Do not call, for use in Ejb3Handler.
    * 
    * @param <B>
    * @param ejbName
    * @param enterpriseBeanMetaDataClass
    * @return
    */
   protected <B extends JBossEnterpriseBeanMetaData> B getEnterpriseBeanMetaData(String ejbName, Class<B> enterpriseBeanMetaDataClass)
   {
      if(metaData == null)
         return null;
      
      JBossEnterpriseBeanMetaData result = metaData.getEnterpriseBean(ejbName);
      
      // FIXME: EJBTHREE-1227: temporary workaround for JBCTS-756
      // see also org.jboss.ejb3.metadata.JBossSessionGenericWrapper
      if(result instanceof JBossGenericBeanMetaData)
      {
         log.warn("FIXME: EJBTHREE-1227: JBossGenericBeanMetaData found for '" + ejbName + "' instead of " + enterpriseBeanMetaDataClass);
         if(enterpriseBeanMetaDataClass.equals(JBossSessionBeanMetaData.class))
         {
            result = new JBossSessionGenericWrapper((JBossGenericBeanMetaData) result);
         }
         else if(enterpriseBeanMetaDataClass.equals(JBossMessageDrivenBeanMetaData.class))
         {
            result = new JBossMessageDrivenBeanGenericWrapper((JBossGenericBeanMetaData) result);
         }
         else
         {
            throw new IllegalStateException("Can't find a generic bean meta data wrapper for " + enterpriseBeanMetaDataClass);
         }
      }
      
      return ClassHelper.cast(enterpriseBeanMetaDataClass, result);
   }
   
   /**
    * Get the meta data associated with this deployment or null if none.
    * 
    * @return   meta data or null
    */
   public JBossMetaData getMetaData()
   {
      return metaData;
   }

   public String getName()
   {
      return unit.getShortName();
   }
}
