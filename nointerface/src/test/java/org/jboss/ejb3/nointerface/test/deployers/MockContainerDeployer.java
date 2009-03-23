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
package org.jboss.ejb3.nointerface.test.deployers;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.nointerface.test.common.MockStatefulContainer;
import org.jboss.ejb3.nointerface.test.common.MockStatelessContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * MockContainerDeployer
 *
 * A deployer (to be used ONLY for testing) which creates mock containers
 * for session beans. The mock containers are installed into MC.
 *
 *
 * @see MockStatefulContainer
 * @see MockContainer
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MockContainerDeployer extends AbstractDeployer
{

   private static Logger logger = Logger.getLogger(MockContainerDeployer.class);

   public MockContainerDeployer()
   {
      setInput(JBossMetaData.class);
      // the Endpoint (container) will be installed as output
      addOutput(BeanMetaData.class);

   }

   /**
    * Deploy the deployment unit
    */
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Deploying unit " + unit.getName());
      }
      JBossMetaData metaData = unit.getAttachment(JBossMetaData.class);
      if (metaData == null)
      {
         if (logger.isTraceEnabled())
            logger.trace("No JBossMetadata for unit : " + unit.getName());
         return;
      }
      // work on the ejbs
      JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
      for (JBossEnterpriseBeanMetaData bean : beans)
      {
         if (bean.isSession())
         {
            if (logger.isTraceEnabled())
            {
               logger.trace("Found bean of type session: " + bean.getEjbClass() + " in unit " + unit.getName());
            }
            // Create a container for each bean
            deploy(unit, (JBossSessionBeanMetaData) bean);
         }
      }

   }

   private void deploy(DeploymentUnit unit, JBossSessionBeanMetaData sessionBeanMetadata) throws DeploymentException
   {
      try
      {
         String beanClassName = sessionBeanMetadata.getEjbClass();
         Endpoint container = null;
         if (sessionBeanMetadata.isStateful())
         {
            container = new MockStatefulContainer(Class.forName(beanClassName, false, unit.getClassLoader()));
         }
         else if (sessionBeanMetadata.isStateless())
         {
            container = new MockStatelessContainer(Class.forName(beanClassName, false, unit.getClassLoader()));
         }
         else
         {
            logger.error("Bean " + beanClassName + " is neither stateful nor stateless, cannot create a container");
            throw new DeploymentException("Bean " + beanClassName + " is of unrecognized type");
         }

         // install the container in MC. Getting the container name is currently duplicated (copied from)
         // Ejb3NoInterfaceDeployer.
         String containerName = getContainerName(unit, sessionBeanMetadata);
         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(containerName, container.getClass().getName());
         builder.setConstructorValue(container);

         unit.addAttachment(BeanMetaData.class + ":" + containerName, builder.getBeanMetaData());

      }
      catch (Exception e)
      {
         logger.error("Error in " + MockContainerDeployer.class + " while creating a container for unit " + unit, e);
         DeploymentException.rethrowAsDeploymentException("Could not create container for unit " + unit, e);
      }
   }

   /**
    *
    * Ultimately, the container name should come from the <code>sessionBeanMetadata</code>.
    * However because of the current behaviour where the container on its start sets the containername
    * in the metadata, its not possible to get this information even before the container is started.
    *
    * Hence let's for the time being create the container name from all the information that we have
    * in the <code>unit</code>
    *
    * @param unit The deployment unit
    * @param sessionBeanMetadata Session bean metadata
    * @return Returns the container name for the bean corresponding to the <code>sessionBeanMetadata</code> in the <code>unit</code>
    *
    * @throws MalformedObjectNameException
    */
   private String getContainerName(DeploymentUnit unit, JBossSessionBeanMetaData sessionBeanMetadata)
         throws MalformedObjectNameException
   {
      // TODO the base ejb3 jmx object name comes from Ejb3Module.BASE_EJB3_JMX_NAME, but
      // we don't need any reference to ejb3-core. Right now just hard code here, we need
      // a better way/place for this later
      StringBuilder containerName = new StringBuilder("jboss.j2ee:service=EJB3" + ",");

      // Get the top level unit for this unit (ex: the top level might be an ear and this unit might be the jar
      // in that ear
      DeploymentUnit toplevelUnit = unit.getTopLevel();
      if (toplevelUnit != null)
      {
         // if top level is an ear, then create the name with the ear reference
         if (isEar(toplevelUnit))
         {
            containerName.append("ear=");
            containerName.append(toplevelUnit.getSimpleName());
            containerName.append(",");

         }
      }
      // now work on the passed unit, to get the jar name
      if (unit.getSimpleName() == null)
      {
         containerName.append("*");
      }
      else
      {
         containerName.append("jar=");
         containerName.append(unit.getSimpleName());
      }
      // now the ejbname
      containerName.append(",name=");
      containerName.append(sessionBeanMetadata.getEjbName());

      if (logger.isTraceEnabled())
      {
         logger.trace("Container name generated for ejb = " + sessionBeanMetadata.getEjbName() + " in unit " + unit
               + " is " + containerName);
      }
      ObjectName containerJMXName = new ObjectName(containerName.toString());
      return containerJMXName.getCanonicalName();
   }

   /**
    * Returns true if this <code>unit</code> represents an .ear deployment
    *
    * @param unit
    * @return
    */
   private boolean isEar(DeploymentUnit unit)
   {
      return unit.getSimpleName().endsWith(".ear");
   }

}
