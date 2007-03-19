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

import java.lang.reflect.Field;

import javassist.bytecode.ClassFile;
import org.jboss.annotation.IgnoreDependency;
import org.jboss.annotation.IgnoreDependencyImpl;
import org.jboss.annotation.ejb.Clustered;
import org.jboss.annotation.ejb.ClusteredImpl;
import org.jboss.annotation.ejb.Consumer;
import org.jboss.annotation.ejb.ConsumerImpl;
import org.jboss.annotation.ejb.CurrentMessage;
import org.jboss.annotation.ejb.CurrentMessageImpl;
import org.jboss.annotation.ejb.DefaultActivationSpecs;
import org.jboss.annotation.ejb.DefaultActivationSpecsImpl;
import org.jboss.annotation.ejb.DeliveryMode;
import org.jboss.annotation.ejb.Depends;
import org.jboss.annotation.ejb.DependsImpl;
import org.jboss.annotation.ejb.ExcludeClassInterceptorsImpl;
import org.jboss.annotation.ejb.ExcludeDefaultInterceptorsImpl;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.LocalBindingImpl;
import org.jboss.annotation.ejb.LocalHomeBinding;
import org.jboss.annotation.ejb.LocalHomeBindingImpl;
import org.jboss.annotation.ejb.LocalHomeImpl;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.ManagementImpl;
import org.jboss.annotation.ejb.MessageProperties;
import org.jboss.annotation.ejb.MessagePropertiesImpl;
import org.jboss.annotation.ejb.PoolClass;
import org.jboss.annotation.ejb.PoolClassImpl;
import org.jboss.annotation.ejb.Producers;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindingImpl;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.ejb.RemoteBindingsImpl;
import org.jboss.annotation.ejb.RemoteHomeBinding;
import org.jboss.annotation.ejb.RemoteHomeBindingImpl;
import org.jboss.annotation.ejb.RemoteHomeImpl;
import org.jboss.annotation.ejb.ResourceAdapter;
import org.jboss.annotation.ejb.ResourceAdapterImpl;
import org.jboss.annotation.ejb.SerializedConcurrentAccess;
import org.jboss.annotation.ejb.SerializedConcurrentAccessImpl;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.ejb.ServiceImpl;
import org.jboss.annotation.ejb.TransactionTimeout;
import org.jboss.annotation.ejb.TransactionTimeoutImpl;
import org.jboss.annotation.ejb.cache.Cache;
import org.jboss.annotation.ejb.cache.CacheImpl;
import org.jboss.annotation.ejb.cache.simple.PersistenceManager;
import org.jboss.annotation.ejb.cache.simple.PersistenceManagerImpl;
import org.jboss.annotation.internal.DefaultInterceptorMarker;
import org.jboss.annotation.internal.DefaultInterceptorMarkerImpl;
import org.jboss.annotation.security.RunAsPrincipalImpl;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.annotation.security.SecurityDomainImpl;
import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.ejb.ActivationConfigPropertyImpl;
import org.jboss.ejb.AroundInvokeImpl;
import org.jboss.ejb.DeclareRolesImpl;
import org.jboss.ejb.DenyAllImpl;
import org.jboss.ejb.InitImpl;
import org.jboss.ejb.InterceptorsImpl;
import org.jboss.ejb.LocalImpl;
import org.jboss.ejb.MessageDrivenImpl;
import org.jboss.ejb.PermitAllImpl;
import org.jboss.ejb.PostActivateImpl;
import org.jboss.ejb.PostConstructImpl;
import org.jboss.ejb.PreDestroyImpl;
import org.jboss.ejb.PrePassivateImpl;
import org.jboss.ejb.RemoteImpl;
import org.jboss.ejb.RemoveImpl;
import org.jboss.ejb.RolesAllowedImpl;
import org.jboss.ejb.RunAsImpl;
import org.jboss.ejb.StatelessImpl;
import org.jboss.ejb.TransactionAttributeImpl;
import org.jboss.ejb.TransactionManagementImpl;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.mdb.ConsumerContainer;
import org.jboss.ejb3.mdb.MDB;
import org.jboss.ejb3.mdb.ProducerImpl;
import org.jboss.ejb3.mdb.ProducersImpl;
import org.jboss.ejb3.metamodel.ActivationConfig;
import org.jboss.ejb3.metamodel.AssemblyDescriptor;
import org.jboss.ejb3.metamodel.CacheConfig;
import org.jboss.ejb3.metamodel.ClusterConfig;
import org.jboss.ejb3.metamodel.ContainerTransaction;
import org.jboss.ejb3.metamodel.EjbJarDD;
import org.jboss.ejb3.metamodel.EnterpriseBean;
import org.jboss.ejb3.metamodel.EnterpriseBeans;
import org.jboss.ejb3.metamodel.ExcludeList;
import org.jboss.ejb3.metamodel.InitMethod;
import org.jboss.ejb3.metamodel.InterceptorBinding;
import org.jboss.ejb3.metamodel.MessageDestination;
import org.jboss.ejb3.metamodel.MessageDrivenBean;
import org.jboss.ejb3.metamodel.MessageDrivenDestination;
import org.jboss.ejb3.metamodel.Method;
import org.jboss.ejb3.metamodel.MethodAttributes;
import org.jboss.ejb3.metamodel.MethodPermission;
import org.jboss.ejb3.metamodel.PoolConfig;
import org.jboss.ejb3.metamodel.RemoveMethod;
import org.jboss.ejb3.metamodel.SecurityIdentity;
import org.jboss.ejb3.metamodel.SessionEnterpriseBean;
import org.jboss.ejb3.metamodel.XmlAnnotation;
import org.jboss.ejb3.service.ServiceContainer;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.logging.Logger;
import org.jboss.metamodel.descriptor.InjectionTarget;
import org.jboss.metamodel.descriptor.MessageDestinationRef;
import org.jboss.metamodel.descriptor.NameValuePair;
import org.jboss.metamodel.descriptor.ResourceRef;
import org.jboss.metamodel.descriptor.RunAs;
import org.jboss.metamodel.descriptor.SecurityRole;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Init;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.MessageDriven;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @author <a href="mailto:bill@jboss.com">Bill Burke</a>
 */
public class Ejb3DescriptorHandler extends Ejb3AnnotationHandler
{
   private static final Logger log = Logger
         .getLogger(Ejb3DescriptorHandler.class);

   protected EjbJarDD dd;

   protected List<EnterpriseBean> ejbs = new ArrayList<EnterpriseBean>();

   private static Class clazz;

   public Ejb3DescriptorHandler(Ejb3Deployment deployment, ClassFile cf,
         EjbJarDD dd)
   {
      super(deployment, cf);
      this.dd = dd;
   }
   
   public Ejb3DescriptorHandler(Ejb3Deployment deployment, EjbJarDD dd)
   {
      super(deployment);
      this.dd = dd;
   }

   public boolean isEjb()
   {
      if (super.isEjb())
         return true;
      EnterpriseBeans enterpriseBeans = dd.getEnterpriseBeans();

      if (enterpriseBeans == null)
      {
         return false;
      }
      return enterpriseBeans.findEjbsByClass(cf.getName()).size() > 0;

   }

