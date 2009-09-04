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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javassist.bytecode.ClassFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.Init;
import javax.ejb.Local;
import javax.ejb.MessageDriven;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;

import org.jboss.aop.annotation.AnnotationRepository;
import org.jboss.ejb3.annotation.Cache;
import org.jboss.ejb3.annotation.Clustered;
import org.jboss.ejb3.annotation.Consumer;
import org.jboss.ejb3.annotation.CurrentMessage;
import org.jboss.ejb3.annotation.DefaultActivationSpecs;
import org.jboss.ejb3.annotation.DeliveryMode;
import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.IgnoreDependency;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.MessageProperties;
import org.jboss.ejb3.annotation.PersistenceManager;
import org.jboss.ejb3.annotation.Pool;
import org.jboss.ejb3.annotation.Producers;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;
import org.jboss.ejb3.annotation.RemoteHomeBinding;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.SerializedConcurrentAccess;
import org.jboss.ejb3.annotation.Service;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.jboss.ejb3.annotation.defaults.PoolDefaults;
import org.jboss.ejb3.annotation.impl.ActivationConfigPropertyImpl;
import org.jboss.ejb3.annotation.impl.AroundInvokeImpl;
import org.jboss.ejb3.annotation.impl.CacheImpl;
import org.jboss.ejb3.annotation.impl.ClusteredImpl;
import org.jboss.ejb3.annotation.impl.ConsumerImpl;
import org.jboss.ejb3.annotation.impl.CurrentMessageImpl;
import org.jboss.ejb3.annotation.impl.DeclareRolesImpl;
import org.jboss.ejb3.annotation.impl.DefaultActivationSpecsImpl;
import org.jboss.ejb3.annotation.impl.DenyAllImpl;
import org.jboss.ejb3.annotation.impl.DependsImpl;
import org.jboss.ejb3.annotation.impl.ExcludeDefaultInterceptorsImpl;
import org.jboss.ejb3.annotation.impl.IgnoreDependencyImpl;
import org.jboss.ejb3.annotation.impl.InitImpl;
import org.jboss.ejb3.annotation.impl.InterceptorsImpl;
import org.jboss.ejb3.annotation.impl.LocalBindingImpl;
import org.jboss.ejb3.annotation.impl.LocalHomeBindingImpl;
import org.jboss.ejb3.annotation.impl.LocalHomeImpl;
import org.jboss.ejb3.annotation.impl.LocalImpl;
import org.jboss.ejb3.annotation.impl.ManagementImpl;
import org.jboss.ejb3.annotation.impl.MessageDrivenImpl;
import org.jboss.ejb3.annotation.impl.MessagePropertiesImpl;
import org.jboss.ejb3.annotation.impl.PermitAllImpl;
import org.jboss.ejb3.annotation.impl.PersistenceManagerImpl;
import org.jboss.ejb3.annotation.impl.PoolImpl;
import org.jboss.ejb3.annotation.impl.PostActivateImpl;
import org.jboss.ejb3.annotation.impl.PostConstructImpl;
import org.jboss.ejb3.annotation.impl.PreDestroyImpl;
import org.jboss.ejb3.annotation.impl.PrePassivateImpl;
import org.jboss.ejb3.annotation.impl.ProducerImpl;
import org.jboss.ejb3.annotation.impl.ProducersImpl;
import org.jboss.ejb3.annotation.impl.RemoteBindingImpl;
import org.jboss.ejb3.annotation.impl.RemoteBindingsImpl;
import org.jboss.ejb3.annotation.impl.RemoteHomeBindingImpl;
import org.jboss.ejb3.annotation.impl.RemoteHomeImpl;
import org.jboss.ejb3.annotation.impl.RemoteImpl;
import org.jboss.ejb3.annotation.impl.RemoveImpl;
import org.jboss.ejb3.annotation.impl.ResourceAdapterImpl;
import org.jboss.ejb3.annotation.impl.ResourceImpl;
import org.jboss.ejb3.annotation.impl.RolesAllowedImpl;
import org.jboss.ejb3.annotation.impl.RunAsImpl;
import org.jboss.ejb3.annotation.impl.RunAsPrincipalImpl;
import org.jboss.ejb3.annotation.impl.SecurityDomainImpl;
import org.jboss.ejb3.annotation.impl.SerializedConcurrentAccessImpl;
import org.jboss.ejb3.annotation.impl.ServiceImpl;
import org.jboss.ejb3.annotation.impl.StatefulImpl;
import org.jboss.ejb3.annotation.impl.StatelessImpl;
import org.jboss.ejb3.annotation.impl.TransactionAttributeImpl;
import org.jboss.ejb3.annotation.impl.TransactionManagementImpl;
import org.jboss.ejb3.annotation.impl.TransactionTimeoutImpl;
import org.jboss.ejb3.common.classloader.util.PrimitiveClassLoadingUtil;
import org.jboss.ejb3.common.lang.ClassHelper;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.mdb.ConsumerContainer;
import org.jboss.ejb3.mdb.MDB;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.service.ServiceContainer;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.CacheConfigMetaData;
import org.jboss.metadata.ejb.jboss.ClusterConfigMetaData;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossConsumerBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossGenericBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.LocalProducerMetaData;
import org.jboss.metadata.ejb.jboss.MessagePropertiesMetaData;
import org.jboss.metadata.ejb.jboss.MethodAttributeMetaData;
import org.jboss.metadata.ejb.jboss.MethodAttributesMetaData;
import org.jboss.metadata.ejb.jboss.PoolConfigMetaData;
import org.jboss.metadata.ejb.jboss.ProducerMetaData;
import org.jboss.metadata.ejb.jboss.RemoteBindingMetaData;
import org.jboss.metadata.ejb.jboss.ResourceManagerMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigPropertyMetaData;
import org.jboss.metadata.ejb.spec.AroundInvokeMetaData;
import org.jboss.metadata.ejb.spec.AroundInvokesMetaData;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.jboss.metadata.ejb.spec.ContainerTransactionMetaData;
import org.jboss.metadata.ejb.spec.ExcludeListMetaData;
import org.jboss.metadata.ejb.spec.InitMethodMetaData;
import org.jboss.metadata.ejb.spec.InitMethodsMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingsMetaData;
import org.jboss.metadata.ejb.spec.InterceptorClassesMetaData;
import org.jboss.metadata.ejb.spec.MethodMetaData;
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;
import org.jboss.metadata.ejb.spec.MethodPermissionMetaData;
import org.jboss.metadata.ejb.spec.MethodPermissionsMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;
import org.jboss.metadata.ejb.spec.RemoveMethodMetaData;
import org.jboss.metadata.ejb.spec.RemoveMethodsMetaData;
import org.jboss.metadata.ejb.spec.SecurityIdentityMetaData;
import org.jboss.metadata.ejb.spec.SubscriptionDurability;
import org.jboss.metadata.javaee.jboss.AnnotationMetaData;
import org.jboss.metadata.javaee.jboss.AnnotationPropertiesMetaData;
import org.jboss.metadata.javaee.jboss.AnnotationPropertyMetaData;
import org.jboss.metadata.javaee.jboss.AnnotationsMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferencesMetaData;
import org.jboss.metadata.javaee.spec.PortComponent;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metadata.javaee.spec.RunAsMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.wsf.spi.metadata.j2ee.PortComponentMD;
import org.jboss.wsf.spi.metadata.j2ee.PortComponentSpec;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @author <a href="mailto:bill@jboss.com">Bill Burke</a>
 */
public class Ejb3DescriptorHandler extends Ejb3AnnotationHandler
{
   private static final Logger log = Logger
         .getLogger(Ejb3DescriptorHandler.class);

   protected JBossMetaData dd;

   protected List<JBossEnterpriseBeanMetaData> ejbs = new ArrayList<JBossEnterpriseBeanMetaData>();

   public Ejb3DescriptorHandler(Ejb3Deployment deployment, ClassFile cf, JBossMetaData dd)
   {
      super(deployment, cf);
      assert dd != null : "dd is null";
      this.dd = dd;
   }
   
   public Ejb3DescriptorHandler(Ejb3Deployment deployment, JBossMetaData dd)
   {
      super(deployment);
      assert dd != null : "dd is null";
      this.dd = dd;
   }

