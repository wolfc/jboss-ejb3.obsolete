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
import java.net.URL;
import java.util.ArrayList;
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
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;

import org.hibernate.cfg.EJB3DTDEntityResolver;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.jboss.ejb3.enc.EjbModuleEjbResolver;
import org.jboss.ejb3.enc.EjbModulePersistenceUnitResolver;
import org.jboss.ejb3.entity.PersistenceUnitDeployment;
import org.jboss.ejb3.entity.PersistenceXmlLoader;
import org.jboss.ejb3.metamodel.EjbJarDD;
import org.jboss.ejb3.metamodel.EjbJarDDObjectFactory;
import org.jboss.ejb3.metamodel.JBossDDObjectFactory;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * An EjbModule represents a collection of beans that are deployed as a unit.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public abstract class Ejb3Deployment
{

   private static final Logger log = Logger.getLogger(Ejb3Deployment.class);
   public static final String ACTUAL_ENTITY_MANAGER_FACTORY_CONTEXT = "java:/ActualEntityManagerFactories";
   public static final String MANAGED_ENTITY_FACTORY_CONTEXT = "java:/managedEntityFactories";

   protected DeploymentUnit unit;

   protected LinkedHashMap<ObjectName, Container> ejbContainers = new LinkedHashMap<ObjectName, Container>();

   protected boolean hasEntities;
   protected List<String> explicitEntityClasses = new ArrayList<String>();

   protected List<PersistenceUnitDeployment> persistenceUnitDeployments = new ArrayList<PersistenceUnitDeployment>();
;

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
   protected EjbModuleEjbResolver ejbRefResolver;
   protected EjbModulePersistenceUnitResolver persistenceUnitResolver;

   //The JACC PolicyConfiguration
   PolicyConfiguration pc;

   public Ejb3Deployment(DeploymentUnit unit, DeploymentScope deploymentScope)
   {
      this.unit = unit;
      this.deploymentScope = deploymentScope;
      try
      {
         initialContext = EJB3Util.getInitialContext(unit.getJndiProperties());
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      ejbRefResolver = new EjbModuleEjbResolver(deploymentScope, unit.getShortName(), ejbContainers, this);
      persistenceUnitResolver = new EjbModulePersistenceUnitResolver(persistenceUnitDeployments, deploymentScope, ejbContainers);
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


   /**
    * Returns a partial MBean attribute name of the form
    * ",ear=foo.ear,jar=foo.jar"
    *
    * @return
    */
   public String getScopeKernelName()
   {
      String scopedKernelName = "";
      if (deploymentScope != null) scopedKernelName += ",ear=" + deploymentScope.getShortName();
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

   public Container getContainer(ObjectName name)
   {
      return (Container) ejbContainers.get(name);
   }

   public java.util.Map getEjbContainers()
   {
      return ejbContainers;
   }


   public PersistenceUnitDeployment getPersistenceUnitDeployment(String unitName) throws NameNotFoundException
   {
      return persistenceUnitResolver.getPersistenceUnitDeployment(unitName);
   }

   public PersistenceUnitDeployment getPersistenceUnitDeploymentInternal(String unitName)
   {
      return persistenceUnitResolver.getPersistenceUnitDeploymentInternal(unitName);
   }

   public List<PersistenceUnitDeployment> getPersistenceUnitDeployments()
   {
      return persistenceUnitDeployments;
   }


   public EJBContainer getEjbContainer(String ejbLink, Class businessIntf)
   {
      return ejbRefResolver.getEjbContainer(ejbLink, businessIntf);
   }

   public String getEjbJndiName(String ejbLink, Class businessIntf)
   {
      return ejbRefResolver.getEjbJndiName(ejbLink, businessIntf);
   }

   public EJBContainer getEjbContainer(Ejb3Deployment deployment, Class businessIntf) throws NameNotFoundException
   {
      return ejbRefResolver.getEjbContainer(deployment, businessIntf);
   }

   public EJBContainer getEjbContainer(Class businessIntf) throws NameNotFoundException
   {
      return ejbRefResolver.getEjbContainer(businessIntf);
   }

   public String getEjbJndiName(Class businessIntf) throws NameNotFoundException
   {
      return ejbRefResolver.getEjbJndiName(businessIntf);
   }

   protected void processEJBContainerMetadata(Container container)
           throws Exception
   {
      ObjectName on = container.getObjectName();
      ejbContainers.put(on, container);
      DependencyPolicy policy = createDependencyPolicy();
      container.processMetadata(policy);

   }

   protected void registerEJBContainer(Container container)
           throws Exception
   {
      ObjectName on = container.getObjectName();
      String name = on.getCanonicalName();
      kernelAbstraction.install(name, container.getDependencyPolicy(), container);
      log.debug("Bound ejb3 container " + name);
   }

   protected abstract PolicyConfiguration createPolicyConfiguration() throws Exception;

   protected abstract void putJaccInService(PolicyConfiguration pc, DeploymentUnit unit);


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
      long start = System.currentTimeMillis();

      pc = createPolicyConfiguration();

      deploy();

      initializePersistenceUnits();

      log.debug("EJB3 deployment time took: "
              + (System.currentTimeMillis() - start));
   }

   public void start() throws Exception
   {
      try
      {
         startPersistenceUnits();

         for (Object o : ejbContainers.values())
         {
            Container con = (Container) o;
            processEJBContainerMetadata(con);
         }

         for (Object o : ejbContainers.values())
         {
            Container con = (Container) o;
            registerEJBContainer(con);
         }

         putJaccInService(pc, unit);
      }
      catch (Exception ex)
      {
         try
         {
            stop();
            destroy();
         }
         catch (Exception ignored)
         {
         }
         throw ex;
      }
   }

   protected void deploy() throws Exception
   {
      Ejb3HandlerFactory factory = Ejb3HandlerFactory.getInstance(this);
      if (unit.getUrl() != null) deployUrl(factory);

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
      
      deployBeansFromLib(initialContext);
   }

   protected void deployUrl(Ejb3HandlerFactory factory)
           throws Exception
   {
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

   protected void deployElement(InputStream stream, Ejb3HandlerFactory factory, InitialContext ctx)
           throws Exception
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
   
   protected void deployBeansFromLib(InitialContext ctx)
   throws Exception
   {
      EjbJarDD dd = EjbJarDDObjectFactory.parse(getDeploymentUnit().getEjbJarXml());
      dd = JBossDDObjectFactory.parse(this.getDeploymentUnit().getJbossXml(), dd);
      
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
            ((EJBContainer) con).instantiated();
            this.ejbContainers.put(con.getObjectName(), con);
            Ejb3Registry.register(con);
         }
      }
   }

   protected void deployElement(Ejb3HandlerFactory factory, ClassFile cf, InitialContext ctx)
           throws Exception
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
            ((EJBContainer) con).instantiated();
            this.ejbContainers.put(con.getObjectName(), con);
            Ejb3Registry.register(con);
         }
      }
   }

   protected void initializePersistenceUnits()
           throws Exception
   {
      URL persistenceXmlUrl = null;
      persistenceXmlUrl = unit.getPersistenceXml();
 
      hasEntities = persistenceXmlUrl != null;

      if (!hasEntities) return;

      if (unit.getClasses() != null)
      {
         for (Class explicit : unit.getClasses())
         {
            if (explicit.isAnnotationPresent(Entity.class))
            {
               explicitEntityClasses.add(explicit.getName());
            }
         }
      }

      // scope the unitName if this is an ejb archive
      // todo revert to this: List<PersistenceMetadata> persistenceMetadata = PersistenceXmlLoader.deploy(persistenceXmlUrl, new HashMap(), new EJB3DTDEntityResolver());
      List<PersistenceMetadata> persistenceMetadata = PersistenceXmlLoader.deploy(persistenceXmlUrl, new HashMap(), new EJB3DTDEntityResolver(), PersistenceUnitTransactionType.JTA);
      for (PersistenceMetadata metadata : persistenceMetadata)
      {
         String earShortName = deploymentScope == null ? null : deploymentScope.getShortName();
         boolean isScoped = ejbContainers.size() > 0;
         PersistenceUnitDeployment deployment = new PersistenceUnitDeployment(initialContext, this, explicitEntityClasses, persistenceXmlUrl, metadata, earShortName, unit.getShortName(), isScoped);
         PersistenceUnitRegistry.register(deployment);
         persistenceUnitDeployments.add(deployment);
      }
   }

   public abstract DependencyPolicy createDependencyPolicy();

   protected void startPersistenceUnits()
   {
      if (persistenceUnitDeployments == null) return;

      for (PersistenceUnitDeployment entityDeployment : persistenceUnitDeployments)
      {
         if (entityDeployment != null)
         {
            DependencyPolicy policy = createDependencyPolicy();
            entityDeployment.addDependencies(policy);
            kernelAbstraction.install(entityDeployment.getKernelName(), policy, entityDeployment);
         }
      }
   }

   protected void stopPersistenceUnits()
   {
      if (persistenceUnitDeployments == null) return;

      for (PersistenceUnitDeployment entityDeployment : persistenceUnitDeployments)
      {
         try
         {
            PersistenceUnitRegistry.unregister(entityDeployment);
            if (entityDeployment != null)
            {
               kernelAbstraction.uninstall(entityDeployment.getKernelName());
            }
         }
         catch (Exception e)
         {
            log.debug("error trying to shut down persistence unit", e);
         }
      }

   }

   public void stop() throws Exception
   {
      for (Object o : ejbContainers.keySet())
      {
         try
         {
            ObjectName on = (ObjectName) o;
            kernelAbstraction.uninstall(on.getCanonicalName());
         }
         catch (Exception e)
         {
            log.debug("error trying to shut down ejb container", e);
         }
      }
      stopPersistenceUnits();
   }

   public void destroy() throws Exception
   {
      undeploy();
      
      PolicyConfigurationFactory pcFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
      PolicyConfiguration pc = pcFactory.getPolicyConfiguration(getJaccContextId(), true);
      pc.delete();
   }
      
   private void undeploy()
   {
      for(Container container : ejbContainers.values())
      {
         Ejb3Registry.unregister(container);
      }
   }
}