   protected void populateBaseInfo() throws Exception
   {
      super.populateBaseInfo();

      EnterpriseBeans enterpriseBeans = (dd.getEnterpriseBeans() != null) ? dd
            .getEnterpriseBeans() : new EnterpriseBeans();

      List<EnterpriseBean> ejbsByClass = enterpriseBeans.findEjbsByClass(cf
            .getName());

      for (int i = 0; i < ejbNames.size(); ++i)
      {
         String ejbNameFromAnnotation = ejbNames.get(i);
         EnterpriseBean enterpriseBean = enterpriseBeans
               .findEjbByEjbName(ejbNameFromAnnotation);
         ejbs.add(enterpriseBean);

         boolean removed = false;
         int j = 0;
         while (!removed && j < ejbsByClass.size())
         {
            EnterpriseBean ejbByClass = ejbsByClass.get(j);
            if (ejbByClass.getEjbName().equals(ejbNameFromAnnotation))
            {
               ejbsByClass.remove(j);
            } else
               ++j;
         }
      }

      for (EnterpriseBean enterpriseBean : ejbsByClass)
      {
         String ejbName = enterpriseBean.getEjbName();

         ejbs.add(enterpriseBean);
         ejbNames.add(ejbName);

         if (enterpriseBean.isSessionBean())
         {
            if (((SessionEnterpriseBean) enterpriseBean).isStateless())
               ejbType = EJB_TYPE.STATELESS;
            else
               ejbType = EJB_TYPE.STATEFUL;
         } else if (enterpriseBean.isEntityBean())
            ejbType = EJB_TYPE.ENTITY;
         else if (enterpriseBean.isMessageDrivenBean())
            ejbType = EJB_TYPE.MESSAGE_DRIVEN;
         else if (enterpriseBean.isService())
            ejbType = EJB_TYPE.SERVICE;
         else if (enterpriseBean.isConsumer())
            ejbType = EJB_TYPE.CONSUMER;
      }
   }
   
   protected EJB_TYPE getEjbType(EnterpriseBean enterpriseBean)
   {
      if (enterpriseBean.isSessionBean())
      {
         if (((SessionEnterpriseBean) enterpriseBean).isStateless())
            return EJB_TYPE.STATELESS;
         else
            return EJB_TYPE.STATEFUL;
      } else if (enterpriseBean.isEntityBean())
         return EJB_TYPE.ENTITY;
      else if (enterpriseBean.isMessageDrivenBean())
         return EJB_TYPE.MESSAGE_DRIVEN;
      else if (enterpriseBean.isService())
         return EJB_TYPE.SERVICE;
      else //if (enterpriseBean.isConsumer())
         return EJB_TYPE.CONSUMER;
   }
   
   public List getContainers(Ejb3Deployment deployment, Map<String, Container> preexistingContainers) throws Exception
   {     
      List containers = new ArrayList();

      EnterpriseBeans enterpriseBeans = (dd.getEnterpriseBeans() != null) ? dd
            .getEnterpriseBeans() : new EnterpriseBeans();

      Collection<EnterpriseBean> allXmlEjbs = enterpriseBeans.getEnterpriseBeans();
      ejbNames = new ArrayList<String>();
      for (EnterpriseBean ejb : allXmlEjbs)
      {
         String ejbName = ejb.getEjbName();
         if (preexistingContainers.get(ejbName) == null)
         {
            ejbNames.add(ejbName);
            ejbs.add(ejb);
         }
      }
      
      for (int ejbIndex = 0; ejbIndex < ejbNames.size(); ++ejbIndex)
      {
         String ejbName = ejbNames.get(ejbIndex);
         EnterpriseBean enterpriseBean = ejbs.get(ejbIndex);
         ejbType = getEjbType(enterpriseBean);
         className = enterpriseBean.getEjbClass();
         
         if (className == null)
            log.warn("Descriptor based bean has no ejb-class defined: " + ejbName);
         else
         {
            ejbClass = di.getClassLoader().loadClass(className);
            if (ejbType == EJB_TYPE.STATELESS)
            {
               EJBContainer container = getStatelessContainer(ejbIndex);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.STATEFUL)
            {
               StatefulContainer container = getStatefulContainer(ejbIndex);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.MESSAGE_DRIVEN)
            {
               MDB container = getMDB(ejbIndex);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.SERVICE)
            {
               ServiceContainer container = getServiceContainer(ejbIndex);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.CONSUMER)
            {
               ConsumerContainer container = getConsumerContainer(ejbIndex);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            log.debug("found EJB3: ejbName=" + ejbName + ", class=" + className + ", type=" + ejbType);
         }
      }
      
      return containers;
   }

   protected StatefulContainer getStatefulContainer(int ejbIndex)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      EnterpriseBean enterpriseBean = ejbs.get(ejbIndex);

      StatefulContainer container = super.getStatefulContainer(ejbIndex);

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      addInterfaces(container, enterpriseBean);

      addDescriptorAnnotations(container, enterpriseBean, ejbName);

      return container;
   }

   private void addHomeAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws Exception
   {
      if (enterpriseBean.getHome() != null)
      {
         RemoteHome annotation = new RemoteHomeImpl(di.getClassLoader()
               .loadClass(enterpriseBean.getHome()));
         addClassAnnotation(container, annotation.annotationType(), annotation);
      }

      if (enterpriseBean.getLocalHome() != null)
      {
         LocalHome annotation = new LocalHomeImpl(di.getClassLoader()
               .loadClass(enterpriseBean.getLocalHome()));
         addClassAnnotation(container, annotation.annotationType(), annotation);
      }
   }

   protected EJBContainer getStatelessContainer(int ejbIndex)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      EnterpriseBean enterpriseBean = ejbs.get(ejbIndex);

      EJBContainer container = super.getStatelessContainer(ejbIndex);

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      StatelessImpl annotation = new StatelessImpl(ejbName);
      if (enterpriseBean != null && !isAnnotatedBean())
      {
         addClassAnnotation(container, Stateless.class, annotation);
      }

      addInterfaces(container, enterpriseBean);

      addDescriptorAnnotations(container, enterpriseBean, ejbName);

