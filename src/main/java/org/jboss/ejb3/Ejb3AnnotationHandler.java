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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.aop.AspectManager;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.ejb3.mdb.ConsumerContainer;
import org.jboss.ejb3.mdb.MDB;
import org.jboss.ejb3.service.ServiceContainer;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossConsumerBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @author <a href="mailto:bill@jboss.com">Bill Burke</a>
 * @version <tt>$Revision$</tt>
 */
public class Ejb3AnnotationHandler implements Ejb3Handler
{
   private static final Logger log = Logger.getLogger(Ejb3AnnotationHandler.class);

   protected static enum EJB_TYPE
   {
      STATELESS, STATEFUL, MESSAGE_DRIVEN, ENTITY, SERVICE, CONSUMER
   }

   protected DeploymentUnit di;

   protected ClassFile cf;
   protected List<String> ejbNames = new ArrayList<String>();
   protected Class<?> ejbClass;
   protected String className;
   protected EJB_TYPE ejbType;
   protected Annotation annotation;
   protected AnnotationsAttribute visible;
   protected Hashtable ctxProperties;
   protected String defaultSLSBDomain;
   protected String defaultSFSBDomain;
   protected String defaultMDBDomain;
   protected String defaultServiceDomain;
   protected String defaultConsumerDomain;
   protected Ejb3Deployment deployment;

   public Ejb3AnnotationHandler(Ejb3Deployment deployment)
   {
      this.deployment = deployment;
      this.di = deployment.getDeploymentUnit();
      defaultSLSBDomain = deployment.getDefaultSLSBDomain();
      defaultSFSBDomain = deployment.getDefaultSFSBDomain();
      defaultMDBDomain = deployment.getDefaultMDBDomain();
      defaultServiceDomain = deployment.getDefaultServiceDomain();
      defaultConsumerDomain = deployment.getDefaultConsumerDomain();
   }

   public Ejb3AnnotationHandler(Ejb3Deployment deployment, ClassFile cf)
   {
      this.deployment = deployment;
      this.di = deployment.getDeploymentUnit();
      defaultSLSBDomain = deployment.getDefaultSLSBDomain();
      defaultSFSBDomain = deployment.getDefaultSFSBDomain();
      defaultMDBDomain = deployment.getDefaultMDBDomain();
      defaultServiceDomain = deployment.getDefaultServiceDomain();
      defaultConsumerDomain = deployment.getDefaultConsumerDomain();
      
      this.cf = cf;
      className = cf.getName();
      visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
   }

   /**
    * The link from an enterprise bean to it's interceptors is via the EJB Jar meta data.
    * So we try to recreate that link it's not already established.
    * 
    * Because of a bug in jboss-metadata an annotated bean will not always
    * get some meta data, so we create some for it.
    * 
    * TODO: JBMETA-4
    * 
    * @param <M>
    * @param deployment
    * @param ejbName
    * @param enterpriseBeanMetaDataClass
    * @return
    */
   private <M extends JBossEnterpriseBeanMetaData> M getEnterpriseBeanMetaData(Ejb3Deployment deployment, String ejbName, Class<M> enterpriseBeanMetaDataClass)
   {
      JBossMetaData ejbJarMetaData = deployment.getMetaData();
      // If there is no meta data at all, don't establish the link.
      if(ejbJarMetaData == null)
         return null;
      
      if(!ejbJarMetaData.isEJB3x())
         throw new IllegalStateException("Attempt to process non-EJB3.x EJB-JAR metadata as EJB3.x for deployment " + deployment.getName());
      
      M beanMetaData = deployment.getEnterpriseBeanMetaData(ejbName, enterpriseBeanMetaDataClass);
      if(beanMetaData == null)
      {
         log.warn("JBMETA-4: did not find any bean meta data for annotation bean " + ejbName + ", will create some");
         try
         {
            beanMetaData = enterpriseBeanMetaDataClass.newInstance();
         }
         catch (InstantiationException e)
         {
            throw new RuntimeException();
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException();
         }
         beanMetaData.setEjbName(ejbName);
         JBossEnterpriseBeansMetaData enterpriseBeans = ejbJarMetaData.getEnterpriseBeans();
         if(enterpriseBeans == null)
         {
            enterpriseBeans = new JBossEnterpriseBeansMetaData();
            ejbJarMetaData.setEnterpriseBeans(enterpriseBeans);
         }
         enterpriseBeans.add(beanMetaData);
      }
      return beanMetaData;
   }
   