   /**
    * @param original
    * @param binding
    * @return
    */
   private Interceptors createInterceptorsFromInterceptorBinding(Interceptors original, InterceptorBindingMetaData binding) throws ClassNotFoundException
   {
      assert binding != null : "binding is null";
      
      InterceptorsImpl impl;
      InterceptorClassesMetaData classes;
      if (binding.isTotalOrdering())
      {
         impl = new InterceptorsImpl();
         classes = binding.getInterceptorOrder();
      }
      else
      {
         impl = InterceptorsImpl.getImpl(original);
         classes = binding.getInterceptorClasses();
      }
      if (classes != null)
      {
         for (String name : classes)
         {
            Class<?> clazz = di.getClassLoader().loadClass(name);
            impl.addValue(clazz);
         }
      }
      return impl;
   }
   
   /**
    * Find all enterprise beans using the given ejb class.
    * 
    * @param dd         the dd to search in
    * @param className  the ejb class to find
    * @return           a list of enterprise beans, never null
    */
   private List<JBossEnterpriseBeanMetaData> findEjbsByClass(JBossMetaData dd, String className)
   {
      assert dd != null : "dd is null";
      assert className != null : "className is null";
      
      List<JBossEnterpriseBeanMetaData> result = new ArrayList<JBossEnterpriseBeanMetaData>();
      
      JBossEnterpriseBeansMetaData enterpriseBeans = dd.getEnterpriseBeans();
      if(enterpriseBeans == null)
         return result;
      
      for(JBossEnterpriseBeanMetaData bean : enterpriseBeans)
      {
         if(bean.getEjbClass() != null && bean.getEjbClass().equals(className))
            result.add(bean);
      }
      
      return result;
   }
   
   public boolean isEjb()
   {
      if (super.isEjb())
         return true;
      
      JBossEnterpriseBeansMetaData enterpriseBeans = dd.getEnterpriseBeans();
      if (enterpriseBeans == null)
      {
         return false;
      }
      
      return findEjbsByClass(dd, cf.getName()).size() > 0;
   }

   /**
    * Helper method to load classes. If no class name is specified
    * the bean class is returned.
    * 
    * @param container  The EJB container
    * @param name       The name of the class or null for the bean class
    * @return           The resulting <tt>Class</tt> object or the bean class
    */
   private Class<?> loadClass(EJBContainer container, String name)
   {
      if(name == null)
         return container.getBeanClass();
      try
      {
         return di.getClassLoader().loadClass(name);
      }
      catch(ClassNotFoundException e)
      {
         // TODO: what shall be the proper exception
         throw new RuntimeException(e);
      }
   }
   
   protected void populateBaseInfo() throws Exception
   {
      super.populateBaseInfo();

      List<JBossEnterpriseBeanMetaData> ejbsByClass = findEjbsByClass(dd, cf.getName());

      for (int i = 0; i < ejbNames.size(); ++i)
      {
         String ejbNameFromAnnotation = ejbNames.get(i);
         ejbs.add(dd.getEnterpriseBean(ejbNameFromAnnotation));

         boolean removed = false;
         int j = 0;
         while (!removed && j < ejbsByClass.size())
         {
            JBossEnterpriseBeanMetaData ejbByClass = ejbsByClass.get(j);
            if (ejbByClass.getEjbName().equals(ejbNameFromAnnotation))
            {
               ejbsByClass.remove(j);
            } else
               ++j;
         }
      }

      for (JBossEnterpriseBeanMetaData enterpriseBean : ejbsByClass)
      {
         String ejbName = enterpriseBean.getEjbName();

         ejbs.add(enterpriseBean);
         ejbNames.add(ejbName);
         
         ejbType = getEjbType(enterpriseBean);
      }
   }
   
   protected EJB_TYPE getEjbType(JBossEnterpriseBeanMetaData enterpriseBean)
   {
      if (enterpriseBean.isSession())
      {
         if (((JBossSessionBeanMetaData) enterpriseBean).isStateless())
            return EJB_TYPE.STATELESS;
         else
            return EJB_TYPE.STATEFUL;
      } else if (enterpriseBean.isEntity())
         return EJB_TYPE.ENTITY;
      else if (enterpriseBean.isMessageDriven())
         return EJB_TYPE.MESSAGE_DRIVEN;
      else if (enterpriseBean.isService())
         return EJB_TYPE.SERVICE;
      else if (enterpriseBean.isConsumer())
         return EJB_TYPE.CONSUMER;
      else
         throw new IllegalStateException("unknown bean type encountered " + enterpriseBean);
   }
   
   public List<Container> getContainers(Ejb3Deployment deployment, Map<String, Container> preexistingContainers) throws Exception
   {     
      List<Container> containers = new ArrayList<Container>();

      JBossEnterpriseBeansMetaData allXmlEjbs = (dd.getEnterpriseBeans() != null) ? dd.getEnterpriseBeans() : new JBossEnterpriseBeansMetaData();
      
      ejbNames = new ArrayList<String>();
      for (JBossEnterpriseBeanMetaData ejb : allXmlEjbs)
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
         JBossEnterpriseBeanMetaData enterpriseBean = ejbs.get(ejbIndex);
         ejbType = getEjbType(enterpriseBean);
         className = enterpriseBean.getEjbClass();
         
         if (className == null)
            log.warn("Descriptor based bean has no ejb-class defined: " + ejbName);
         else
         {
            ejbClass = di.getClassLoader().loadClass(className);
            if (ejbType == EJB_TYPE.STATELESS)
            {
               EJBContainer container = getStatelessContainer(ejbIndex, (JBossSessionBeanMetaData) enterpriseBean);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.STATEFUL)
            {
               StatefulContainer container = getStatefulContainer(ejbIndex, (JBossSessionBeanMetaData) enterpriseBean);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.MESSAGE_DRIVEN)
            {
               MDB container = getMDB(ejbIndex, (JBossMessageDrivenBeanMetaData) enterpriseBean);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.SERVICE)
            {
               ServiceContainer container = getServiceContainer(ejbIndex, (JBossServiceBeanMetaData) enterpriseBean);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            else if (ejbType == EJB_TYPE.CONSUMER)
            {
               ConsumerContainer container = getConsumerContainer(ejbIndex, (JBossConsumerBeanMetaData) enterpriseBean);
               container.setJaccContextId(getJaccContextId());
               containers.add(container);
            }
            log.debug("found EJB3: ejbName=" + ejbName + ", class=" + className + ", type=" + ejbType);
         }
      }
      
      return containers;
   }

   @Override
   protected StatefulContainer getStatefulContainer(int ejbIndex, JBossSessionBeanMetaData enterpriseBean)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      StatefulContainer container = super.getStatefulContainer(ejbIndex, enterpriseBean);

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      StatefulImpl annotation = new StatefulImpl(ejbName);
      if (enterpriseBean != null && !isAnnotatedBean())
      {
         addClassAnnotation(container, annotation);
      }

      if(enterpriseBean instanceof JBossSessionBeanMetaData)
         addInterfaces(container, (JBossSessionBeanMetaData) enterpriseBean);
      else
         log.trace("Not analyzing interfaces on " + enterpriseBean);

      addDescriptorAnnotations(container, enterpriseBean, ejbName, true);