      return container;
   }

   protected ServiceContainer getServiceContainer(int ejbIndex)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      org.jboss.ejb3.metamodel.Service service = (org.jboss.ejb3.metamodel.Service) ejbs
            .get(ejbIndex);

      ServiceContainer container = super.getServiceContainer(ejbIndex);
      ServiceImpl annotation = new ServiceImpl((Service) container
            .resolveAnnotation(Service.class));

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      if (service != null && !isAnnotatedBean())
      {
         if (service.getObjectName() != null)
            annotation.setObjectName(service.getObjectName());
         if (service.getEjbName() != null)
            annotation.setName(service.getEjbName());
         if (service.getXMBean() != null)
            annotation.setXMBean(service.getXMBean());
         addClassAnnotation(container, Service.class, annotation);
      }

      addInterfaces(container, service);

      addDescriptorAnnotations(container, service, ejbName);

      addServiceAnnotations(container, service);

      return container;
   }

   protected ConsumerContainer getConsumerContainer(int ejbIndex)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      org.jboss.ejb3.metamodel.Consumer consumer = (org.jboss.ejb3.metamodel.Consumer) ejbs
            .get(ejbIndex);

      ConsumerContainer container = super.getConsumerContainer(ejbIndex);
      ConsumerImpl annotation = new ConsumerImpl((Consumer) container
            .resolveAnnotation(Consumer.class));

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      if (consumer != null && !isAnnotatedBean())
      {
         if (consumer.getDestination() != null)
         {
            ActivationConfigPropertyImpl property = new ActivationConfigPropertyImpl(
                  "destination", consumer.getDestination());
            annotation.addActivationConfig(property);
         }

         if (consumer.getDestinationType() != null)
         {
            ActivationConfigPropertyImpl property = new ActivationConfigPropertyImpl(
                  "destinationType", consumer.getDestinationType());
            annotation.addActivationConfig(property);
         }

         addClassAnnotation(container, Consumer.class, annotation);
      }

      addInterfaces(container, consumer);

      addDescriptorAnnotations(container, consumer, ejbName);

      addConsumerAnnotations(container, consumer);

      return container;
   }

   protected String getMDBDomainName(int ejbIndex)
   {
      return defaultMDBDomain;
   }

   protected MDB getMDB(int ejbIndex) throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      EnterpriseBean enterpriseBean = ejbs.get(ejbIndex);

      MDB container = super.getMDB(ejbIndex);

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      addMDBAnnotations(container, ejbName, enterpriseBean);

      addInterfaces(container, enterpriseBean);

      addDescriptorAnnotations(container, enterpriseBean, ejbName);

      return container;
   }

   protected String getAspectDomain(int ejbIndex, String defaultDomain)
   {
      EnterpriseBean enterpriseBean = ejbs.get(ejbIndex);
      if (enterpriseBean != null)
      {
         String aopDomainName = enterpriseBean.getAopDomainName();
         if (aopDomainName != null)
         {
            log.debug("Found aop-domain-name element for annotation "
                  + aopDomainName + " for ejbName "
                  + enterpriseBean.getEjbName());

            return aopDomainName;
         }
      }
      return super.getAspectDomain(ejbIndex, defaultDomain);
   }

   protected boolean isAnnotatedBean()
   {
      return super.isEjb() || super.isJBossBeanType();
   }

   private void addMDBAnnotations(MDB container, String ejbName,
         EnterpriseBean enterpriseBean)
   {
      if (enterpriseBean != null)
      {
         MessageDrivenBean mdb = (MessageDrivenBean) enterpriseBean;

         ArrayList properties = new ArrayList();
         if (mdb.getAcknowledgeMode() != null)
            properties.add(new ActivationConfigPropertyImpl("acknowledgeMode",
                  mdb.getAcknowledgeMode()));

         if (mdb.getMessageDrivenDestination() != null)
         {
            MessageDrivenDestination destination = mdb
                  .getMessageDrivenDestination();
            if (destination.getDestinationType() != null)
               properties.add(new ActivationConfigPropertyImpl(
                     "destinationType", destination.getDestinationType()));
            if (destination.getSubscriptionDurability() != null)
            {
               String durable = "false";
               if (destination.getSubscriptionDurability().equals("Durable"))
                  durable = "true";
               properties.add(new ActivationConfigPropertyImpl("subscriptionDurability",
                     durable));
               if (destination.getSubscriptionDurability().equals("Durable"))
                  properties.add(new ActivationConfigPropertyImpl(
                        "subscriptionName", "subscriptionName"));

            }
         }

         if (mdb.getResourceAdaptorName() != null)
         {
            ResourceAdapter adapter = new ResourceAdapterImpl(mdb
                  .getResourceAdaptorName());
            addClassAnnotation(container, ResourceAdapter.class, adapter);
         }

         ActivationConfig activationConfig = mdb.getActivationConfig();
         if (activationConfig != null)
         {
            for (Object o : activationConfig.getActivationConfigProperties())
            {
               NameValuePair property = (NameValuePair) o;
               properties.add(new ActivationConfigPropertyImpl(property
                     .getName(), property.getValue()));
            }
         }

         if (mdb.getDestinationJndiName() != null)
         {
            properties.add(new ActivationConfigPropertyImpl("destination", mdb
                  .getDestinationJndiName()));
         }

         if (mdb.getMdbUser() != null)
         {
            properties.add(new ActivationConfigPropertyImpl("user", mdb
                  .getMdbUser()));
         }

         if (mdb.getMdbPassword() != null)
         {
            properties.add(new ActivationConfigPropertyImpl("password", mdb
                  .getMdbPassword()));

         }
         
         if (mdb.getMdbSubscriptionId() != null)
         {
            properties.add(new ActivationConfigPropertyImpl("subscriptionName", mdb
                  .getMdbSubscriptionId()));

         }

         ActivationConfigPropertyImpl[] propsArray = new ActivationConfigPropertyImpl[properties
               .size()];
         properties.toArray(propsArray);
         MessageDrivenImpl annotation = new MessageDrivenImpl(ejbName,
               propsArray);
         if (mdb.getMessagingType() != null)
         {
            try
            {
               annotation.setMessageListenerInterface(container
                     .getClassloader().loadClass(mdb.getMessagingType()));
            } catch (ClassNotFoundException e)
            {
               throw new RuntimeException(e);
            }
         }

         if (isAnnotatedBean())
         {
            annotation.merge((MessageDriven) ejbClass
                  .getAnnotation(MessageDriven.class));
         }

         addClassAnnotation(container, MessageDriven.class, annotation);

         addDefaultActivationConfig(container, mdb);
      }
   }

   private void addDefaultActivationConfig(MDB container, MessageDrivenBean mdb)
   {
      ActivationConfig defaultActivationConfig = mdb.getDefaultActivationConfig();
      if (defaultActivationConfig != null)
      {
         DefaultActivationSpecsImpl activationAnnotation = new DefaultActivationSpecsImpl();
         for (Object o : defaultActivationConfig.getActivationConfigProperties())
         {
            NameValuePair property = (NameValuePair) o;
            activationAnnotation.addActivationConfigProperty(new ActivationConfigPropertyImpl(property
                  .getName(), property.getValue()));
         }

         DefaultActivationSpecs existingAnnotation = (DefaultActivationSpecs)ejbClass.getAnnotation(DefaultActivationSpecs.class);
         if (existingAnnotation != null)
            activationAnnotation.merge(existingAnnotation);

         addClassAnnotation(container, DefaultActivationSpecs.class, activationAnnotation);
      }
   }

   private void addInterfaces(EJBContainer container,
         EnterpriseBean enterpriseBean) throws ClassNotFoundException
   {
      if (enterpriseBean != null)
      {
         String local = enterpriseBean.getLocal();
         String remote = enterpriseBean.getRemote();

         if (remote != null)
         {
            StringTokenizer classes = new StringTokenizer(remote, ",");
            ArrayList<Class> remoteClasses = new ArrayList<Class>();
            while (classes.hasMoreTokens())
            {
               String token = classes.nextToken();
               String classname = token.trim();
               remoteClasses.add(di.getClassLoader().loadClass(classname));

            }
            Class[] intfs = new Class[remoteClasses.size()];
            intfs = remoteClasses.toArray(intfs);
            addClassAnnotation(container, Remote.class, new RemoteImpl(intfs));
         }

         if (local != null)
         {
            StringTokenizer classes = new StringTokenizer(local, ",");
            ArrayList<Class> localClasses = new ArrayList<Class>();
            while (classes.hasMoreTokens())
            {
               String token = classes.nextToken();
               String classname = token.trim();
               localClasses.add(di.getClassLoader().loadClass(classname));

            }
            Class[] intfs = new Class[localClasses.size()];
            intfs = localClasses.toArray(intfs);
            addClassAnnotation(container, Local.class, new LocalImpl(intfs));
         }
      }
   }

   private void addDescriptorAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean, String ejbName) throws Exception
   {
      container.setXml(enterpriseBean);

      addTransactionAnnotations(container, enterpriseBean, ejbName);

      addAssemblyAnnotations(container, enterpriseBean, ejbName);

      addSecurityAnnotations(container, enterpriseBean, ejbName);

      addEjbAnnotations(container, enterpriseBean);

      addEjb21Annotations(container);
   }

   private void addEjb21Annotations(EJBContainer container) throws Exception
   {
      Class[] interfaces = ejbClass.getInterfaces();
      for (Class beanInterface : interfaces)
      {
         if (beanInterface.equals(javax.ejb.SessionBean.class))
         {
            Method method = new Method();
            method.setEjbName(container.getEjbName());

            Object annotation = new PostConstructImpl();
            Class annotationClass = javax.annotation.PostConstruct.class;
            method.setMethodName("ejbCreate");
            addAnnotations(annotationClass, annotation, container, method);

            annotation = new PostActivateImpl();
            annotationClass = javax.ejb.PostActivate.class;
            method.setMethodName("ejbActivate");
            addAnnotations(annotationClass, annotation, container, method);

            annotation = new PrePassivateImpl();
            annotationClass = javax.ejb.PrePassivate.class;
            method.setMethodName("ejbPassivate");
            addAnnotations(annotationClass, annotation, container, method);

            annotation = new PreDestroyImpl();
            annotationClass = javax.annotation.PreDestroy.class;
            method.setMethodName("ejbRemove");
            addAnnotations(annotationClass, annotation, container, method);
         }
      }
   }

   private void addAssemblyAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean, String ejbName) throws Exception
   {
      AssemblyDescriptor assembly = dd.getAssemblyDescriptor();
      if (assembly != null)
      {
         addExcludeAnnotations(container, assembly.getExcludeList(), ejbName);

         addInterceptorBindingAnnotations(container, enterpriseBean, ejbName);
      }

      if (enterpriseBean instanceof SessionEnterpriseBean)
      {
         addInitAnnotations(container, ((SessionEnterpriseBean) enterpriseBean)
               .getInitMethods(), ejbName);
         addRemoveAnnotations(container,
               ((SessionEnterpriseBean) enterpriseBean).getRemoveMethods(),
               ejbName);
      }
   }

   private void addExcludeAnnotations(EJBContainer container, ExcludeList list,
         String ejbName) throws ClassNotFoundException, NoSuchMethodException,
         NoSuchFieldException
   {
      if (list != null)
      {
         for (Object o : list.getMethods())
         {
            Method method = (Method) o;
            if (method.getEjbName().equals(ejbName))
            {
               DenyAllImpl annotation = new DenyAllImpl();
               addAnnotations(DenyAll.class, annotation, container, method);
            }
         }
      }
   }

   private void addInitAnnotations(EJBContainer container,
         List<InitMethod> list, String ejbName) throws ClassNotFoundException,
         NoSuchMethodException, NoSuchFieldException
   {
      if (list != null)
      {
         for (InitMethod initMethod : list)
         {
            Method method = initMethod.getBeanMethod();
            InitImpl annotation = new InitImpl();
            addAnnotations(Init.class, annotation, container, method);
         }
      }
   }

   private void addRemoveAnnotations(EJBContainer container,
         List<RemoveMethod> list, String ejbName)
         throws ClassNotFoundException, NoSuchMethodException,
         NoSuchFieldException
   {
      if (list != null)
      {
         for (RemoveMethod removeMethod : list)
         {
            Method method = removeMethod.getBeanMethod();
            RemoveImpl annotation = new RemoveImpl(removeMethod
                  .isRetainIfException());
            addAnnotations(Remove.class, annotation, container, method);
         }
      }
   }

   private void addSecurityAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean, String ejbName)
         throws ClassNotFoundException, NoSuchMethodException,
         NoSuchFieldException
   {
      AssemblyDescriptor assembly = dd.getAssemblyDescriptor();
      if (assembly != null)
      {
         List securityRoles = assembly.getSecurityRoles();

         if (securityRoles.size() > 0)
         {
            ArrayList roleList = new ArrayList();
            for (Object securityRole : securityRoles)
            {
               SecurityRole role = (SecurityRole) securityRole;
               roleList.add(role.getRoleName());

            }
            DeclareRolesImpl annotation = new DeclareRolesImpl(
                  (String[]) roleList.toArray(new String[roleList.size()]));
            addClassAnnotation(container, DeclareRoles.class, annotation);
         }

         List methodPermissions = assembly.getMethodPermissions();
         for (Object methodPermission : methodPermissions)
         {
            MethodPermission permission = (MethodPermission) methodPermission;
            for (Method method : permission.getMethods())
            {
               if (method.getEjbName().equals(ejbName))
               {
                  if (permission.isUnchecked())
                  {
                     PermitAllImpl annotation = new PermitAllImpl();
                     addAnnotations(PermitAll.class, annotation, container,
                           method);
                  } else
                  {
                     RolesAllowedImpl annotation = new RolesAllowedImpl();

                     for (Object o : permission.getRoleNames())
                     {
                        String roleName = (String) o;
                        annotation.addValue(roleName);
                     }
                     addAnnotations(RolesAllowed.class, annotation, container,
                           method);
                  }
               }
            }
         }
      }

      if (enterpriseBean != null && enterpriseBean.getSecurityDomain() != null)
      {
         String securityDomain = enterpriseBean.getSecurityDomain();

         SecurityDomainImpl annotation = new SecurityDomainImpl(securityDomain);

         if (dd.getUnauthenticatedPrincipal() != null)
            annotation.setUnauthenticatedPrincipal(dd
                  .getUnauthenticatedPrincipal());

         addClassAnnotation(container, annotation.annotationType(), annotation);
      } else if (dd.getSecurityDomain() != null)
      {
         String securityDomain = dd.getSecurityDomain();

         SecurityDomainImpl annotation = new SecurityDomainImpl(securityDomain);

         if (dd.getUnauthenticatedPrincipal() != null)
            annotation.setUnauthenticatedPrincipal(dd
                  .getUnauthenticatedPrincipal());

         addClassAnnotation(container, annotation.annotationType(), annotation);
      } else if (dd.getUnauthenticatedPrincipal() != null)
      {
         SecurityDomain annotation = (SecurityDomain) ejbClass
               .getAnnotation(SecurityDomain.class);
         if (annotation != null)
         {
            SecurityDomainImpl override = new SecurityDomainImpl(annotation
                  .value());
            override.setUnauthenticatedPrincipal(dd
                  .getUnauthenticatedPrincipal());

            addClassAnnotation(container, override.annotationType(), override);
         }
      }
   }

   private void addTransactionAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean, String ejbName)
         throws ClassNotFoundException, NoSuchMethodException,
         NoSuchFieldException
   {
      if (enterpriseBean != null)
      {
         if (enterpriseBean.getTransactionManagementType() != null)
         {
            TransactionManagementImpl annotation = new TransactionManagementImpl();
            annotation.setValue(enterpriseBean.getTransactionManagementType());
            addClassAnnotation(container, TransactionManagement.class,
                  annotation);
         }

         MethodAttributes attributes = enterpriseBean.getMethodAttributes();
         if (attributes != null)
         {
            Iterator methods = attributes.getMethods().iterator();
            while (methods.hasNext())
            {
               Method method = (Method) methods.next();
               if (method.getTransactionTimeout() != null)
               {
                  TransactionTimeout timeoutAnnotation = new TransactionTimeoutImpl(
                        Integer.parseInt(method.getTransactionTimeout()));
                  addAnnotations(TransactionTimeout.class, timeoutAnnotation,
                        container, method);
               }
            }
         }
      }

      AssemblyDescriptor descriptor = dd.getAssemblyDescriptor();
      if (descriptor != null)
      {
         Iterator transactions = descriptor.getContainerTransactions()
               .iterator();
         while (transactions.hasNext())
         {
            ContainerTransaction transaction = (ContainerTransaction) transactions
                  .next();
            if (transaction.getMethod().getEjbName().equals(ejbName))
            {
               String transAttribute = transaction.getTransAttribute();
               TransactionAttributeImpl annotation = new TransactionAttributeImpl();
               if (transAttribute.equals("Mandatory"))
                  annotation.setType(TransactionAttributeType.MANDATORY);
               else if (transAttribute.equals("Required"))
                  annotation.setType(TransactionAttributeType.REQUIRED);
               else if (transAttribute.equals("RequiresNew"))
                  annotation.setType(TransactionAttributeType.REQUIRES_NEW);
               else if (transAttribute.equals("Supports"))
                  annotation.setType(TransactionAttributeType.SUPPORTS);
               else if (transAttribute.equals("NotSupported"))
                  annotation.setType(TransactionAttributeType.NOT_SUPPORTED);
               else if (transAttribute.equals("Never"))
                  annotation.setType(TransactionAttributeType.NEVER);

               addAnnotations(TransactionAttribute.class, annotation,
                     container, transaction.getMethod());
            }
         }
      }
   }

   /**
    * Interceptors are additive. What's in the annotations and in the XML is
    * merged
    */
   private void addInterceptorBindingAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean, String ejbName)
         throws ClassNotFoundException, NoSuchMethodException,
         NoSuchFieldException
   {
      boolean definesInterceptors = false;

      List<InterceptorBinding> interceptorBindings = dd.getAssemblyDescriptor()
            .getInterceptorBindings();
      for (InterceptorBinding binding : interceptorBindings)
      {
         // Wolf: why ignore ordered binding?
         /*
         if (binding.isOrdered())
         {
            continue;
         }
         */
         if (binding.getEjbName().equals(ejbName))
         {
            if (binding.getMethodName() == null
                  || binding.getMethodName().trim().length() == 0)
            {
               addClassLevelInterceptorBindingAnnotations(container, binding);
               definesInterceptors = true;
            } else
            {
               definesInterceptors = addMethodLevelInterceptorBindingAnnotations(
                     container, binding);
            }

         }
      }

      if (!definesInterceptors
            && di.getInterceptorInfoRepository().hasDefaultInterceptors())
      {
         addClassAnnotation(container, DefaultInterceptorMarker.class,
               new DefaultInterceptorMarkerImpl());
      }
   }

   /**
    * Interceptors are additive. What's in the annotations and in the XML is
    * merged
    */
   private void addClassLevelInterceptorBindingAnnotations(
         EJBContainer container, InterceptorBinding binding)
         throws ClassNotFoundException
   {
      Interceptors interceptors = (Interceptors) container
            .resolveAnnotation(Interceptors.class);
      InterceptorsImpl impl = InterceptorsImpl.getImpl(interceptors);
      for (String name : binding.getInterceptorClasses())
      {
         Class clazz = di.getClassLoader().loadClass(name);
         impl.addValue(clazz);
      }

      addClassAnnotation(container, impl.annotationType(), impl);

      boolean exclude = binding.getExcludeDefaultInterceptors();
      if (exclude
            && container.resolveAnnotation(ExcludeDefaultInterceptors.class) == null)
      {
         addClassAnnotation(container, ExcludeDefaultInterceptors.class,
               new ExcludeDefaultInterceptorsImpl());
      }

   }

   /**
    * Interceptors are additive. What's in the annotations and in the XML is
    * merged
    */
   private boolean addMethodLevelInterceptorBindingAnnotations(
         EJBContainer container, InterceptorBinding binding)
         throws ClassNotFoundException
   {
      boolean addedAnnotations = false;
      for (java.lang.reflect.Method method : container.getBeanClass()
            .getMethods())
      {
         boolean matches = false;
         if (method.getName().equals(binding.getMethodName()))
         {
            if (binding.getMethodParams() == null)
            {
               matches = true;
            } else
            {
               Class[] methodParams = method.getParameterTypes();
               List<String> bindingParams = binding.getMethodParams();

               if (methodParams.length == bindingParams.size())
               {
                  matches = true;
                  int i = 0;
                  for (String paramName : bindingParams)
                  {
                     String methodParamName = InterceptorInfoRepository
                           .simpleType(methodParams[i++]);
                     if (!paramName.equals(methodParamName))
                     {
                        matches = false;
                        break;
                     }
                  }
               }
            }
         }

         if (matches)
         {
            Interceptors interceptors = (Interceptors) container
                  .resolveAnnotation(method, Interceptors.class);
            InterceptorsImpl impl = InterceptorsImpl.getImpl(interceptors);
            for (String name : binding.getInterceptorClasses())
            {
               Class clazz = di.getClassLoader().loadClass(name);
               impl.addValue(clazz);
            }
            log.debug("adding " + Interceptors.class.getName()
                  + " method annotation to " + ejbClass.getName() + "."
                  + method.getName() + "(" + getParameters(method) + ")");
            container.getAnnotations().addAnnotation(method,
                  Interceptors.class, impl);

            boolean excludeDefault = binding.getExcludeDefaultInterceptors();
            if (excludeDefault
                  && container.resolveAnnotation(method,
                        ExcludeDefaultInterceptors.class) == null)
            {
               log.debug("adding " + ExcludeDefaultInterceptors.class.getName()
                     + " method annotation to " + ejbClass.getName() + "."
                     + method.getName() + "(" + getParameters(method) + ")");
               container.getAnnotations().addAnnotation(method,
                     ExcludeDefaultInterceptors.class,
                     new ExcludeDefaultInterceptorsImpl());
            }

            boolean excludeClass = binding.getExcludeClassInterceptors();
            if (excludeClass
                  && container.resolveAnnotation(method,
                        ExcludeClassInterceptors.class) == null)
            {
               log.debug("adding " + ExcludeClassInterceptors.class.getName()
                     + " method annotation to " + ejbClass.getName() + "."
                     + method.getName() + "(" + getParameters(method) + ")");
               container.getAnnotations().addAnnotation(method,
                     ExcludeClassInterceptors.class,
                     new ExcludeClassInterceptorsImpl());
            }
            matches = false;
            addedAnnotations = true;
         }
      }

      return addedAnnotations;
   }

   private void addEjbAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws Exception
   {
      if (enterpriseBean != null)
      {
         addHomeAnnotations(container, enterpriseBean);

         addJndiAnnotations(container, enterpriseBean);

         addInterceptorMethodAnnotations(container, enterpriseBean);

         handleResourceRefs(container, enterpriseBean.getResourceRefs());

         addMessageDestinationAnnotations(container, enterpriseBean.getMessageDestinationRefs());

         addSecurityIdentityAnnotation(container, enterpriseBean
               .getSecurityIdentity());

         addDependencies(container, enterpriseBean);

         addPoolAnnotations(container, enterpriseBean);
         
         addXmlAnnotations(container, enterpriseBean);

         if (enterpriseBean instanceof SessionEnterpriseBean)
         {
            addConcurrentAnnotations(container, (SessionEnterpriseBean)enterpriseBean);
            addClusterAnnotations(container, (SessionEnterpriseBean)enterpriseBean);
            addCacheAnnotations(container, (SessionEnterpriseBean)enterpriseBean);
         }
      }
   }

   private void addConcurrentAnnotations(EJBContainer container,
         SessionEnterpriseBean enterpriseBean) throws Exception
   {
      if (enterpriseBean.getConcurrent() != null)
      {
         boolean concurrent = Boolean.getBoolean(enterpriseBean.getConcurrent());
         if (concurrent)
         {
            SerializedConcurrentAccessImpl annotation = new SerializedConcurrentAccessImpl();
            addClassAnnotation(container, SerializedConcurrentAccess.class, annotation);
         }
         else
         {
            container.getAnnotations().disableAnnotation(SerializedConcurrentAccess.class.getName());
         }
      }
   }

   private void addPoolAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws Exception
   {
      if (enterpriseBean.getPoolConfig() != null)
      {
         PoolConfig config = enterpriseBean.getPoolConfig();

         PoolClassImpl poolAnnotation = new PoolClassImpl();

         if (config.getPoolClass() != null)
            poolAnnotation.setValue(di.getClassLoader().loadClass(config.getPoolClass()));

         if (config.getMaxSize() != null)
            poolAnnotation.setMaxSize(Integer.parseInt(config.getMaxSize()));

         if (config.getTimeout() != null)
            poolAnnotation.setTimeout(Long.parseLong(config.getTimeout()));

         addClassAnnotation(container, PoolClass.class, poolAnnotation);
      }
   }
   
   private void addXmlAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws Exception
   {
      Iterator xmlAnnotations = enterpriseBean.getXmlAnnotations().iterator();
      while (xmlAnnotations.hasNext())
      {
         XmlAnnotation xmlAnnotation = (XmlAnnotation)xmlAnnotations.next();

         Class annotationClass = di.getClassLoader().loadClass(xmlAnnotation.getAnnotationClass());
         Class annotationImplementationClass = di.getClassLoader().loadClass(xmlAnnotation.getAnnotationImplementationClass());
         Object annotation = annotationImplementationClass.newInstance();
         
         Iterator properties = xmlAnnotation.getProperties().iterator();
         while (properties.hasNext())
         {
            NameValuePair property = (NameValuePair)properties.next();
            Field field = annotationImplementationClass.getDeclaredField(property.getName());
            setAnnotationPropertyField(field, annotation, property.getValue());
         }
            
         if (xmlAnnotation.getInjectionTarget() == null)
         {
            addClassAnnotation(container, annotationClass, annotation);
         } 
         else
         {
            Method method = new Method();
            method.setMethodName(xmlAnnotation.getInjectionTarget().getTargetName());
            addAnnotations(annotationClass, annotation, container, method);
         }
      }
   }
   
   protected void setAnnotationPropertyField(Field field, Object annotation, String value) throws Exception
   {
      if (field.getType() == String.class)
         field.set(annotation, value);
      else if (field.getType() == Long.class)
         field.setLong(annotation, Long.parseLong(value));
      else if (field.getType() == Integer.class)
         field.setInt(annotation, Integer.parseInt(value));
      else if (field.getType() == Class.class)
         field.set(annotation, di.getClassLoader().loadClass(value));
      else if (field.getType() == Boolean.class)
         field.setBoolean(annotation, Boolean.parseBoolean(value));
   }

   private void addCacheAnnotations(EJBContainer container,
         SessionEnterpriseBean enterpriseBean) throws Exception
   {
      if (enterpriseBean.getCacheConfig() != null)
      {
         CacheConfig config = enterpriseBean.getCacheConfig();
         if (config.getCacheClass() != null)
         {
            Class cacheClass = di.getClassLoader().loadClass(config.getCacheClass());
            CacheImpl cacheAnnotation = new CacheImpl(cacheClass);
            addClassAnnotation(container, Cache.class, cacheAnnotation);

            if (cacheClass == org.jboss.ejb3.cache.simple.SimpleStatefulCache.class)
            {
               if (!ejbClass.isAnnotationPresent(PersistenceManager.class))
               {
                  PersistenceManagerImpl persistenceAnnotation = new PersistenceManagerImpl();
                  if (config.getPersistenceManager() != null)
                     persistenceAnnotation.setValue(di.getClassLoader().loadClass(config.getPersistenceManager()));
                  addClassAnnotation(container, PersistenceManager.class, persistenceAnnotation);
               }
            }
         }

         if (config.getName() != null)
         {
            org.jboss.annotation.ejb.cache.tree.CacheConfigImpl configAnnotation = new org.jboss.annotation.ejb.cache.tree.CacheConfigImpl();

            configAnnotation.setName(config.getName());

            if (config.getMaxSize() != null)
               configAnnotation.setMaxSize(Integer.parseInt(config.getMaxSize()));

            if (config.getIdleTimeoutSeconds() != null)
               configAnnotation.setIdleTimeoutSeconds(Long.parseLong(config.getIdleTimeoutSeconds()));

            if (config.getReplicationIsPassivation() != null)
               configAnnotation.setReplicationIsPassivation(Boolean.parseBoolean(config.getReplicationIsPassivation()));

            if (config.getRemoveTimeoutSeconds() != null)
               configAnnotation.setRemovalTimeoutSeconds(Long.parseLong(config.getRemoveTimeoutSeconds()));
            
            org.jboss.annotation.ejb.cache.tree.CacheConfig existingConfig = (org.jboss.annotation.ejb.cache.tree.CacheConfig)ejbClass.getAnnotation(org.jboss.annotation.ejb.cache.tree.CacheConfig.class);
            if (existingConfig != null)
               configAnnotation.merge(existingConfig);
            
            addClassAnnotation(container, org.jboss.annotation.ejb.cache.tree.CacheConfig.class, configAnnotation);
         }
         else
         {
            org.jboss.annotation.ejb.cache.simple.CacheConfigImpl configAnnotation = new org.jboss.annotation.ejb.cache.simple.CacheConfigImpl();

            if (config.getMaxSize() != null)
               configAnnotation.setMaxSize(Integer.parseInt(config.getMaxSize()));

            if (config.getIdleTimeoutSeconds() != null)
               configAnnotation.setIdleTimeoutSeconds(Long.parseLong(config.getIdleTimeoutSeconds()));
            
            if (config.getRemoveTimeoutSeconds() != null)
               configAnnotation.setRemovalTimeoutSeconds(Long.parseLong(config.getRemoveTimeoutSeconds()));

            org.jboss.annotation.ejb.cache.simple.CacheConfig existingConfig = (org.jboss.annotation.ejb.cache.simple.CacheConfig)ejbClass.getAnnotation(org.jboss.annotation.ejb.cache.simple.CacheConfig.class);
            if (existingConfig != null)
               configAnnotation.merge(existingConfig);
            
            addClassAnnotation(container, org.jboss.annotation.ejb.cache.simple.CacheConfig.class, configAnnotation);
         }
      }

   }

   private void addClusterAnnotations(EJBContainer container,
         SessionEnterpriseBean enterpriseBean) throws Exception
   {
      ClusteredImpl clusteredAnnotation = null;

      if (enterpriseBean.getClustered() != null)
      {
         Clustered existingAnnotation = (Clustered)ejbClass.getAnnotation(Clustered.class);

         boolean clustered = Boolean.parseBoolean(enterpriseBean.getClustered());
         if (!clustered)
         {
            if (existingAnnotation != null)
              container.getAnnotations().disableAnnotation(Clustered.class.getName());

            return;
         }
         else
         {
            if (existingAnnotation == null)
               clusteredAnnotation = new ClusteredImpl();
         }
      }

      ClusterConfig config = enterpriseBean.getClusterConfig();
      if (config != null)
      {
         if (clusteredAnnotation == null)
            clusteredAnnotation = new ClusteredImpl();

         if (config.getLoadBalancePolicy() != null)
         {
            Class policy = di.getClassLoader().loadClass(config.getLoadBalancePolicy());
            clusteredAnnotation.setLoadBalancePolicy(policy);
         }

         if (config.getPartition() != null)
         {
            clusteredAnnotation.setPartition(config.getPartition());
         }
      }

      if (clusteredAnnotation != null)
      {
         addClassAnnotation(container, Clustered.class, clusteredAnnotation);
      }
   }

   private void addDependencies(EJBContainer container,
         EnterpriseBean enterpriseBean) throws Exception
   {
      if (enterpriseBean.getDependencies().size() > 0)
      {
         DependsImpl annotation = new DependsImpl();
         Iterator<String> dependencies = enterpriseBean.getDependencies()
               .iterator();
         while (dependencies.hasNext())
         {
            annotation.addDependency(dependencies.next());
         }

         addClassAnnotation(container, Depends.class, annotation);
      }

      if (enterpriseBean.getIgnoreDependencies().size() > 0)
      {
         Iterator<InjectionTarget> ignores = enterpriseBean.getIgnoreDependencies().iterator();
         while (ignores.hasNext())
         {
            InjectionTarget ignore = ignores.next();
            IgnoreDependencyImpl annotation = new IgnoreDependencyImpl();

            Method method = new Method();
            method.setMethodName(ignore.getTargetName());

            addAnnotations(IgnoreDependency.class, annotation, container, method);
         }
      }
   }

   private void addServiceAnnotations(EJBContainer container, EnterpriseBean ejb)
         throws ClassNotFoundException
   {
      org.jboss.ejb3.metamodel.Service service = (org.jboss.ejb3.metamodel.Service) ejb;

      if (service == null)
         return;

      String management = service.getManagement();

      if (management != null)
      {
         ManagementImpl annotation = new ManagementImpl(di.getClassLoader()
               .loadClass(management));
         addClassAnnotation(container, Management.class, annotation);
      }
   }

   private void addConsumerAnnotations(EJBContainer container,
         EnterpriseBean ejb) throws ClassNotFoundException,
         NoSuchFieldException, NoSuchMethodException
   {
      org.jboss.ejb3.metamodel.Consumer consumer = (org.jboss.ejb3.metamodel.Consumer) ejb;

      if (consumer == null)
         return;

      if (consumer.getProducers().size() > 0
            || consumer.getLocalProducers().size() > 0)
      {
         ProducersImpl producersAnnotation = new ProducersImpl();

         Iterator producers = consumer.getProducers().iterator();
         while (producers.hasNext())
         {
            org.jboss.ejb3.metamodel.Producer producer = (org.jboss.ejb3.metamodel.Producer) producers
                  .next();
            ProducerImpl annotation = new ProducerImpl(di.getClassLoader()
                  .loadClass(producer.getClassName()));
            if (producer.getConnectionFactory() != null)
               annotation.setConnectionFactory(producer.getConnectionFactory());
            producersAnnotation.addProducer(annotation);
         }

         producers = consumer.getLocalProducers().iterator();
         while (producers.hasNext())
         {
            org.jboss.ejb3.metamodel.Producer producer = (org.jboss.ejb3.metamodel.Producer) producers
                  .next();
            ProducerImpl annotation = new ProducerImpl(di.getClassLoader()
                  .loadClass(producer.getClassName()));
            if (producer.getConnectionFactory() != null)
               annotation.setConnectionFactory(producer.getConnectionFactory());
            producersAnnotation.addProducer(annotation);
         }
         addClassAnnotation(container, Producers.class, producersAnnotation);
      }

      org.jboss.ejb3.metamodel.CurrentMessage currentMessage = consumer
            .getCurrentMessage();
      if (currentMessage != null)
      {
         List methods = currentMessage.getMethods();
         CurrentMessageImpl annotation = new CurrentMessageImpl();
         for (int i = 0; i < methods.size(); ++i)
         {
            Method method = (Method) methods.get(i);
            addAnnotations(CurrentMessage.class, annotation, container, method);
         }
      }

      org.jboss.ejb3.metamodel.MessageProperties properties = consumer
            .getMessageProperties();
      if (properties != null)
      {
         List methods = properties.getMethods();

         MessagePropertiesImpl annotation = new MessagePropertiesImpl();

         String delivery = properties.getDelivery();
         if (delivery != null && delivery.equals("Persistent"))
            annotation.setDelivery(DeliveryMode.PERSISTENT);
         else
            annotation.setDelivery(DeliveryMode.NON_PERSISTENT);

         String priority = properties.getPriority();
         if (priority != null)
            annotation.setDelivery(DeliveryMode.PERSISTENT);

         String interfac = properties.getClassName();
         if (interfac != null)
         {
            Class clazz = di.getClassLoader().loadClass(interfac);
            annotation.setInterface(clazz);
         }

         for (int i = 0; i < methods.size(); ++i)
         {
            Method method = (Method) methods.get(i);
            addAnnotations(MessageProperties.class, annotation, container,
                  method);
         }
      }
   }

   private void addJndiAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws ClassNotFoundException
   {
      addLocalJndiAnnotations(container, enterpriseBean);
      addRemoteJndiAnnotations(container, enterpriseBean);
   }

   private void addLocalJndiAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws ClassNotFoundException
   {
      String localJndiName = enterpriseBean.getLocalJndiName();
      if (localJndiName != null)
      {
         LocalBindingImpl localBinding = new LocalBindingImpl(localJndiName);
         addClassAnnotation(container, LocalBinding.class, localBinding);
      }
      
      String localHomeJndiName = enterpriseBean.getLocalHomeJndiName();
      if (localHomeJndiName != null)
      {
         LocalHomeBindingImpl localHomeBinding = new LocalHomeBindingImpl(localHomeJndiName);
         addClassAnnotation(container, LocalHomeBinding.class, localHomeBinding);
      } 
   }

   private void addRemoteJndiAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws ClassNotFoundException
   {
	   String homeJndiName = enterpriseBean.getHomeJndiName();
	   if (homeJndiName != null)
      {
         RemoteHomeBindingImpl homeBinding = new RemoteHomeBindingImpl(homeJndiName);
         addClassAnnotation(container, RemoteHomeBinding.class, homeBinding);
      } 
	      
      List<org.jboss.ejb3.metamodel.RemoteBinding> bindingsList = enterpriseBean.getRemoteBindings();
      if (bindingsList.size() == 0)
      {
         addSimpleJndiAnnotations(container, enterpriseBean);
         return;
      }

      AnnotationRepository annotations = container.getAnnotations();

      annotations.disableAnnotation(RemoteBinding.class.getName());

      List<RemoteBindingImpl> bindingAnnotationsList = new ArrayList();

      Iterator bindings = bindingsList.iterator();
      while(bindings.hasNext())
      {
         org.jboss.ejb3.metamodel.RemoteBinding binding = (org.jboss.ejb3.metamodel.RemoteBinding)bindings.next();
         RemoteBindingImpl bindingAnnotation = new RemoteBindingImpl();

         if (binding.getJndiName() != null)
            bindingAnnotation.setJndiBinding(binding.getJndiName());

         if (binding.getClientBindUrl() != null)
            bindingAnnotation.setBindUrl(binding.getClientBindUrl());

         if (binding.getInterceptorStack() != null)
            bindingAnnotation.setStack(binding.getInterceptorStack());

         if (binding.getProxyFactory() != null)
            bindingAnnotation.setFactory(di.getClassLoader().loadClass(binding.getProxyFactory()));

         bindingAnnotationsList.add(bindingAnnotation);

      }

      RemoteBindingsImpl bindingsAnnotation = new RemoteBindingsImpl(bindingAnnotationsList);
      addClassAnnotation(container, RemoteBindings.class, bindingsAnnotation);
   }

   private void addSimpleJndiAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean) throws ClassNotFoundException
   {
      RemoteBindingImpl remoteBinding = null;

      String jndiName = enterpriseBean.getJndiName();
      if (jndiName != null)
      {
         remoteBinding = new RemoteBindingImpl();
         remoteBinding.setJndiBinding(jndiName);
         addClassAnnotation(container, RemoteBinding.class, remoteBinding);
      }

      if (remoteBinding != null)
      {
         RemoteBinding existingBinding = (RemoteBinding)ejbClass.getAnnotation(RemoteBinding.class);
         if (existingBinding != null)
            remoteBinding.merge(existingBinding);

         addClassAnnotation(container, RemoteBinding.class, remoteBinding);
      }
   }

   private void handleResourceRefs(EJBContainer container,
         Collection<ResourceRef> resourceRefList)
   {
      Iterator refs = resourceRefList.iterator();
      while (refs.hasNext())
      {
         ResourceRef ref = (ResourceRef) refs.next();

         if (ref.getResourceName() != null)
         {
            // for <resource-manager>
            ref.setJndiName(dd.resolveResourceManager(ref.getResourceName()));
            ref.setMappedName(dd.resolveResourceManager(ref.getResourceName()));
         }
      }
   }

   private void addMessageDestinationAnnotations(EJBContainer container,
         Collection destinationRefList)

   {
      Iterator refs = destinationRefList.iterator();
      while (refs.hasNext())
      {
         MessageDestinationRef ref = (MessageDestinationRef) refs.next();

         if (ref.getMappedName() == null || ref.getMappedName().equals(""))
         {
            AssemblyDescriptor descriptor = dd.getAssemblyDescriptor();
            if (descriptor != null)
            {
               MessageDestination destination = descriptor
                     .findMessageDestination(ref.getMessageDestinationLink());
               if (destination != null)
               {
                  ref.setMappedName(destination.getJndiName());
               }
            }
         }
      }
   }

   private void addInterceptorMethodAnnotations(EJBContainer container,
         EnterpriseBean enterpriseBean)
   {
      if (enterpriseBean instanceof SessionEnterpriseBean)
      {
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((SessionEnterpriseBean) enterpriseBean).getAroundInvoke(),
               AroundInvoke.class, "around-invoke-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((SessionEnterpriseBean) enterpriseBean).getPostConstruct(),
               PostConstruct.class, "post-construct-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((SessionEnterpriseBean) enterpriseBean).getPostActivate(),
               PostActivate.class, "post-activate-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((SessionEnterpriseBean) enterpriseBean).getPrePassivate(),
               PrePassivate.class, "pre-passivate-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((SessionEnterpriseBean) enterpriseBean).getPreDestroy(),
               PreDestroy.class, "pre-destroy-method");
      } else if (enterpriseBean instanceof MessageDrivenBean)
      {
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((MessageDrivenBean) enterpriseBean).getAroundInvoke(),
               AroundInvoke.class, "around-invoke-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((MessageDrivenBean) enterpriseBean).getPostConstruct(),
               PostConstruct.class, "post-construct-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               ((MessageDrivenBean) enterpriseBean).getPreDestroy(),
               PreDestroy.class, "pre-destroy-method");
      }
   }

   private void addInterceptorMethodAnnotation(EJBContainer container,
         EnterpriseBean enterpriseBean, Method method, Class ann, String xmlName)
   {
      if (method == null)
         return;

      java.lang.reflect.Method found = null;
      for (java.lang.reflect.Method rm : container.getBeanClass()
            .getDeclaredMethods())
      {
         if (rm.getName().equals(method.getMethodName()))
         {
            if (ann == AroundInvoke.class)
            {
               if (InterceptorInfoRepository.checkValidBusinessSignature(rm))
               {
                  found = rm;
                  break;
               }
            } else
            {
               if (InterceptorInfoRepository
                     .checkValidBeanLifecycleSignature(rm))
               {
                  found = rm;
                  break;
               }
            }
         }
      }

      if (found == null)
      {
         log.warn("No method found within " + container.getBeanClassName()
               + " with name " + method.getMethodName()
               + " with the right signature for " + xmlName + "was found");
         return;
      }

      if (container.resolveAnnotation(found, ann) == null)
      {
         log.debug("adding " + ann.getName() + " method annotation to "
               + ejbClass.getName() + "." + found.getName());

         container.getAnnotations().addAnnotation(found, ann,
               getInterceptorImpl(ann));
      }
   }

   private Object getInterceptorImpl(Class ann)
   {
      if (ann == AroundInvoke.class)
      {
         return new AroundInvokeImpl();
      } else if (ann == PostConstruct.class)
      {
         return new PostConstructImpl();
      } else if (ann == PostActivate.class)
      {
         return new PostActivateImpl();
      } else if (ann == PrePassivate.class)
      {
         return new PrePassivateImpl();
      } else if (ann == PreDestroy.class)
      {
         return new PreDestroyImpl();
      }

      return null;
   }

   private void addSecurityIdentityAnnotation(EJBContainer container,
         SecurityIdentity identity)
   {
      if (identity != null && !identity.isUseCallerIdentity())
      {
         RunAs runAs = identity.getRunAs();
         if (runAs != null)
         {
            RunAsImpl annotation = new RunAsImpl(runAs.getRoleName());
            addClassAnnotation(container, annotation.annotationType(),
                  annotation);

            String runAsPrincipal = identity.getRunAsPrincipal();
            if (runAsPrincipal != null)
            {
               RunAsPrincipalImpl principalAnnotation = new RunAsPrincipalImpl(
                     runAs.getRoleName());
               addClassAnnotation(container, principalAnnotation
                     .annotationType(), principalAnnotation);
            }
         }
      }
   }

   protected void overrideAnnotations(EJBContainer container, Member m,
         String annotation, Object value)
   {
      AnnotationRepository annotations = container.getAnnotations();

      if (value instanceof javax.annotation.security.DenyAll)
      {
         annotations.disableAnnotation(m,
               javax.annotation.security.PermitAll.class.getName());
         annotations.disableAnnotation(m,
               javax.annotation.security.RolesAllowed.class.getName());
      } else if (value instanceof javax.annotation.security.PermitAll)
      {
         annotations.disableAnnotation(m,
               javax.annotation.security.DenyAll.class.getName());
         annotations.disableAnnotation(m,
               javax.annotation.security.RolesAllowed.class.getName());
      } else if (value instanceof javax.annotation.security.RolesAllowed)
      {
         annotations.disableAnnotation(m,
               javax.annotation.security.PermitAll.class.getName());
         annotations.disableAnnotation(m,
               javax.annotation.security.DenyAll.class.getName());
      }
   }

   private void addClassAnnotation(EJBContainer container,
         Class annotationClass, Object annotation)
   {
      log.debug("adding class annotation " + annotationClass.getName() + " to "
            + ejbClass.getName() + " " + annotation);
      log.debug("adding class annotation " + annotationClass.getName() + " to "
            + ejbClass.getName() + " " + annotation);
      container.getAnnotations()
            .addClassAnnotation(annotationClass, annotation);
   }

   private void addAnnotations(Class annotationClass, Object annotation,
         EJBContainer container, Method method) throws ClassNotFoundException,
         NoSuchMethodException, NoSuchFieldException
   {
      String methodName = method.getMethodName();

      try
      {
         AnnotationRepository annotations = container.getAnnotations();
         if (methodName.equals("*"))
         {
            log.debug("adding " + annotationClass.getName() + " annotation to "
                  + ejbClass.getName() + "." + methodName);
   
            for (java.lang.reflect.Method declaredMethod : ejbClass
                  .getDeclaredMethods())
            {
               annotations.addAnnotation(declaredMethod, annotationClass,
                     annotation);
               overrideAnnotations(container, declaredMethod, annotationClass
                     .getName(), annotation);
            }
         } else
         {
            List params = method.getMethodParams();
            if (params == null)
            {
               java.lang.reflect.Method[] methods = ejbClass.getMethods();
               boolean foundMethod = false;
               for (int methodIndex = 0; methodIndex < methods.length; ++methodIndex)
               {
                  if (methods[methodIndex].getName().equals(methodName))
                  {
                     log.debug("adding " + annotationClass.getName()
                           + " method annotation to " + ejbClass.getName() + "."
                           + methodName);
                     annotations.addAnnotation(methods[methodIndex],
                           annotationClass, annotation);
                     overrideAnnotations(container, methods[methodIndex],
                           annotationClass.getName(), annotation);
                     foundMethod = true;
   
                  }
               }
   
               if (!foundMethod)
               {
                  methods = ejbClass.getDeclaredMethods();
                  for (int methodIndex = 0; methodIndex < methods.length; ++methodIndex)
                  {
                     if (methods[methodIndex].getName().equals(methodName))
                     {
                        log.debug("adding " + annotationClass.getName()
                              + " method annotation to " + ejbClass.getName()
                              + "." + methodName);
                        annotations.addAnnotation(methods[methodIndex],
                              annotationClass, annotation);
                        overrideAnnotations(container, methods[methodIndex],
                              annotationClass.getName(), annotation);
                        foundMethod = true;
   
                     }
                  }
               }
   
               if (!foundMethod)
               {
                  java.lang.reflect.Field member = ejbClass
                        .getDeclaredField(methodName);
                  if (member != null)
                  {
                     log.debug("adding " + annotationClass.getName()
                           + " field annotation to " + ejbClass.getName() + "."
                           + methodName);
                     annotations
                           .addAnnotation(member, annotationClass, annotation);
                     overrideAnnotations(container, member, annotationClass
                           .getName(), annotation);
                  }
               }
            } else
            {
               Class[] methodSignature = new Class[params.size()];
               Iterator paramIterator = params.iterator();
               int paramIndex = 0;
               while (paramIterator.hasNext())
               {
                  String param = (String) paramIterator.next();
                  Class paramClass = null;
                  if (param.equals("boolean"))
                     paramClass = boolean.class;
                  else if (param.equals("int"))
                     paramClass = int.class;
                  else if (param.equals("long"))
                     paramClass = long.class;
                  else if (param.equals("short"))
                     paramClass = short.class;
                  else if (param.equals("byte"))
                     paramClass = byte.class;
                  else if (param.equals("char"))
                     paramClass = char.class;
                  else
                     paramClass = di.getClassLoader().loadClass(param);
                  methodSignature[paramIndex++] = paramClass;
               }
               java.lang.reflect.Member member = ejbClass.getMethod(methodName,
                     methodSignature);
               log.debug("adding " + annotationClass.getName()
                     + " method annotation to " + ejbClass.getName() + "."
                     + methodName);
               annotations.addAnnotation(member, annotationClass, annotation);
               overrideAnnotations(container, member, annotationClass.getName(),
                     annotation);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create annotation from method/field " + method.getMethodName() + " for EJB " + container.getEjbName(), e);
      }
   }

   private static String getParameters(java.lang.reflect.Method m)
   {
      if (m.getParameterTypes().length == 0)
      {
         return "";
      }
      StringBuffer sb = new StringBuffer();
      boolean first = true;
      for (Class param : m.getParameterTypes())
      {
         if (!first)
         {
            sb.append(", ");
         } else
         {
            first = false;
         }
         sb.append(InterceptorInfoRepository.simpleType(param));
      }
      return sb.toString();
   }

}