   public void setCtxProperties(Hashtable ctxProperties)
   {
      this.ctxProperties = ctxProperties;
   }

   protected String getJaccContextId()
   {
      return di.getShortName();
   }

   public boolean isEjb()
   {
      if (visible == null) return false;

      if (EJB3Util.isStateless(visible)) return true;
      if (EJB3Util.isMessageDriven(visible)) return true;
      if (EJB3Util.isStatefulSession(visible)) return true;
      return false;
   }

   public boolean isJBossBeanType()
   {
      if (visible == null) return false;

      if (EJB3Util.isService(visible)) return true;
      if (EJB3Util.isConsumer(visible)) return true;
      return false;
   }

   public List getContainers(ClassFile cf, Ejb3Deployment deployment) throws Exception
   {
      List<EJBContainer> containers = new ArrayList<EJBContainer>();

      populateBaseInfo();

      // TODO: because Ejb3Deployment.deploy will first do annotation deployments
      // and then meta data deployments, bean meta data will never be attached to
      // the container. So we do it here.
      for (int ejbIndex = 0; ejbIndex < ejbNames.size(); ++ejbIndex)
      {
         String ejbName = ejbNames.get(ejbIndex);
         if (ejbType == EJB_TYPE.STATELESS)
         {
            EJBContainer container = getStatelessContainer(ejbIndex, getEnterpriseBeanMetaData(deployment, ejbName, JBossSessionBeanMetaData.class));
            container.setJaccContextId(getJaccContextId());
            containers.add(container);
         }
         else if (ejbType == EJB_TYPE.STATEFUL)
         {
            StatefulContainer container = getStatefulContainer(ejbIndex, getEnterpriseBeanMetaData(deployment, ejbName, JBossSessionBeanMetaData.class));
            container.setJaccContextId(getJaccContextId());
            containers.add(container);
         }
         else if (ejbType == EJB_TYPE.MESSAGE_DRIVEN)
         {
            MDB container = getMDB(ejbIndex, getEnterpriseBeanMetaData(deployment, ejbName, JBossMessageDrivenBeanMetaData.class));
            validateMDBTransactionAttribute(container);
            container.setJaccContextId(getJaccContextId());
            containers.add(container);
         }
         else if (ejbType == EJB_TYPE.SERVICE)
         {
            ServiceContainer container = getServiceContainer(ejbIndex, getEnterpriseBeanMetaData(deployment, ejbName, JBossServiceBeanMetaData.class));
            container.setJaccContextId(getJaccContextId());
            containers.add(container);
         }
         else if (ejbType == EJB_TYPE.CONSUMER)
         {
            ConsumerContainer container = getConsumerContainer(ejbIndex, getEnterpriseBeanMetaData(deployment, ejbName, JBossConsumerBeanMetaData.class));
            container.setJaccContextId(getJaccContextId());
            containers.add(container);
         }
         log.debug("found EJB3: ejbName=" + ejbName + ", class=" + className + ", type=" + ejbType);
      }

      return containers;
   }
   
   protected void validateMDBTransactionAttribute(MDB mdb)
   {
      TransactionAttribute tx = (TransactionAttribute)mdb.resolveAnnotation(TransactionAttribute.class); 
      if (tx != null)
      {
         TransactionAttributeType type = tx.value();
         if (type != TransactionAttributeType.REQUIRED && type != TransactionAttributeType.NOT_SUPPORTED)
            throw new RuntimeException("MDB " + mdb.getEjbName() + " has an invalid TransactionAttribute: " + type + 
                  ". Only REQUIRED and NOT_SUPPORTED are valid");
      }
   }
   
   protected String getAspectDomain(int ejbIndex, String defaultDomain)
   {
      return EJB3Util.getAspectDomain(visible, defaultDomain);
   }

   protected ServiceContainer getServiceContainer(int ejbIndex, JBossServiceBeanMetaData beanMetaData) throws Exception
   {
      String containerName = getAspectDomain(ejbIndex, defaultServiceDomain);
      DomainDefinition domain = AspectManager.instance().getContainer(containerName);

      if (domain == null)
         throw new RuntimeException("No container configured with name '"
                 + containerName + "''");

      return new ServiceContainer(deployment.getMbeanServer(), di.getClassLoader(), className,
              ejbNames.get(ejbIndex), (Domain) domain.getManager(), ctxProperties,
              deployment, beanMetaData);

   }

