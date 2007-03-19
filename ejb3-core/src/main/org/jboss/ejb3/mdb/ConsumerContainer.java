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
package org.jboss.ejb3.mdb;

import org.jboss.annotation.ejb.Consumer;
import org.jboss.annotation.ejb.DefaultActivationSpecs;
import org.jboss.annotation.ejb.Local;
import org.jboss.annotation.ejb.MessageProperties;
import org.jboss.annotation.ejb.MessagePropertiesImpl;
import org.jboss.annotation.ejb.Producer;
import org.jboss.annotation.ejb.Producers;
import org.jboss.aop.AspectManager;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationResponse;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.PayloadKey;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ConsumerContainer extends MessagingContainer
{
   private static final Logger log = Logger.getLogger(ConsumerContainer.class);
   
   protected Class messagingType = null;
   protected Method ON_MESSAGE;
   
   protected ArrayList<ProducerFactory> producers = new ArrayList<ProducerFactory>();
   
   /**
    * Default destination type. Used when no message-driven-destination is given
    * in ejb-jar, and a lookup of destinationJNDI from jboss.xml is not
    * successfull. Default value: javax.jms.Topic.
    */
   protected final static String DEFAULT_DESTINATION_TYPE = "javax.jms.Topic";


   /**
    * This is needed because API changed from JBoss 4.0.1sp1 to 4.0.2
    * TODO remove this after 4.0.2 is out.
    */
   public static final String CONSUMER_MESSAGE = "CONSUMER_MESSAGE";


   public ConsumerContainer(String ejbName, AspectManager manager, ClassLoader cl, String beanClassName,
                            Hashtable ctxProperties, InterceptorInfoRepository interceptorRepository,
                            Ejb3Deployment deployment)
   {
      super(ejbName, manager, cl, beanClassName, ctxProperties, interceptorRepository, deployment);
   }
   
   public InvocationResponse dynamicInvoke(Invocation invocation) throws Throwable
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      EJBContainerInvocation newSi = null;
      pushEnc();
      try
      {
         Thread.currentThread().setContextClassLoader(classloader);
         MethodInvocation si = (MethodInvocation) invocation;
         MethodInfo info = (MethodInfo) methodInterceptors.get(si.getMethodHash());
         if (info == null)
         {
            throw new RuntimeException("Could not resolve beanClass method from proxy call");
         }
       
         newSi = new EJBContainerInvocation(info);
         newSi.setArguments(si.getArguments());
         newSi.setMetaData(si.getMetaData());
         newSi.setAdvisor(this);

         InvocationResponse response = new InvocationResponse(newSi.invokeNext());
         response.setContextInfo(newSi.getResponseContextInfo());
         return response;
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }
   
   protected Method getOnMessage()
   {
      if (ON_MESSAGE != null)
         return ON_MESSAGE;
      
      try
      {
         final Class arg = Message.class;
         ON_MESSAGE = javax.jms.MessageListener.class.getMethod("onMessage", new Class[]{arg});
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }

      return ON_MESSAGE;
   }
   
   public Object localInvoke(MethodInfo info, Object[] args) throws Throwable
   {     
      if (info.getAdvisedMethod().equals(getOnMessage()))
      {
         ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
         pushEnc();
         
         try
         {
            Message message = (Message)args[0];
            MethodInvocation invocation = (MethodInvocation) ((ObjectMessage) message).getObject();
            invocation.getMetaData().addMetaData(CONSUMER_MESSAGE, CONSUMER_MESSAGE, message, PayloadKey.TRANSIENT);
            return this.dynamicInvoke(invocation);
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(oldLoader);
            popEnc();
         }
      }
      else
         return super.localInvoke(info, args);
   }
   
   public Class getMessagingType()
   {
      return javax.jms.MessageListener.class;
   }
   
   public MethodInfo getMethodInfo(Method method)
   {
      MethodInfo info = new MethodInfo();
      info.setAdvisor(this);
      info.setAdvisedMethod(method);
      info.setUnadvisedMethod(method);
    
      return info;
   }
 
   public Map getActivationConfigProperties()
   {
      HashMap result = new HashMap();
      Consumer annotation = (Consumer) resolveAnnotation(Consumer.class);
      for (ActivationConfigProperty property : annotation.activationConfig())
      {
         addActivationSpecProperty(result, property);
      }
      
      DefaultActivationSpecs defaultSpecsAnnotation = (DefaultActivationSpecs)resolveAnnotation(DefaultActivationSpecs.class);
      if (defaultSpecsAnnotation != null)
      {
         for (ActivationConfigProperty property : defaultSpecsAnnotation.value())
         {
            addActivationSpecProperty(result, property);
         }
      }
      
      return result;
   }


   /**
    * Initialize the container invoker. Sets up a connection, a server session
    * pool and a connection consumer for the configured destination.
    * <p/>
    * Any JMSExceptions produced while initializing will be assumed to be
    * caused due to JMS Provider failure.
    *
    * @throws Exception Failed to initalize.
    */
   public void start() throws Exception
   {
      super.start();
      
      registerProducers();
   }

   public Class[] getProducerInterfaces(Container container1)
   {
      Class beanClass = container1.getBeanClass();
      Class[] interfaces = beanClass.getInterfaces();
      if (interfaces.length == 0) throw new RuntimeException("Bean class must implement at least one interface: " + beanClass.getName());
      if (interfaces.length == 1)
      {
         return interfaces;
      }
      ArrayList localInterfaces = new ArrayList();
      for (int i = 0; i < interfaces.length; i++)
      {
         if (interfaces[i].isAnnotationPresent(Producer.class))
         {
            localInterfaces.add(interfaces[i]);
         }
      }
      Producer annotation = (Producer)resolveAnnotation(Producer.class);
      if (annotation != null)
      {
         Class producer = annotation.producer();
         if (producer != null)
            localInterfaces.add(producer);
      }
      
      Producers producersAnnotation = (Producers)resolveAnnotation(Producers.class);
      if (producersAnnotation != null)
      {
         for (Producer producerAnnotation : producersAnnotation.value())
         {
            Class producer = producerAnnotation.producer();
            if (producer != null)
               localInterfaces.add(producer);
         }
      }
      
      if (localInterfaces.size() == 0) return null;
      interfaces = (Class[]) localInterfaces.toArray(new Class[localInterfaces.size()]);
      return interfaces;
   }

   protected void registerProducers() throws Exception
   {
      Destination dest = (Destination) getInitialContext().lookup(getDestination());
      Class[] producers = getProducerInterfaces(this);
      MessageProperties props = (MessageProperties) resolveAnnotation(MessageProperties.class);
      if (props == null) props = new MessagePropertiesImpl();
      for (Class producer : producers)
      {
         log.debug("Producer: " + producer.getName());
         ProducerFactory producerFactory = null;
         if (producer.isAnnotationPresent(Local.class))
         {
            producerFactory = new LocalProducerFactory(this, producer, props, dest, getInitialContext(), initialContextProperties);
         }
         else
         {
            producerFactory = new RemoteProducerFactory(this, producer, props, dest, getInitialContext(), initialContextProperties);
         }
         this.producers.add(producerFactory);
         producerFactory.start();
      }
   }

   protected void unregisterProducers() throws Exception
   {
      for (ProducerFactory factory : producers)
      {
         factory.stop();
      }
   }
   
   protected void populateActivationSpec()
   {
      DefaultActivationSpecs defaultSpecs = (DefaultActivationSpecs) resolveAnnotation(DefaultActivationSpecs.class);
      if (defaultSpecs != null)
      {
         activationSpec.merge(defaultSpecs.value());
      }

      Consumer md = (Consumer) resolveAnnotation(Consumer.class);
      activationSpec.merge(md.activationConfig());
   }

   public void stop() throws Exception
   {
      unregisterProducers();
      super.stop();
   }
}