      return container;
   }

   private void addHomeAnnotations(EJBContainer container,
         JBossSessionBeanMetaData sessionBean) throws Exception
   {
      if (sessionBean.getHome() != null)
      {
         RemoteHomeImpl annotation = new RemoteHomeImpl(di.getClassLoader()
               .loadClass(sessionBean.getHome()));
         addClassAnnotation(container, annotation.annotationType(), annotation);
      }

      if (sessionBean.getLocalHome() != null)
      {
         LocalHomeImpl annotation = new LocalHomeImpl(di.getClassLoader()
               .loadClass(sessionBean.getLocalHome()));
         addClassAnnotation(container, annotation.annotationType(), annotation);
      }
   }

   @Override
   protected EJBContainer getStatelessContainer(int ejbIndex, JBossSessionBeanMetaData enterpriseBean)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      EJBContainer container = super.getStatelessContainer(ejbIndex, enterpriseBean);

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      StatelessImpl annotation = new StatelessImpl(ejbName);
      if (enterpriseBean != null && !isAnnotatedBean())
      {
         addClassAnnotation(container, Stateless.class, annotation);
      }

      if(enterpriseBean instanceof JBossSessionBeanMetaData)
         addInterfaces(container, (JBossSessionBeanMetaData) enterpriseBean);
      else
         log.debug("Not analyzing interfaces on " + enterpriseBean);

      addDescriptorAnnotations(container, enterpriseBean, ejbName);

      return container;
   }

   @Override
   protected ServiceContainer getServiceContainer(int ejbIndex, JBossServiceBeanMetaData service)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      ServiceContainer container = super.getServiceContainer(ejbIndex, service);
      ServiceImpl annotation = new ServiceImpl((Service) container
            .resolveAnnotation(Service.class));

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      if (service != null && !isAnnotatedBean())
      {
         if (service.getObjectName() != null)
            annotation.setObjectName(service.getObjectName());
         if (service.getEjbName() != null)
            annotation.setName(service.getEjbName());
         if (service.getXmbean() != null)
            annotation.setXMBean(service.getXmbean());
         addClassAnnotation(container, Service.class, annotation);
      }

      addInterfaces(container, service);

      addDescriptorAnnotations(container, service, ejbName);

      addServiceAnnotations(container, service);

      return container;
   }

   @Override
   protected ConsumerContainer getConsumerContainer(int ejbIndex, JBossConsumerBeanMetaData consumer)
         throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      ConsumerContainer container = super.getConsumerContainer(ejbIndex, consumer);
      ConsumerImpl annotation = new ConsumerImpl((Consumer) container
            .resolveAnnotation(Consumer.class));

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      if (consumer != null && !isAnnotatedBean())
      {
         if (consumer.getMessageDestination() != null)
         {
            ActivationConfigPropertyImpl property = new ActivationConfigPropertyImpl(
                  "destination", consumer.getMessageDestination());
            annotation.addActivationConfig(property);
         }

         if (consumer.getMessageDestinationType() != null)
         {
            ActivationConfigPropertyImpl property = new ActivationConfigPropertyImpl(
                  "destinationType", consumer.getMessageDestinationType());
            annotation.addActivationConfig(property);
         }

         addClassAnnotation(container, Consumer.class, annotation);
      }

      // A consumer bean doesn't have any business interfaces
      //addInterfaces(container, consumer);

      addDescriptorAnnotations(container, consumer, ejbName);

      addConsumerAnnotations(container, consumer);

      return container;
   }

   protected String getMDBDomainName(int ejbIndex)
   {
      return defaultMDBDomain;
   }

   @Override
   protected MDB getMDB(int ejbIndex, JBossMessageDrivenBeanMetaData enterpriseBean) throws Exception
   {
      String ejbName = ejbNames.get(ejbIndex);

      MDB container = super.getMDB(ejbIndex, enterpriseBean);

      container.setAssemblyDescriptor(dd.getAssemblyDescriptor());

      if(enterpriseBean instanceof JBossMessageDrivenBeanMetaData)
         addMDBAnnotations(container, ejbName, (JBossMessageDrivenBeanMetaData) enterpriseBean);
      /*
      else if(enterpriseBean instanceof JBossGenericBeanMetaData)
      {
         // EJBTHREE-936: TODO: unsupported wickedness starts here
         JBossMessageDrivenBeanMetaData mdb = new JBossMessageDrivenBeanMetaData();
         mdb.setDestinationJndiName(enterpriseBean.getMappedName());
         
         addMDBAnnotations(container, ejbName, mdb);
      }
      */

      // An MDB doesn't have business interfaces, or does it?
      //addInterfaces(container, enterpriseBean);

      addDescriptorAnnotations(container, enterpriseBean, ejbName);

      return container;
   }

   protected String getAspectDomain(int ejbIndex, String defaultDomain)
   {
      JBossEnterpriseBeanMetaData enterpriseBean = ejbs.get(ejbIndex);
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

   private void addMDBAnnotations(MDB container, String ejbName, JBossMessageDrivenBeanMetaData mdb)
   {
      if (mdb != null)
      {
         if (mdb.getResourceAdapterName() != null)
         {
            ResourceAdapter adapter = new ResourceAdapterImpl(mdb.getResourceAdapterName());
            addClassAnnotation(container, ResourceAdapter.class, adapter);
         }

         ArrayList<ActivationConfigProperty> properties = new ArrayList<ActivationConfigProperty>();

         if (mdb.isJMS())
         {
            if (mdb.getAcknowledgeMode() != null)
               properties.add(new ActivationConfigPropertyImpl("acknowledgeMode", mdb.getAcknowledgeMode()));

            if(mdb.getMessageDestinationType() != null)
            {
               properties.add(new ActivationConfigPropertyImpl("destinationType", mdb.getMessageDestinationType()));
            }
            SubscriptionDurability subscriptionDurability = mdb.getSubscriptionDurability();
            if(subscriptionDurability != null)
            {
               String durable = "false";
               if (subscriptionDurability.equals(SubscriptionDurability.Durable))
                  durable = "true";
               properties.add(new ActivationConfigPropertyImpl("subscriptionDurability", durable));
               if (subscriptionDurability.equals(SubscriptionDurability.Durable))
                  properties.add(new ActivationConfigPropertyImpl("subscriptionName", "subscriptionName"));
            }

            // prefer jndi name over message destination link
            if (mdb.getDestinationJndiName() != null)
            {
               properties.add(new ActivationConfigPropertyImpl("destination", mdb
                     .getDestinationJndiName()));
            }
            else if(mdb.getMessageDestinationLink() != null)
            {
               log.warn("Message destination link on a MDB is not yet implemented, specify a jndi name in jboss.xml");
               /*
               // TODO: I can't resolve here, because we're still scanning, maybe this will work:
               // This will be picked up by MessagingContainer and then resolved to a jndi name
               properties.add(new ActivationConfigPropertyImpl("destinationLink", mdb.getMessageDestinationLink()));
               */
            }
            
            if (mdb.getMdbSubscriptionId() != null)
            {
               properties.add(new ActivationConfigPropertyImpl("subscriptionName", mdb
                     .getMdbSubscriptionId()));

            }

            // FIXME These properties are only for our jmsra.rar
            
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
         }

         ActivationConfigMetaData activationConfig = mdb.getActivationConfig();
         if (activationConfig != null && activationConfig.getActivationConfigProperties() != null)
         {
            for (ActivationConfigPropertyMetaData property : activationConfig.getActivationConfigProperties())
            {
               properties.add(new ActivationConfigPropertyImpl(property
                     .getName(), property.getValue()));
            }
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
            annotation.merge(ejbClass.getAnnotation(MessageDriven.class));
         }

         addClassAnnotation(container, MessageDriven.class, annotation);

         addDefaultActivationConfig(container, mdb);
      }
   }

   private void addDefaultActivationConfig(MDB container, JBossMessageDrivenBeanMetaData mdb)
   {
      ActivationConfigMetaData defaultActivationConfig = mdb.getDefaultActivationConfig();
      if (defaultActivationConfig != null)
      {
         DefaultActivationSpecsImpl activationAnnotation = new DefaultActivationSpecsImpl();
         for (ActivationConfigPropertyMetaData property : defaultActivationConfig.getActivationConfigProperties())
         {
            activationAnnotation.addActivationConfigProperty(new ActivationConfigPropertyImpl(property
                  .getName(), property.getValue()));
         }

         DefaultActivationSpecs existingAnnotation = ejbClass.getAnnotation(DefaultActivationSpecs.class);
         if (existingAnnotation != null)
            activationAnnotation.merge(existingAnnotation);

         addClassAnnotation(container, DefaultActivationSpecs.class, activationAnnotation);
      }
   }

   private void addInterfaces(EJBContainer container, JBossSessionBeanMetaData enterpriseBean) throws ClassNotFoundException
   {
      if (enterpriseBean != null)
      {
         // Initialize
         List<Class<?>> localClasses = new ArrayList<Class<?>>();
         List<Class<?>> remoteClasses = new ArrayList<Class<?>>();
         List<String> localClassNames = new ArrayList<String>();
         List<String> remoteClassNames = new ArrayList<String>();
         
         // Obtain business interfaces (local and remote)
         BusinessLocalsMetaData businessLocals = enterpriseBean.getBusinessLocals();
         BusinessRemotesMetaData businessRemotes = enterpriseBean.getBusinessRemotes();
         
         // Obtain local and remote interfaces
         String local = enterpriseBean.getLocal();
         String remote = enterpriseBean.getRemote();
         
         // If business locals are defined
         if (businessLocals != null)
         {
            localClassNames.addAll(businessLocals);
         }

         // If business remotes are defined
         if (businessRemotes != null)
         {
            remoteClassNames.addAll(businessRemotes);
         }
         
         // If local interface(s) is/are defined
         if (local != null)
         {
            // Add all defines local interfaces to list
            StringTokenizer classes = new StringTokenizer(local, ",");
            while (classes.hasMoreTokens())
            {
               String token = classes.nextToken();
               String classname = token.trim();
               localClassNames.add(classname);
            }
         }
         
         // If remote interface(s) is/are defined
         if (remote != null)
         {
            // Add all defined remote interfaces to list
            StringTokenizer classes = new StringTokenizer(remote, ",");
            while (classes.hasMoreTokens())
            {
               String token = classes.nextToken();
               String classname = token.trim();
               remoteClassNames.add(classname);
            }
         }
         
         // For each of the local and business local interfaces
         for (String localClassName : localClassNames)
         {
            // Obtain class
            Class<?> localClass = di.getClassLoader().loadClass(localClassName);

            // Ensure specified class is an interface
            if (!localClass.isInterface())
            {
               throw new RuntimeException("Specified class for @Local " + localClass.getName()
                     + " is not an interface");
            }

            // Log and add the business remote interface to the list of classes to be added as @Local
            log.debug("Adding @Local interface " + localClass.getName() + " as specified in metadata");
            localClasses.add(localClass);
         }
         
         // For each of the remote and  business remote interfaces
         for (String remoteClassName : remoteClassNames)
         {
            // Obtain class
            Class<?> remoteClass = di.getClassLoader().loadClass(remoteClassName);

            // Ensure specified class is an interface
            if (!remoteClass.isInterface())
            {
               throw new RuntimeException("Specified class for @Remote " + remoteClass.getName()
                     + " is not an interface");
            }

            // Log and add the business remote interface to the list
            log.debug("Adding @Remote interface " + remoteClass.getName() + " as specified in metadata");
            remoteClasses.add(remoteClass);
         }

         // Add @Local to local and local business interfaces
         if (localClasses.size() > 0)
         {
            Class<?>[] lIntfs = new Class[localClasses.size()];
            lIntfs = localClasses.toArray(lIntfs);
            addClassAnnotation(container, Local.class, new LocalImpl(lIntfs));
         }

         // Add @Remote to remote and remote business interfaces
         if (remoteClasses.size() > 0)
         {
            Class<?>[] rIntfs = new Class[remoteClasses.size()];
            rIntfs = remoteClasses.toArray(rIntfs);
            addClassAnnotation(container, Remote.class, new RemoteImpl(rIntfs));
         }
      }
   }

   /**
    * Add descriptor annotations on non stateful session beans.
    * 
    * @param container
    * @param enterpriseBean
    * @param ejbName
    * @throws Exception
    */
   private void addDescriptorAnnotations(EJBContainer container, JBossEnterpriseBeanMetaData enterpriseBean, String ejbName) throws Exception
   {
      addDescriptorAnnotations(container, enterpriseBean, ejbName, false);
   }
   
   private void addDescriptorAnnotations(EJBContainer container, JBossEnterpriseBeanMetaData enterpriseBean, String ejbName, boolean isStateful) throws Exception
   {
      // EJBTHREE-936: TODO: another wicked patch: jndi-name might mean local-jndi-name
      // TODO: Make sure this is done after addInterfaces!
      if(enterpriseBean instanceof JBossGenericBeanMetaData)
      {
         Class<?>[] remoteAndBusinessRemoteInterfaces = ProxyFactoryHelper.getRemoteAndBusinessRemoteInterfaces(container);
         if(remoteAndBusinessRemoteInterfaces.length == 0)
         {
            enterpriseBean.setLocalJndiName(enterpriseBean.getMappedName());
            enterpriseBean.setMappedName(null);
         }
      }
      
      addTransactionAnnotations(container, enterpriseBean, ejbName);

      addAssemblyAnnotations(container, enterpriseBean, ejbName);

      addSecurityAnnotations(container, enterpriseBean, ejbName);

      addEjbAnnotations(container, enterpriseBean);

      addEjb21Annotations(container, isStateful);
      
      addWebServiceAnnotations(container, enterpriseBean);
   }

   /**
    * EJB3 4.3.5
    * On a 2.1 session bean the ejbRemove is treated as PreDestroy, ejbActivate as PostActivate,
    * and ejbPassivate as PrePassivate. If it is a stateless session bean the ejbCreate is treated
    * as PostConstruct, if it is stateful the ejbCreate is treated as Init.
    * 
    * @param container
    * @param enterpriseBean
    * @throws Exception
    */
   private void addEjb21Annotations(EJBContainer container, boolean isStateful) throws Exception
   {
      if(javax.ejb.SessionBean.class.isAssignableFrom(ejbClass))
      {
         MethodMetaData method = new MethodMetaData();
         method.setEjbName(container.getEjbName());

         Annotation annotation;
         Class<? extends Annotation> annotationClass;
         // EJB3 4.6.2: The class may implement the ejbCreate method(s).
         // EJB3 4.6.4: The method must be declared as public.
         if(hasPublicMethod(ejbClass, "ejbCreate"))
         {
            if(isStateful)
            {
               annotation = new InitImpl();
            }
            else
            {
               annotation = new PostConstructImpl();
            }
            annotationClass = annotation.annotationType();
            method.setMethodName("ejbCreate");
            addAnnotations(annotationClass, annotation, container, method);
         }

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
            
         annotation = new ResourceImpl();
         annotationClass = Resource.class;
         method.setMethodName("setSessionContext");
         // TODO: set param?
         addAnnotations(annotationClass, annotation, container, method);
      }
   }

   private void addAssemblyAnnotations(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean, String ejbName) throws Exception
   {
      JBossAssemblyDescriptorMetaData assembly = dd.getAssemblyDescriptor();
      if (assembly != null)
      {
         addExcludeAnnotations(container, assembly.getExcludeList(), ejbName);

//         addInterceptorBindingAnnotations(container, enterpriseBean, ejbName);
      }

      if (enterpriseBean instanceof JBossSessionBeanMetaData)
      {
         JBossSessionBeanMetaData sessionBean = (JBossSessionBeanMetaData) enterpriseBean;
         addInitAnnotations(container, sessionBean.getInitMethods(), ejbName);
         addRemoveAnnotations(container, sessionBean.getRemoveMethods(), ejbName);
      }
   }

   private void addExcludeAnnotations(EJBContainer container, ExcludeListMetaData list, String ejbName) 
      throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException
   {
      if (list != null && list.getMethods() != null)
      {
         for(MethodMetaData method : list.getMethods())
         {
            if (method.getEjbName().equals(ejbName))
            {
               DenyAllImpl annotation = new DenyAllImpl();
               addAnnotations(DenyAll.class, annotation, container, method);
            }
         }
      }
   }

   private void addInitAnnotations(EJBContainer container, InitMethodsMetaData list, String ejbName)
      throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException
   {
      if (list != null)
      {
         for (InitMethodMetaData initMethod : list)
         {
            NamedMethodMetaData method = initMethod.getBeanMethod();
            InitImpl annotation = new InitImpl();
            addAnnotations(Init.class, annotation, container, method);
         }
      }
   }

   private void addRemoveAnnotations(EJBContainer container, RemoveMethodsMetaData list, String ejbName)
         throws ClassNotFoundException, NoSuchMethodException,
         NoSuchFieldException
   {
      if (list != null)
      {
         for (RemoveMethodMetaData removeMethod : list)
         {
            NamedMethodMetaData method = removeMethod.getBeanMethod();
            RemoveImpl annotation = new RemoveImpl(removeMethod.isRetainIfException());
            addAnnotations(Remove.class, annotation, container, method);
         }
      }
   }

   private void addSecurityAnnotations(EJBContainer container, JBossEnterpriseBeanMetaData enterpriseBean, String ejbName)
      throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException
   {
      JBossAssemblyDescriptorMetaData assembly = dd.getAssemblyDescriptor();
      if (assembly != null)
      {
         SecurityRolesMetaData securityRoles = assembly.getSecurityRoles();

         if (securityRoles != null && securityRoles.size() > 0)
         {
            List<String> roleList = new ArrayList<String>();
            for (SecurityRoleMetaData securityRole : securityRoles)
            {
               roleList.add(securityRole.getRoleName());

            }
            DeclareRolesImpl annotation = new DeclareRolesImpl(roleList.toArray(new String[roleList.size()]));
            addClassAnnotation(container, DeclareRoles.class, annotation);
         }

         MethodPermissionsMetaData methodPermissions = assembly.getMethodPermissions();
         if(methodPermissions != null)
         {
            for (MethodPermissionMetaData permission : methodPermissions)
            {
               for (MethodMetaData method : permission.getMethods())
               {
                  if (method.getEjbName().equals(ejbName))
                  {
                     if (permission.isNotChecked())
                     {
                        PermitAllImpl annotation = new PermitAllImpl();
                        addAnnotations(PermitAll.class, annotation, container, method);
                     } else
                     {
                        RolesAllowedImpl annotation = new RolesAllowedImpl();
   
                        for (String roleName : permission.getRoles())
                        {
                           annotation.addValue(roleName);
                        }
                        
                        // Log and add
                        log.debug("Adding @" + RolesAllowed.class.getSimpleName() + " for method "
                              + method.getMethodName() + "("
                              + method.getMethodParams() + ") of EJB " + method.getEjbName() + ": "
                              + Arrays.asList(annotation.value()));
                        addAnnotations(RolesAllowed.class, annotation, container, method);
                     }
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
         SecurityDomain annotation = ejbClass.getAnnotation(SecurityDomain.class);
         SecurityDomainImpl override;
         if (annotation != null)
         {
            override = new SecurityDomainImpl(annotation.value());
            override.setUnauthenticatedPrincipal(dd
                  .getUnauthenticatedPrincipal());
         }
         else
         {
            override = new SecurityDomainImpl();
            override.setUnauthenticatedPrincipal(dd.getUnauthenticatedPrincipal());
         }
         addClassAnnotation(container, override.annotationType(), override);
      }
   }

   private void addTransactionAnnotations(EJBContainer container, JBossEnterpriseBeanMetaData enterpriseBean, String ejbName)
      throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException
   {
      if (enterpriseBean != null)
      {
         TransactionManagementType transactionType = enterpriseBean.getTransactionType();
         if (transactionType != null)
         {
            TransactionManagementImpl annotation = new TransactionManagementImpl();
            annotation.setValue(transactionType);
            addClassAnnotation(container, TransactionManagement.class, annotation);
         }

         MethodAttributesMetaData attributes = enterpriseBean.getMethodAttributes();
         if (attributes != null)
         {
            for(MethodAttributeMetaData method : attributes)
            {
               TransactionTimeout timeoutAnnotation = new TransactionTimeoutImpl(method.getTransactionTimeout());
               addAnnotations(TransactionTimeout.class, timeoutAnnotation, container, method.getMethodName(), null);
            }
         }
      }

      JBossAssemblyDescriptorMetaData descriptor = dd.getAssemblyDescriptor();
      if (descriptor != null && descriptor.getContainerTransactions() != null)
      {
         for(ContainerTransactionMetaData transaction : descriptor.getContainerTransactions())
         {
            for(MethodMetaData method : transaction.getMethods())
            {
               if (method.getEjbName().equals(ejbName))
               {
                  TransactionAttributeImpl annotation = new TransactionAttributeImpl();
                  annotation.setType(transaction.getTransAttribute());
                  addAnnotations(TransactionAttribute.class, annotation, container, method);
               }
            }
         }
      }
   }

   /**
    * Interceptors are additive. What's in the annotations and in the XML is
    * merged
    */
   private void addInterceptorBindingAnnotations(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean, String ejbName)
         throws ClassNotFoundException, NoSuchMethodException,
         NoSuchFieldException
   {
      boolean definesInterceptors = false;

      InterceptorBindingsMetaData interceptorBindings = dd.getAssemblyDescriptor().getInterceptorBindings();
      if(interceptorBindings != null)
      {
         for (InterceptorBindingMetaData binding : interceptorBindings)
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
               if(binding.getMethod() == null)
               {
                  addClassLevelInterceptorBindingAnnotations(container, binding);
                  definesInterceptors = true;
               } 
               else
               {
                  definesInterceptors = addMethodLevelInterceptorBindingAnnotations(
                        container, binding);
               }
   
            }
         }
      }

      /*
      if (!definesInterceptors
            && di.getInterceptorInfoRepository().hasDefaultInterceptors())
      {
         addClassAnnotation(container, DefaultInterceptorMarker.class,
               new DefaultInterceptorMarkerImpl());
      }
      */
   }

   /**
    * Interceptors are additive. What's in the annotations and in the XML is
    * merged
    */
   private void addClassLevelInterceptorBindingAnnotations(
         EJBContainer container, InterceptorBindingMetaData binding)
         throws ClassNotFoundException
   {
      Interceptors interceptors = (Interceptors) container
            .resolveAnnotation(Interceptors.class);
      if (binding != null)
      {
         Interceptors impl = createInterceptorsFromInterceptorBinding(interceptors, binding);

         addClassAnnotation(container, impl.annotationType(), impl);
      }

      boolean exclude = false;
      if (binding != null)
         exclude = binding.isExcludeDefaultInterceptors();
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
         EJBContainer container, InterceptorBindingMetaData binding)
         throws ClassNotFoundException
   {
      /*
      boolean addedAnnotations = false;
      for (java.lang.reflect.Method method : container.getBeanClass()
            .getMethods())
      {
         boolean matches = false;
         if (method.getName().equals(binding.getMethod().getMethodName()))
         {
            if (binding.getMethod().getMethodParams() == null)
            {
               matches = true;
            } else
            {
               Class<?>[] methodParams = method.getParameterTypes();
               MethodParametersMetaData bindingParams = binding.getMethod().getMethodParams();

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
            if (binding != null)
            {
               Interceptors impl = createInterceptorsFromInterceptorBinding(interceptors, binding);
               log.debug("adding " + Interceptors.class.getName()
                     + " method annotation to " + method);
               container.getAnnotations().addAnnotation(method,
                     Interceptors.class, impl);
            }

            boolean excludeDefault = false;
            if (binding != null) 
               excludeDefault = binding.isExcludeDefaultInterceptors();
            if (excludeDefault
                  && container.resolveAnnotation(method,
                        ExcludeDefaultInterceptors.class) == null)
            {
               log.debug("adding " + ExcludeDefaultInterceptors.class.getName()
                     + " method annotation to " + method);
               container.getAnnotations().addAnnotation(method,
                     ExcludeDefaultInterceptors.class,
                     new ExcludeDefaultInterceptorsImpl());
            }

            boolean excludeClass = false;
            if (binding != null)
               excludeClass = binding.isExcludeClassInterceptors();
            if (excludeClass
                  && container.resolveAnnotation(method,
                        ExcludeClassInterceptors.class) == null)
            {
               log.debug("adding " + ExcludeClassInterceptors.class.getName()
                     + " method annotation to " + method);
               container.getAnnotations().addAnnotation(method,
                     ExcludeClassInterceptors.class,
                     new ExcludeClassInterceptorsImpl());
            }
            matches = false;
            addedAnnotations = true;
         }
      }

      return addedAnnotations;
      */
      return false;
   }

   private void addEjbAnnotations(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean) throws Exception
   {
      if (enterpriseBean != null)
      {
         if (enterpriseBean instanceof JBossSessionBeanMetaData)
         {
            addHomeAnnotations(container, (JBossSessionBeanMetaData) enterpriseBean);
   
            addJndiAnnotations(container, (JBossSessionBeanMetaData) enterpriseBean);
         }

         addInterceptorMethodAnnotations(container, enterpriseBean);

         handleResourceRefs(container, enterpriseBean.getResourceReferences());

         addMessageDestinationAnnotations(container, enterpriseBean.getMessageDestinationReferences());

         addSecurityIdentityAnnotation(container, enterpriseBean.getSecurityIdentity());

         addDependencies(container, enterpriseBean);

         addPoolAnnotations(container, enterpriseBean);
         
         addXmlAnnotations(container, enterpriseBean);

         if (enterpriseBean instanceof JBossSessionBeanMetaData)
         {
            addConcurrentAnnotations(container, (JBossSessionBeanMetaData)enterpriseBean);
            addClusterAnnotations(container, (JBossSessionBeanMetaData)enterpriseBean);
            addCacheAnnotations(container, (JBossSessionBeanMetaData)enterpriseBean);
         }
      }
   }

   private void addWebServiceAnnotations(EJBContainer container, JBossEnterpriseBeanMetaData enterpriseBean)
   {
      if (enterpriseBean != null && (enterpriseBean instanceof JBossSessionBeanMetaData))
      {
         PortComponent pc = ((JBossSessionBeanMetaData)enterpriseBean).getPortComponent();
         if (pc != null)
         {
            PortComponentMD annotation = new PortComponentMD();
            annotation.setAuthMethod(pc.getAuthMethod());
            annotation.setPortComponentName(pc.getPortComponentName());
            annotation.setPortComponentURI(pc.getPortComponentURI());
            annotation.setSecureWSDLAccess(pc.getSecureWSDLAccess());
            annotation.setTransportGuarantee(pc.getTransportGuarantee());
            
            addClassAnnotation(container, PortComponentSpec.class, annotation);
         }
      }
   }
   
   private void addConcurrentAnnotations(EJBContainer container,
         JBossSessionBeanMetaData enterpriseBean) throws Exception
   {
      if (enterpriseBean.isConcurrent() != null)
      {
         if (enterpriseBean.isConcurrent())
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
         JBossEnterpriseBeanMetaData enterpriseBean) throws Exception
   {
      if (enterpriseBean.getPoolConfig() != null)
      {
         PoolConfigMetaData config = enterpriseBean.getPoolConfig();

         PoolImpl poolAnnotation = new PoolImpl();

         if (config.getValue() != null && !config.getValue().trim().equals(""))
            poolAnnotation.setValue(config.getValue());
         
         // EJBTHREE-1119
         if(config.getValue()==null || config.getValue().trim().equals(""))
         {
            // Set default implementation to Threadlocal
            poolAnnotation.setValue(PoolDefaults.POOL_IMPLEMENTATION_THREADLOCAL);
         }

         if (config.getMaxSize() != null)
            poolAnnotation.setMaxSize(config.getMaxSize());

         if (config.getTimeout() != null)
            poolAnnotation.setTimeout(config.getTimeout());

         addClassAnnotation(container, Pool.class, poolAnnotation);
      }
   }
   
   private void addXmlAnnotations(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean) throws Exception
   {
      AnnotationsMetaData annotations = enterpriseBean.getAnnotations();
      if(annotations == null)
         return;
      
      for(AnnotationMetaData xmlAnnotation: annotations)
      {
         Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) di.getClassLoader().loadClass(xmlAnnotation.getAnnotationClass());
         Class<? extends Annotation> annotationImplementationClass = (Class<? extends Annotation>) di.getClassLoader().loadClass(xmlAnnotation.getAnnotationImplementationClass());
         Annotation annotation = annotationImplementationClass.newInstance();
         
         AnnotationPropertiesMetaData properties = xmlAnnotation.getProperties();
         if (properties != null)
         {
            for (AnnotationPropertyMetaData property : properties)
            {
               Field field = annotationImplementationClass.getDeclaredField(property.getName());
               setAnnotationPropertyField(field, annotation, property.getPropertyValue());
            }
         }
            
         if (xmlAnnotation.getInjectionTarget() == null)
         {
            addClassAnnotation(container, annotationClass, annotation);
         } 
         else
         {
            MethodMetaData method = new MethodMetaData();
            method.setMethodName(xmlAnnotation.getInjectionTarget().getInjectionTargetName());
            addAnnotations(annotationClass, annotation, container, method);
         }
      }
   }
   
   // FIXME: Wolf: Why do we have this method here? It's a bean property setter, so should
   // be part of bean spi.
   protected void setAnnotationPropertyField(Field field, Object annotation, String value) throws Exception
   {
      if (field.getType() == String.class)
         field.set(annotation, value);
      else if (field.getType() == Long.class || field.getType() == Long.TYPE)
         field.setLong(annotation, Long.parseLong(value));
      else if (field.getType() == Integer.class || field.getType() == Integer.TYPE)
         field.setInt(annotation, Integer.parseInt(value));
      else if (field.getType() == Class.class)
         field.set(annotation, di.getClassLoader().loadClass(value));
      else if (field.getType() == Boolean.class || field.getType() == Boolean.TYPE)
         field.setBoolean(annotation, Boolean.parseBoolean(value));
      else
         throw new IllegalArgumentException("unsupported field type " + field.getType() + " on field " + field);
   }

   private void addCacheAnnotations(EJBContainer container,
         JBossSessionBeanMetaData enterpriseBean) throws Exception
   {
      if (enterpriseBean.getCacheConfig() != null)
      {
         CacheConfigMetaData config = enterpriseBean.getCacheConfig();
         if (config.getValue() != null && !config.getValue().equals(""))
         {
            String cacheValue = config.getValue();
            CacheImpl cacheAnnotation = new CacheImpl(cacheValue);
            addClassAnnotation(container, Cache.class, cacheAnnotation);

            // FIXME: Wolf: what the hell is this?
            // FIXME: ALR: This shouldn't be hardcoded; configuration needs reworking
            if (cacheValue.equals("SimpleStatefulCache"))
            {
               if (!ejbClass.isAnnotationPresent(PersistenceManager.class))
               {
                  PersistenceManagerImpl persistenceAnnotation = new PersistenceManagerImpl();
                  if (config.getPersistenceManager() != null)
                  {
                     persistenceAnnotation.setValue(config.getPersistenceManager());
                  }
                  addClassAnnotation(container, PersistenceManager.class, persistenceAnnotation);
               }
            }
         }

         if (config.getName() != null)
         {
            org.jboss.ejb3.annotation.impl.CacheConfigImpl configAnnotation = new org.jboss.ejb3.annotation.impl.CacheConfigImpl();

            configAnnotation.setName(config.getName());

            if (config.getMaxSize() != null)
               configAnnotation.setMaxSize(config.getMaxSize());

            if (config.getIdleTimeoutSeconds() != null)
               configAnnotation.setIdleTimeoutSeconds(config.getIdleTimeoutSeconds());

            if (config.getReplicationIsPassivation() != null)
               configAnnotation.setReplicationIsPassivation(Boolean.parseBoolean(config.getReplicationIsPassivation()));

            if (config.getRemoveTimeoutSeconds() != null)
               configAnnotation.setRemovalTimeoutSeconds(config.getRemoveTimeoutSeconds());
            
            org.jboss.ejb3.annotation.CacheConfig existingConfig = ejbClass.getAnnotation(org.jboss.ejb3.annotation.CacheConfig.class);
            if (existingConfig != null)
               configAnnotation.merge(existingConfig);
            
            addClassAnnotation(container, org.jboss.ejb3.annotation.CacheConfig.class, configAnnotation);
         }
         else
         {
            org.jboss.ejb3.annotation.impl.CacheConfigImpl configAnnotation = new org.jboss.ejb3.annotation.impl.CacheConfigImpl();

            if (config.getMaxSize() != null)
               configAnnotation.setMaxSize(config.getMaxSize());

            if (config.getIdleTimeoutSeconds() != null)
               configAnnotation.setIdleTimeoutSeconds(config.getIdleTimeoutSeconds());
            
            if (config.getRemoveTimeoutSeconds() != null)
               configAnnotation.setRemovalTimeoutSeconds(config.getRemoveTimeoutSeconds());

            org.jboss.ejb3.annotation.CacheConfig existingConfig = ejbClass.getAnnotation(org.jboss.ejb3.annotation.CacheConfig.class);
            if (existingConfig != null)
               configAnnotation.merge(existingConfig);
            
            addClassAnnotation(container, org.jboss.ejb3.annotation.CacheConfig.class, configAnnotation);
         }
      }

   }

   private void addClusterAnnotations(EJBContainer container,
         JBossSessionBeanMetaData enterpriseBean) throws Exception
   {
      /* FIXME: Why disable the annotation?
      if (!enterpriseBean.isClustered())
      {
         // ask directly, not the container (metadata setup in progress)
         Clustered existingAnnotation = ejbClass.getAnnotation(Clustered.class);
         if (existingAnnotation != null)
            container.getAnnotations().disableAnnotation(Clustered.class.getName());
         return;
      }
      */

      ClusterConfigMetaData config = enterpriseBean.getClusterConfig();
      if (config != null)
      {
         ClusteredImpl clusteredAnnotation = new ClusteredImpl();
         
         if (config.getBeanLoadBalancePolicy() != null)
         {
            String policy = config.getBeanLoadBalancePolicy();
            clusteredAnnotation.setLoadBalancePolicy(policy);
         }
         
         if (config.getHomeLoadBalancePolicy() != null)
         {
            String policy = config.getHomeLoadBalancePolicy();
            clusteredAnnotation.setHomeLoadBalancePolicy(policy);
         }

         if (config.getPartitionName() != null)
         {
            clusteredAnnotation.setPartition(config.getPartitionName());
         }
         
         addClassAnnotation(container, Clustered.class, clusteredAnnotation);
      }
   }

   private void addDependencies(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean) throws Exception
   {
      Set<String> depends = enterpriseBean.getDepends();
      if (depends != null && depends.size() > 0)
      {
         DependsImpl annotation = new DependsImpl();
         Iterator<String> dependencies = enterpriseBean.getDepends()
               .iterator();
         while (dependencies.hasNext())
         {
            annotation.addDependency(dependencies.next());
         }

         addClassAnnotation(container, Depends.class, annotation);
      }

      if (enterpriseBean.getIgnoreDependency() != null)
      {
         for(ResourceInjectionTargetMetaData ignore : enterpriseBean.getIgnoreDependency().getInjectionTargets())
         {
            IgnoreDependencyImpl annotation = new IgnoreDependencyImpl();

            MethodMetaData method = new MethodMetaData();
            method.setMethodName(ignore.getInjectionTargetName());

            addAnnotations(IgnoreDependency.class, annotation, container, method);
         }
      }
   }

   private void addServiceAnnotations(EJBContainer container, JBossServiceBeanMetaData service)
         throws ClassNotFoundException
   {
      if (service == null)
         return;

      String management = service.getManagement();

      if (management != null)
      {
         ManagementImpl annotation = new ManagementImpl(di.getClassLoader().loadClass(management));
         addClassAnnotation(container, Management.class, annotation);
      }
   }

   private void addConsumerAnnotations(EJBContainer container,
         JBossConsumerBeanMetaData consumer) throws ClassNotFoundException,
         NoSuchFieldException, NoSuchMethodException
   {
      if (consumer == null)
         return;

      List<ProducerMetaData> producers = consumer.getProducers();
      List<LocalProducerMetaData> localProducers = consumer.getLocalProducers();
      if ((producers != null && producers.size() > 0) || (localProducers != null && localProducers.size() > 0))
      {
         ProducersImpl producersAnnotation = new ProducersImpl();

         if(producers != null)
         {
            for(ProducerMetaData producer : producers)
            {
               ProducerImpl annotation = new ProducerImpl(di.getClassLoader()
                     .loadClass(producer.getClassName()));
               if (producer.getConnectionFactory() != null)
                  annotation.setConnectionFactory(producer.getConnectionFactory());
               producersAnnotation.addProducer(annotation);
            }
         }

         if(localProducers != null)
         {
            for(ProducerMetaData producer : localProducers)
            {
               ProducerImpl annotation = new ProducerImpl(di.getClassLoader()
                     .loadClass(producer.getClassName()));
               if (producer.getConnectionFactory() != null)
                  annotation.setConnectionFactory(producer.getConnectionFactory());
               producersAnnotation.addProducer(annotation);
            }
         }
         
         addClassAnnotation(container, Producers.class, producersAnnotation);
      }

      MethodAttributesMetaData currentMessage = consumer
            .getCurrentMessage();
      if (currentMessage != null)
      {
         CurrentMessageImpl annotation = new CurrentMessageImpl();
         for(MethodAttributeMetaData method : currentMessage)
         {
            addAnnotations(CurrentMessage.class, annotation, container, method);
         }
      }

      List<MessagePropertiesMetaData> propertiesList = consumer.getMessageProperties();
      if (propertiesList != null)
      {
         for(MessagePropertiesMetaData properties : propertiesList)
         {
            MessagePropertiesImpl annotation = new MessagePropertiesImpl();
   
            String delivery = properties.getDelivery();
            if (delivery != null && delivery.equals("Persistent"))
               annotation.setDelivery(DeliveryMode.PERSISTENT);
            else
               annotation.setDelivery(DeliveryMode.NON_PERSISTENT);
   
            Integer priority = properties.getPriority();
            if (priority != null)
               annotation.setDelivery(DeliveryMode.PERSISTENT);
   
            String interfac = properties.getClassName();
            if (interfac != null)
            {
               Class<?> clazz = di.getClassLoader().loadClass(interfac);
               annotation.setInterface(clazz);
            }
   
            MethodAttributeMetaData method = properties.getMethod();
            addAnnotations(MessageProperties.class, annotation, container, method);
         }
      }
   }

   private void addJndiAnnotations(EJBContainer container,
         JBossSessionBeanMetaData enterpriseBean) throws ClassNotFoundException
   {
      addLocalJndiAnnotations(container, enterpriseBean);
      addRemoteJndiAnnotations(container, enterpriseBean);
   }

   private void addLocalJndiAnnotations(EJBContainer container,
         JBossSessionBeanMetaData enterpriseBean) throws ClassNotFoundException
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
         JBossSessionBeanMetaData enterpriseBean) throws ClassNotFoundException
   {
	  String homeJndiName = enterpriseBean.getHomeJndiName();
	  if (homeJndiName != null && !homeJndiName.trim().equals(""))
      {
         RemoteHomeBindingImpl homeBinding = new RemoteHomeBindingImpl(homeJndiName);
         addClassAnnotation(container, RemoteHomeBinding.class, homeBinding);
      } 
	  
	  // JBCTS-718
      // If jndi-name is defined, use the value specified
      String jndiName = enterpriseBean.getJndiName();
      if (jndiName != null && !jndiName.trim().equals(""))
      {
         RemoteBindingImpl remoteBindingAnnotation = new RemoteBindingImpl();
         remoteBindingAnnotation.setJndiBinding(jndiName);
         addClassAnnotation(container, RemoteBinding.class, remoteBindingAnnotation);
         log.debug("Adding " + RemoteBinding.class.getName() + " to " + container.toString() + ": "
               + remoteBindingAnnotation.toString());
         return;
      }
	  
      List<RemoteBindingMetaData> bindingsList = enterpriseBean.getRemoteBindings();
      if (bindingsList == null || bindingsList.size() == 0)
      {
         addSimpleJndiAnnotations(container, enterpriseBean);
         return;
      }

      AnnotationRepository annotations = container.getAnnotations();

      annotations.disableAnnotation(RemoteBinding.class.getName());

      List<RemoteBindingImpl> bindingAnnotationsList = new ArrayList<RemoteBindingImpl>();

      for(RemoteBindingMetaData binding : bindingsList)
      {
         RemoteBindingImpl bindingAnnotation = new RemoteBindingImpl();

         if (binding.getJndiName() != null)
            bindingAnnotation.setJndiBinding(binding.getJndiName());

         if (binding.getClientBindUrl() != null)
            bindingAnnotation.setBindUrl(binding.getClientBindUrl());

         if (binding.getInterceptorStack() != null)
            bindingAnnotation.setStack(binding.getInterceptorStack());

         if (binding.getProxyFactory() != null)
            bindingAnnotation.setFactory(binding.getProxyFactory());

         bindingAnnotationsList.add(bindingAnnotation);

      }

      RemoteBindingsImpl bindingsAnnotation = new RemoteBindingsImpl(bindingAnnotationsList);
      addClassAnnotation(container, RemoteBindings.class, bindingsAnnotation);
   }

   private void addSimpleJndiAnnotations(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean) throws ClassNotFoundException
   {
      RemoteBindingImpl remoteBinding = null;

      String jndiName = enterpriseBean.getMappedName();
      if (jndiName != null)
      {
         remoteBinding = new RemoteBindingImpl();
         remoteBinding.setJndiBinding(jndiName);
         addClassAnnotation(container, RemoteBinding.class, remoteBinding);
      }

      if (remoteBinding != null)
      {
         RemoteBinding existingBinding = ejbClass.getAnnotation(RemoteBinding.class);
         if (existingBinding != null)
            remoteBinding.merge(existingBinding);

         addClassAnnotation(container, RemoteBinding.class, remoteBinding);
      }
   }

   private void handleResourceRefs(EJBContainer container, ResourceReferencesMetaData resourceRefList)
   {
      if(resourceRefList == null)
         return;
      
      for(ResourceReferenceMetaData ref : resourceRefList)
      {
         if (ref.getResourceName() != null)
         {
            // for <resource-manager>
            ResourceManagerMetaData resourceManager = dd.getResourceManager(ref.getResourceName());
            if(resourceManager != null)
            {
               ref.setJndiName(resourceManager.getResJndiName());
               ref.setMappedName(resourceManager.getResJndiName());
            }
         }
      }
   }

   private void addMessageDestinationAnnotations(EJBContainer container, MessageDestinationReferencesMetaData refs)
   {
      if(refs == null)
         return;
      
      for(MessageDestinationReferenceMetaData ref : refs)
      {
         if (ref.getMappedName() == null || ref.getMappedName().equals(""))
         {
            JBossAssemblyDescriptorMetaData descriptor = dd.getAssemblyDescriptor();
            if (descriptor != null)
            {
               MessageDestinationMetaData destination = descriptor.getMessageDestination(ref.getLink());
               if (destination != null)
               {
                  ref.setMappedName(destination.getJndiName());
               }
            }
         }
      }
   }

   private void addInterceptorMethodAnnotations(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean)
   {
      if (enterpriseBean instanceof JBossSessionBeanMetaData)
      {
         JBossSessionBeanMetaData sessionBean = (JBossSessionBeanMetaData) enterpriseBean;
         addInterceptorMethodAnnotation(container, enterpriseBean,
               sessionBean.getAroundInvokes(),
               AroundInvoke.class, "around-invoke-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               sessionBean.getPostConstructs(),
               PostConstruct.class, "post-construct-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               sessionBean.getPostActivates(),
               PostActivate.class, "post-activate-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               sessionBean.getPrePassivates(),
               PrePassivate.class, "pre-passivate-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               sessionBean.getPreDestroys(),
               PreDestroy.class, "pre-destroy-method");
      } 
      else if (enterpriseBean instanceof JBossMessageDrivenBeanMetaData)
      {
         JBossMessageDrivenBeanMetaData messageDriven = (JBossMessageDrivenBeanMetaData) enterpriseBean;
         addInterceptorMethodAnnotation(container, enterpriseBean,
               messageDriven.getAroundInvokes(),
               AroundInvoke.class, "around-invoke-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               messageDriven.getPostConstructs(),
               PostConstruct.class, "post-construct-method");
         addInterceptorMethodAnnotation(container, enterpriseBean,
               messageDriven.getPreDestroys(),
               PreDestroy.class, "pre-destroy-method");
      }
   }

   private void addInterceptorMethodAnnotation(EJBContainer container, Class<?> cls, String methodName, Class<? extends Annotation> ann, String xmlName)
   {
      Method found = null;
      for (Method rm : cls.getDeclaredMethods())
      {
         if (rm.getName().equals(methodName))
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
         log.warn("No method found within " + cls.getName()
               + " with name " + methodName
               + " with the right signature for " + xmlName + "was found");
         return;
      }

      if (container.resolveAnnotation(found, ann) == null)
      {
         log.debug("adding " + ann.getName() + " method annotation to "
               + found);

         container.getAnnotations().addAnnotation(found, ann,
               getInterceptorImpl(ann));
      }      
   }
   
   private void addInterceptorMethodAnnotation(EJBContainer container, JBossEnterpriseBeanMetaData enterpriseBean, AroundInvokesMetaData callbacks, Class<? extends Annotation> ann, String xmlName)
   {
      if (callbacks == null)
         return;

      for(AroundInvokeMetaData callback : callbacks)
      {
         Class<?> callbackClass = loadClass(container, callback.getClassName());
         
         addInterceptorMethodAnnotation(container, callbackClass, callback.getMethodName(), ann, xmlName);
      }
   }
   
   private void addInterceptorMethodAnnotation(EJBContainer container,
         JBossEnterpriseBeanMetaData enterpriseBean, LifecycleCallbacksMetaData callbacks, Class<? extends Annotation> ann, String xmlName)
   {
      if (callbacks == null)
         return;

      for(LifecycleCallbackMetaData callback : callbacks)
      {
         Class<?> callbackClass = loadClass(container, callback.getClassName());
         
         addInterceptorMethodAnnotation(container, callbackClass, callback.getMethodName(), ann, xmlName);
      }
   }

   private Object getInterceptorImpl(Class<?> ann)
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
         SecurityIdentityMetaData identity)
   {
      if (identity != null && !identity.isUseCallerId())
      {
         RunAsMetaData runAs = identity.getRunAs();
         RunAsImpl annotation = null;
         if (runAs != null)
         {
            annotation = new RunAsImpl(runAs.getRoleName());
            addClassAnnotation(container, annotation.annotationType(),
                  annotation);
         }
         
         String runAsPrincipal = identity.getRunAsPrincipal();
         if (runAsPrincipal != null)
         {
            RunAsPrincipalImpl principalAnnotation = new RunAsPrincipalImpl(runAsPrincipal);
            addClassAnnotation(container, principalAnnotation
                  .annotationType(), principalAnnotation);
         }
      }
   }

   /*
    * This method in non-deterministic. It should expect to be called
    * in random order, so at the end there is no guarenteed security
    * annotation configuration.
    */
   @Deprecated
   protected void overrideAnnotations(EJBContainer container, Member m,
         String annotation, Object value)
   {
      /*
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
      */
   }

   private void addClassAnnotation(EJBContainer container, Annotation annotation)
   {
      addClassAnnotation(container, annotation.annotationType(), annotation);
   }
   
   private void addClassAnnotation(EJBContainer container, Class<? extends Annotation> annotationClass, Annotation annotation)
   {
      log.debug("adding class annotation " + annotationClass.getName() + " to "
            + container + " " + annotation);
      container.getAnnotations()
            .addClassAnnotation(annotationClass, annotation);
   }

   private <A extends Annotation> void addAnnotations(Class<A> annotationClass, A annotation, EJBContainer container, MethodAttributeMetaData method)
   {
      addAnnotations(annotationClass, annotation, container, method.getMethodName(), null);
   }
   
   private void addAnnotations(Class<? extends Annotation> annotationClass, Annotation annotation, EJBContainer container, String methodName, MethodParametersMetaData params)
   {
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
            if (params == null)
            {
               java.lang.reflect.Method[] methods = ejbClass.getMethods();
               boolean foundMethod = false;
               for (int methodIndex = 0; methodIndex < methods.length; ++methodIndex)
               {
                  if (methods[methodIndex].getName().equals(methodName))
                  {
                     log.debug("adding " + annotationClass.getName()
                           + " method annotation to " + methods[methodIndex]);
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
                              + " method annotation to " + methods[methodIndex]);
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
                           + " field annotation to " + member);
                     annotations
                           .addAnnotation(member, annotationClass, annotation);
                     overrideAnnotations(container, member, annotationClass
                           .getName(), annotation);
                  }
               }
            } else
            {
               Class<?>[] methodSignature = new Class[params.size()];
               int paramIndex = 0;
               for(String param : params)
               {
                  Class<?> paramClass = PrimitiveClassLoadingUtil.loadClass(param, di.getClassLoader());
                  methodSignature[paramIndex++] = paramClass;
               }
               if(log.isTraceEnabled())
                  log.trace("Looking for method " + methodName + Arrays.toString(methodSignature) + " on class " + ejbClass);
               Member member = ClassHelper.getPrivateMethod(ejbClass, methodName, methodSignature);
               log.debug("adding " + annotationClass.getName()
                     + " method annotation to " + member);
               annotations.addAnnotation(member, annotationClass, annotation);
               overrideAnnotations(container, member, annotationClass.getName(),
                     annotation);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create annotation for method/field " + methodName + " for EJB " + container.getEjbName(), e);
      }
   }
   
   private void addAnnotations(Class<? extends Annotation> annotationClass, Annotation annotation, EJBContainer container, NamedMethodMetaData method)
   {
      addAnnotations(annotationClass, annotation, container, method.getMethodName(), method.getMethodParams());
   }
   
   private void addAnnotations(Class<? extends Annotation> annotationClass, Annotation annotation,
         EJBContainer container, MethodMetaData method) throws ClassNotFoundException,
         NoSuchMethodException, NoSuchFieldException
   {
      addAnnotations(annotationClass, annotation, container, method.getMethodName(), method.getMethodParams());
   }

   /**
    * Verify whether the class has a public method with a certain name.
    * 
    * @param cls            the class to check
    * @param methodName     the method to find
    * @return               true if a method with that name exists on that class
    */
   private boolean hasPublicMethod(Class<?> cls, String methodName)
   {
      assert cls != null : "cls is null";
      assert methodName != null : "methodName is null";
      
      for(java.lang.reflect.Method m : cls.getMethods())
      {
         if(m.getName().equals(methodName))
            return true;
      }
      
      return false;
   }

}