   protected ConsumerContainer getConsumerContainer(int ejbIndex, JBossConsumerBeanMetaData beanMetaData) throws Exception
   {
      String containerName = getAspectDomain(ejbIndex, defaultConsumerDomain);
      DomainDefinition domain = AspectManager.instance().getContainer(containerName);

      if (domain == null)
         throw new RuntimeException("No container configured with name '"
                 + containerName + "''");

      return new ConsumerContainer(ejbNames.get(ejbIndex), (Domain) domain.getManager(),
              di.getClassLoader(), className, ctxProperties,
              deployment, beanMetaData);

   }

   protected StatefulContainer getStatefulContainer(int ejbIndex, JBossSessionBeanMetaData beanMetaData) throws Exception
   {
      String containerName = getAspectDomain(ejbIndex, defaultSFSBDomain);
      DomainDefinition domain = AspectManager.instance().getContainer(containerName);

      if (domain == null)
         throw new RuntimeException("No container configured with name '"
                 + containerName + "''");

      return new StatefulContainer(di.getClassLoader(), className,
              ejbNames.get(ejbIndex), (Domain) domain.getManager(), ctxProperties,
              deployment, beanMetaData);

   }

   protected EJBContainer getStatelessContainer(int ejbIndex, JBossSessionBeanMetaData beanMetaData) throws Exception
   {
      String containerName = getAspectDomain(ejbIndex, defaultSLSBDomain);
      
      DomainDefinition domain = AspectManager.instance().getContainer(containerName);

      if (domain == null)
         throw new RuntimeException("No container configured with name '"
                 + containerName + "''");

      return new StatelessContainer(di.getClassLoader(), className,
              ejbNames.get(ejbIndex), (Domain) domain.getManager(),
              ctxProperties, deployment, beanMetaData);
   }

   protected String getMDBDomainName(int ejbIndex)
   {
      return defaultMDBDomain;
   }

   protected void createProxyFactories()
   {

   }

   protected MDB getMDB(int ejbIndex, JBossMessageDrivenBeanMetaData beanMetaData) throws Exception
   {
      String domainName = getMDBDomainName(ejbIndex);
      
      String containerName = getAspectDomain(ejbIndex, domainName);
      DomainDefinition domain = AspectManager.instance().getContainer(containerName);

      if (domain == null)
         throw new RuntimeException("No container configured with name '"
                 + containerName + "''");

      MDB container = new MDB(ejbNames.get(ejbIndex), (Domain) domain.getManager(), di.getClassLoader(), className,
              ctxProperties, deployment, beanMetaData);

      return container;
   }

   protected void populateBaseInfo() throws Exception
   {
      String ejbName = null;
      ejbClass = di.getClassLoader().loadClass(className);

      visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);

      if (visible != null)
      {
         annotation = visible.getAnnotation(javax.ejb.Stateless.class.getName());
         if (annotation != null)
         {
            ejbType = EJB_TYPE.STATELESS;
         }
         else
         {
            annotation = visible.getAnnotation(javax.ejb.Stateful.class.getName());
            if (annotation != null)
            {
               ejbType = EJB_TYPE.STATEFUL;
            }
            else
            {
               annotation = visible.getAnnotation(javax.persistence.Entity.class.getName());
               if (annotation != null)
               {
                  ejbType = EJB_TYPE.ENTITY;
               }
               else
               {
                  annotation = visible.getAnnotation(javax.ejb.MessageDriven.class.getName());
                  if (annotation != null)
                  {
                     ejbType = EJB_TYPE.MESSAGE_DRIVEN;
                  }
                  else
                  {
                     annotation = visible.getAnnotation(org.jboss.ejb3.annotation.Service.class.getName());
                     if (annotation != null)
                     {
                        ejbType = EJB_TYPE.SERVICE;
                     }
                     else
                     {
                        annotation = visible.getAnnotation(org.jboss.ejb3.annotation.Consumer.class.getName());
                        if (annotation != null)
                        {
                           ejbType = EJB_TYPE.CONSUMER;
                        }
                     }
                  }
               }
            }
         }

         if (annotation != null)
         {
            StringMemberValue mv = (StringMemberValue) annotation.getMemberValue("name");
            if (mv != null)
               ejbName = mv.getValue();
            else
               ejbName = ejbClass.getSimpleName();
         }
      }

      if (ejbName != null)
      {
         ejbNames.add(ejbName);
      }
   }
}
