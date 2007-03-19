/*
* JBoss, Home of Professional Open Source
* Copyright 2005, Red Hat Middleware LLC., and individual contributors as indicated
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

import org.jboss.deployers.plugins.deployer.AbstractSimpleDeployer;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.DeploymentContext;
import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.EJB3Deployer;
import org.jboss.kernel.Kernel;
import org.jboss.virtual.VirtualFile;
import org.jboss.logging.Logger;

import javax.management.MBeanServer;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.List;

/**
 * Creates initial EJB deployments and initializes only basic metadata.
 * A registration process is required so that
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57082 $
 */
public class EJBRegistrationDeployer extends AbstractSimpleDeployer
{
   private static final Logger log = Logger.getLogger(EJBRegistrationDeployer.class);

   private HashSet ignoredJarsSet;
   private MBeanServer mbeanServer;
   private Kernel kernel;
   private Properties defaultPersistenceProperties;
   private List<String> allowedSuffixes;

   public EJBRegistrationDeployer()
   {
      // make sure we run before the stage 2 deployer
      // TODO: what's is the proper relative order?
      setRelativeOrder(COMPONENT_DEPLOYER - 1);
   }
   
   public List<String> getAllowedSuffixes()
   {
      return allowedSuffixes;
   }

   public void setAllowedSuffixes(List<String> allowedSuffixes)
   {
      this.allowedSuffixes = allowedSuffixes;
   }

   public HashSet getIgnoredJarsSet()
   {
      return ignoredJarsSet;
   }

   public void setIgnoredJarsSet(HashSet ignoredJarsSet)
   {
      this.ignoredJarsSet = ignoredJarsSet;
   }

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public Kernel getKernel()
   {
      return kernel;
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   public Properties getDefaultPersistenceProperties()
   {
      return defaultPersistenceProperties;
   }

   public void setDefaultPersistenceProperties(Properties defaultPersistenceProperties)
   {
      this.defaultPersistenceProperties = defaultPersistenceProperties;
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      try
      {
         if (unit.getDeploymentContext().isComponent()) return;
         VirtualFile jar = unit.getDeploymentContext().getRoot();
         if (jar.isLeaf() || ignoredJarsSet.contains(jar.getName())                 )
         {
            log.trace("EJBRegistrationDeployer ignoring: " + jar.getName());
            return;
         }
         if(!hasAllowedSuffix(jar.getName()))
         {
            log.trace("EJBRegistrationDeployer suffix not allowed: " + jar.getName());
            return;
         }
         log.debug("********* EJBRegistrationDepoyer Begin Unit: " + unit.getName() + " jar: " + jar.getName());
         VirtualFile ejbjar = unit.getMetaDataFile("ejb-jar.xml");
         if (ejbjar != null)
         {
            InputStream is = ejbjar.openStream();
            boolean has30EjbJarXml = EJB3Deployer.has30EjbJarXml(is);
            is.close();
            if (!has30EjbJarXml) return;
         }
         DeploymentScope scope = null;
         DeploymentContext parent = unit.getDeploymentContext().getParent();
         if (parent != null && parent.getRoot().getName().endsWith(".ear")) // todo should look for metadata instead of ".ear"
         {
            scope = parent.getTransientAttachments().getAttachment(DeploymentScope.class);
            if (scope == null)
            {
               scope = new JBoss5DeploymentScope(unit.getDeploymentContext().getParent());
               parent.getTransientAttachments().addAttachment(DeploymentScope.class, scope);
            }
         }
         JBoss5DeploymentUnit du = new JBoss5DeploymentUnit(unit);
         du.setDefaultPersistenceProperties(defaultPersistenceProperties);
         Ejb3JBoss5Deployment deployment = new Ejb3JBoss5Deployment(du, kernel, mbeanServer, unit, scope);
         if (scope != null) scope.register(deployment);
         // create() creates initial EJB containers and initializes metadata.
         deployment.create();
         if (deployment.getEjbContainers().size() == 0 && deployment.getPersistenceUnitDeployments().size() == 0)
         {
            log.trace("EJBRegistrationDeployer no containers in scanned jar, consider adding it to the ignore list: " + jar.getName() + " url: " + jar.toURL() + " unit: " + unit.getName());
            return;
         }
         unit.addAttachment(Ejb3Deployment.class, deployment);
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
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
   
   public void undeploy(DeploymentUnit unit)
   {
      Ejb3Deployment deployment = unit.getAttachment(Ejb3Deployment.class);
      if (deployment == null) return;
      try
      {
         deployment.stop();
      }
      catch (Exception e)
      {
         log.error("failed to stop deployment", e);
      }
      try
      {
         deployment.destroy();
      }
      catch (Exception e)
      {
         log.error("failed to destroy deployment", e);
      }
   }

}
