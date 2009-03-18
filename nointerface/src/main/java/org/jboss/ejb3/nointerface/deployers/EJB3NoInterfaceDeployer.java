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
package org.jboss.ejb3.nointerface.deployers;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ejb.LocalBean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.nointerface.mc.NoInterfaceViewJNDIBinder;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * EJB3NoInterfaceDeployer
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJB3NoInterfaceDeployer extends AbstractDeployer
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(EJB3NoInterfaceDeployer.class);

   /**
    * Constructor
    */
   public EJB3NoInterfaceDeployer()
   {
      setStage(DeploymentStages.REAL);
      addInput(JBossMetaData.class);

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
            // Create view for each bean
            deploy(unit, (JBossSessionBeanMetaData) bean);
         }
      }

   }

   /**
    * Creates a {@link NoInterfaceViewMCBean} for the no-interface view represented by the
    * <code>sessionBeanMetaData</code>. The {@link NoInterfaceViewMCBean} is created only
    * if the bean is eligible for a no-interface view as defined by the EJB3.1 spec
    *
    *
    * @param unit
    * @param sessionBeanMetaData
    * @throws DeploymentException
    */
   private void deploy(DeploymentUnit unit, JBossSessionBeanMetaData sessionBeanMetaData) throws DeploymentException
   {
      try
      {
         if (!isEligibleForNoInterfaceView(unit, sessionBeanMetaData))
         {
            logger.debug("Bean " + sessionBeanMetaData.getEjbClass() + " is not eligible for no-interface view");
            return;
         }
         Class<?> beanClass = Class.forName(sessionBeanMetaData.getEjbClass(), false, unit.getClassLoader());

         String containerMCBeanName = sessionBeanMetaData.getContainerName();
         if (logger.isTraceEnabled())
         {
            logger.trace("Container name for bean " + sessionBeanMetaData.getEjbName() + " in unit " + unit + " is " + containerMCBeanName);
         }
         if (containerMCBeanName == null)
         {
            // The container name is set in the metadata only after the creation of the container
            // However, this deployer does not have an dependency on the creation of a container,
            // so getting the container name from the bean metadata won't work. Need to do a different/better way
            //String containerMCBeanName = sessionBeanMetaData.getContainerName();
            containerMCBeanName = getContainerName(unit, sessionBeanMetaData);

         }

         // The no-interface view needs to be a MC bean so that it can "depend" on the container, so let's
         // make the no-interface view a MC bean
         NoInterfaceViewJNDIBinder bean = NoInterfaceViewJNDIBinder.getNoInterfaceViewJndiBinder(beanClass,
               sessionBeanMetaData);
         String noInterfaceViewMCBeanName = sessionBeanMetaData.getEjbName() + "@" + ((Object) bean).toString();
         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(noInterfaceViewMCBeanName, bean.getClass()
               .getName());
         builder.setConstructorValue(bean);

         ValueMetaData inject = builder.createInject(containerMCBeanName, null, null, ControllerState.DESCRIBED);
         // Too bad we have to know the field name. Need to do more research on MC to see if we can
         // add property metadata based on type instead of field name.
         builder.addPropertyMetaData("container", inject);
         
         // for SFSB we also need to inject the StatefulSessionFactory (which at the moment is 
         // available at the same containerMCBeanName and is infact the container)
         if (sessionBeanMetaData.isStateful())
         {
            // inject the container (=StatefulSessionFactory)
            builder.addPropertyMetaData("statefulSessionFactory", inject);
         }

         logger.info("Added injection on " + bean + " for container " + containerMCBeanName + " cl set to "
               + unit.getClassLoader());

         // Add this as an attachment
         unit.addAttachment(BeanMetaData.class + ":" + noInterfaceViewMCBeanName, builder.getBeanMetaData());

      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Could not create no-interface view for "
               + sessionBeanMetaData.getEjbClass() + " in unit " + unit.getName(), t);
      }
   }

   /**
    * Undeploy
    * @param unit
    * @param deployment
    */
   public void undeploy(DeploymentUnit unit, JBossMetaData deployment)
   {
      // TODO Needs implementation

   }

   /**
    * See section 4.9.8 (bullet 1) of the EJB3.1 spec for eligibility of
    * a bean for no-interface view
    *
    * @param sessionBeanMetadata
    * @return
    */
   protected boolean isEligibleForNoInterfaceView(DeploymentUnit unit, JBossSessionBeanMetaData sessionBeanMetadata)
         throws Exception
   {

      // if the bean has a @LocalBean defined, then it qualifies for a no-interface view
      // irrespective of the other rules.
      //TODO: The JBMETA does not yet support @LocalBean so let's HACK it for now
      String ejbClassName = sessionBeanMetadata.getEjbClass();
      Class<?> beanClass = Class.forName(ejbClassName, false, unit.getClassLoader());
      if (beanClass.getAnnotation(LocalBean.class) != null) //sessionBeanMetadata.getLocalBean())
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Bean class " + ejbClassName + " in unit " + unit + " is marked as @LocalBean");
         }
         return true;
      }

      // If there are any local business interfaces then its not eligible
      if (sessionBeanMetadata.getBusinessLocals() != null && !sessionBeanMetadata.getBusinessLocals().isEmpty())
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Bean " + sessionBeanMetadata.getEjbClass()
                  + " has business local, hence not eligible for no-interface view");
         }
         return false;
      }

      // If there are any remote business interfaces then its not eligible
      if (sessionBeanMetadata.getBusinessRemotes() != null && !sessionBeanMetadata.getBusinessRemotes().isEmpty())
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Bean " + sessionBeanMetadata.getEjbClass()
                  + " has business remote, hence not eligible for no-interface view");
         }

         return false;
      }

      // If it has a 2.x home or local home view, then its not eligible
      if (sessionBeanMetadata.getHome() != null || sessionBeanMetadata.getLocalHome() != null)
      {
         if (logger.isTraceEnabled())
         {
            logger.trace("Bean " + sessionBeanMetadata.getEjbClass()
                  + " has 2.x home/local-home, hence not eligible for no-interface view");
         }

         return false;
      }

      // Check if the bean implements any interfaces
      if (doesBeanImplementAnyInterfaces(beanClass))
      {
         if (logger.isTraceEnabled())
         {
            logger
                  .trace("Bean "
                        + sessionBeanMetadata.getEjbClass()
                        + " implements interfaces (other than the one's excluded as per section 4.9.8 of EJB3.1 spec), hence not eligible for no-interface view");
         }
         return false;
      }
      // The bean satisfies the pre-requisites of a no-interface view.
      return true;

   }

   /**
    * Checks whether the bean class implements any interfaces other than
    * {@link Serializable} or {@link Externalizable} or anything from javax.ejb.* packages.
    *
    * @param beanClass
    * @return Returns true if the bean implements any interface(s) other than {@link Serializable}
    *           or {@link Externalizable} or anything from javax.ejb.* packages.
    * @throws DeploymentException
    */
   protected boolean doesBeanImplementAnyInterfaces(Class<?> beanClass) throws DeploymentException
   {
      Class<?>[] interfaces = beanClass.getInterfaces();
      if (interfaces.length == 0)
      {
         return false;
      }

      // As per section 4.9.8 (bullet 1.3) of EJB3.1 spec
      // java.io.Serializable; java.io.Externalizable; any of the interfaces defined by the javax.ejb
      // are excluded from interface check
      
      // Impl detail : We need an ArrayList because it supports removing of elements through iterator, while
      // iterating. The List returned through Arrays.asList(...) does not allow this and throws UnsupportedException
      List<Class<?>> implementedInterfaces = new ArrayList<Class<?>>(Arrays.asList(interfaces));
      Iterator<Class<?>> implementedInterfacesIterator = implementedInterfaces.iterator();
      while (implementedInterfacesIterator.hasNext())
      {
         Class<?> implementedInterface = implementedInterfacesIterator.next();
         if (implementedInterface.equals(java.io.Serializable.class)
               || implementedInterface.equals(java.io.Externalizable.class)
               || implementedInterface.getName().startsWith("javax.ejb."))
         {
            implementedInterfacesIterator.remove();
         }
      }
      // Now that we have removed the interfaces that should be excluded from the check,
      // if the implementedInterfaces collection is empty then this bean can be considered for no-interface view
      return !implementedInterfaces.isEmpty();
   }

   /**
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
