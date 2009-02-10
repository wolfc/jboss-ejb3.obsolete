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

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.aop.Domain;
import org.jboss.aop.MethodInfo;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.EJBContainerInvocation;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3Module;
import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.ejb3.jms.JMSDestinationFactory;
import org.jboss.ejb3.mdb.inflow.JBossMessageEndpointFactory;
import org.jboss.ejb3.proxy.factory.ProxyFactoryHelper;
import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory;
import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigPropertyMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public abstract class MessagingContainer extends EJBContainer implements TimedObjectInvoker
{
   private static final Logger log = Logger.getLogger(MessagingContainer.class);
   
   protected TimerService timerService;
   private Method timeout;
   protected ActivationSpec activationSpec = new ActivationSpec();
   protected JBossMessageEndpointFactory messageEndpointFactory;
   private MessagingDelegateWrapper mbean = new MessagingDelegateWrapper(this);

   private TimerServiceFactory timerServiceFactory;

   /**
    * Default destination type. Used when no message-driven-destination is given
    * in ejb-jar, and a lookup of destinationJNDI from jboss.xml is not
    * successfull. Default value: javax.jms.Topic.
    */
   protected final static String DEFAULT_DESTINATION_TYPE = "javax.jms.Topic";

   public MessagingContainer(String ejbName, Domain domain, ClassLoader cl, String beanClassName, Hashtable ctxProperties,
              Ejb3Deployment deployment, JBossEnterpriseBeanMetaData beanMetaData) throws ClassNotFoundException
   {
      super(Ejb3Module.BASE_EJB3_JMX_NAME + ",name=" + ejbName, domain, cl, beanClassName, ejbName, ctxProperties, deployment, beanMetaData);
      
      messageEndpointFactory = new JBossMessageEndpointFactory();
      messageEndpointFactory.setContainer(this);
      
      initializeTimeout();
   }
   
   @Override
   public BeanContext<?> createBeanContext()
   {
      return new MDBContext(this, construct());
   }
   
   public Object getMBean()
   {
      return mbean;
   }
   
   protected JBossEnterpriseBeanMetaData getMetaData()
   {
      // TODO: resolve this cast using generics on EJBContainer
      return (JBossEnterpriseBeanMetaData) getXml();
   }
   
   abstract protected NamedMethodMetaData getTimeoutMethodMetaData();
   
   public abstract Class getMessagingType();
   
   public abstract Map<String, ActivationConfigPropertyMetaData> getActivationConfigProperties();
   
   protected abstract void populateActivationSpec();
   
   @Deprecated
   public MethodInfo getMethodInfo(Method method)
   {
      return super.getMethodInfo(method);
   }

   private void initializeTimeout()
   {
      this.timeout = getTimeoutCallback(getTimeoutMethodMetaData(), getBeanClass());
   }
   
   public void setMessageEndpointFactory(JBossMessageEndpointFactory messageEndpointFactory)
   {
      this.messageEndpointFactory = messageEndpointFactory;
   }
   
   public String getResourceAdaptorName()
   {
      ResourceAdapter annotation = (ResourceAdapter) resolveAnnotation(ResourceAdapter.class);
      if (annotation == null)
         return JMS_ADAPTOR;
      
      return annotation.value();
   }
   
   protected void addActivationSpecProperty(Map<String, ActivationConfigPropertyMetaData> result, ActivationConfigProperty property)
   {
      if (!property.propertyName().equals("messagingType"))
      {
         ActivationConfigPropertyMetaData metaData = new ActivationConfigPropertyMetaData();
         metaData.setName(property.propertyName());
         metaData.setValue(property.propertyValue());   
         result.put(property.propertyName(), metaData);
      }
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
   @Override
   protected void lockedStart() throws Exception
   {
      super.lockedStart();

      populateActivationSpec();
         
      innerStart();

      timerService = timerServiceFactory.createTimerService(this);

      startProxies();
      
      timerServiceFactory.restoreTimerService(timerService);
   }

   protected void startDelivery()
   {
      try
      {
         messageEndpointFactory.activate();
      }
      catch(DeploymentException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected void innerStart() throws Exception
   {
      log.debug("Initializing");
   }

   protected boolean isDeliveryActive()
   {
      return messageEndpointFactory.isDeliveryActive();
   }
   
   public ObjectName getJmxName()
   {
      ObjectName jmxName = null;
      String jndiName = ProxyFactoryHelper.getLocalJndiName(this);
      // The name must be escaped since the jndiName may be arbitrary
      String name = org.jboss.ejb.Container.BASE_EJB_CONTAINER_NAME + ",jndiName=" + jndiName;
      try
      {
         jmxName = org.jboss.mx.util.ObjectNameConverter.convert(name);
      }
      catch (MalformedObjectNameException e)
      {
         e.printStackTrace();
         throw new RuntimeException("Failed to create ObjectName, msg=" + e.getMessage());
      }

      return jmxName;
   }

   protected void startProxies() throws Exception
   {
      messageEndpointFactory.start();
   }

   /**
    * Parse the JNDI suffix from the given JNDI name.
    *
    * @param jndiname     The JNDI name used to lookup the destination.
    * @param defautSuffix Description of Parameter
    * @return The parsed suffix or the defaultSuffix
    */
   protected String parseJndiSuffix(final String jndiname,
                                    final String defautSuffix)
   {
      // jndiSuffix is merely the name that the user has given the MDB.
      // since the jndi name contains the message type I have to split
      // at the "/" if there is no slash then I use the entire jndi name...
      String jndiSuffix = "";

      if (jndiname != null)
      {
         int indexOfSlash = jndiname.indexOf("/");
         if (indexOfSlash != -1)
         {
            jndiSuffix = jndiname.substring(indexOfSlash + 1);
         }
         else
         {
            jndiSuffix = jndiname;
         }
      }
      else
      {
         // if the jndi name from jboss.xml is null then lets use the ejbName
         jndiSuffix = defautSuffix;
      }

      return jndiSuffix;
   }

   public Object localInvoke(Method method, Object[] args) throws Throwable
   {
      MethodInfo info = getMethodInfo(method);
      if (info == null)
      {
         throw new RuntimeException("Could not resolve beanClass method from proxy call: " + method.toString());
      }
      return localInvoke(info, args);
   }

   public Object localInvoke(MethodInfo info, Object[] args) throws Throwable
   {
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      pushEnc();
      try
      {
         EJBContainerInvocation nextInvocation = new EJBContainerInvocation(info);
         nextInvocation.setAdvisor(getAdvisor());
         nextInvocation.setArguments(args);
         return nextInvocation.invokeNext();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldLoader);
         popEnc();
      }
   }

   public TimerService getTimerService()
   {
      return timerService;
   }

   public TimerService getTimerService(Object pKey)
   {
      assert timerService != null : "Timer Service not yet initialized";
      return timerService;
   }
   
   public void callTimeout(Timer timer) throws Exception
   {
      if (timeout == null) throw new EJBException("No method has been annotated with @Timeout");
      Object[] args = {timer};
      try
      {
         localInvoke(timeout, args);
      }
      catch (Throwable throwable)
      {
         if (throwable instanceof Exception) throw (Exception) throwable;
         throw new RuntimeException(throwable);
      }
   }

   @Override
   protected void lockedStop() throws Exception
   {
      if (timerService != null)
      {
         timerServiceFactory.suspendTimerService(timerService);
         timerService = null;
      }

      stopProxies();
      
      super.lockedStop();
   }

   protected void stopDelivery()
   {
      messageEndpointFactory.deactivate();
   }
   
   protected void stopProxies() throws Exception
   {
      messageEndpointFactory.stop();
   }

   // ********* JMS Specific
   protected static final String JMS_ADAPTOR = "jms-ra.rar";
   protected static final String DESTINATION = "destination";
   protected static final String DESTINATION_TYPE = "destinationType";
   protected static final String PROVIDER_ADAPTER_JNDI = "providerAdapterJNDI";
   protected static final String MAX_SESSION = "maxSession";
   
   protected void initializePool() throws Exception
   {
     super.initializePool();
   }
   
   protected String getProviderAdapterJNDI()
   {
      ActivationConfigPropertyMetaData property = (ActivationConfigPropertyMetaData)getActivationConfigProperties().get(PROVIDER_ADAPTER_JNDI);
      if (property != null)
         return property.getValue();
      return "java:/DefaultJMSProvider";
   }
   
   protected String getMaxSession()
   {
      ActivationConfigPropertyMetaData property = (ActivationConfigPropertyMetaData)getActivationConfigProperties().get(MAX_SESSION);
      if (property != null)
         return property.getValue();
      return null;
   }
   
   protected String getDestination()
   {
      ActivationConfigPropertyMetaData property = (ActivationConfigPropertyMetaData)getActivationConfigProperties().get(DESTINATION);
      if (property != null)
         return property.getValue();
      return null;
   }
   
   protected String getDestinationType()
   {
      ActivationConfigPropertyMetaData property = (ActivationConfigPropertyMetaData)getActivationConfigProperties().get(DESTINATION_TYPE);
      if (property != null)
         return property.getValue();
      return null;
   }
   
   protected void innerCreateQueue(Context context)
           throws Exception
   {
      log.debug("Got destination type Queue for " + ejbName);

      // Get the JNDI suffix of the destination
      String jndiSuffix = parseJndiSuffix(getDestination(), ejbName);
      log.debug("jndiSuffix: " + jndiSuffix);

      // lookup or create the destination queue
      Queue queue = null;
      try
      {
         // First we try the specified queue
         if (getDestination() != null)
            queue = (Queue) context.lookup(getDestination());
      }
      catch (NamingException e)
      {
         log.warn("Could not find the queue destination-jndi-name=" + getDestination());
      }
      catch (ClassCastException e)
      {
         throw new DeploymentException("Expected a Queue destination-jndi-name=" + getDestination());
      }

      if (queue == null)
         queue = (Queue) createDestination(Queue.class,
                 context,
                 "queue/" + jndiSuffix,
                 jndiSuffix);
   }

   protected void innerCreateTopic(Context context)
           throws Exception
   {
      log.debug("Got destination type Topic for " + ejbName);

      // Get the JNDI suffix of the destination
      String jndiSuffix = parseJndiSuffix(getDestination(), ejbName);
      log.debug("jndiSuffix: " + jndiSuffix);

      // lookup or create the destination topic
      Topic topic = null;
      try
      {
         // First we try the specified topic
         if (getDestination() != null)
            topic = (Topic) context.lookup(getDestination());
      }
      catch (NamingException e)
      {
         log.warn("Could not find the topic destination-jndi-name=" + getDestination());
      }
      catch (ClassCastException e)
      {
         throw new DeploymentException("Expected a Topic destination-jndi-name=" + getDestination());
      }

      if (topic == null)
         topic = (Topic) createDestination(Topic.class,
                 context,
                 "topic/" + jndiSuffix,
                 jndiSuffix);
   }

   /**
    * Create and or lookup a JMS destination.
    *
    * @param type       Either javax.jms.Queue or javax.jms.Topic.
    * @param ctx        The naming context to lookup destinations from.
    * @param jndiName   The name to use when looking up destinations.
    * @param jndiSuffix The name to use when creating destinations.
    * @return The destination.
    * @throws IllegalArgumentException Type is not Queue or Topic.
    * @throws Exception                Description of Exception
    */
   private Destination createDestination(final Class<? extends Destination> type,
                                           final Context ctx,
                                           final String jndiName,
                                           final String jndiSuffix)
           throws Exception
   {
      try
      {
         // first try to look it up
         return (Destination) ctx.lookup(jndiName);
      }
      catch (NamingException e)
      {
         // is JMS?
         if (getDestination() == null)
         {
            return null;
         }
         else
         {
            // if the lookup failes, the try to create it
            log.warn("destination not found: " + jndiName + " reason: " + e);
            log.warn("creating a new temporary destination: " + jndiName);
            
            createTemporaryDestination(type, jndiSuffix);
       
            // try to look it up again
            return (Destination) ctx.lookup(jndiName);
         }
      }
   }
   
   private void createTemporaryDestination(Class<? extends Destination> type, String jndiSuffix) throws Exception
   {
      //
      // jason: we should do away with this...
      //
      // attempt to create the destination (note, this is very
      // very, very unportable).
      //

      // MBeanServer server = org.jboss.mx.util.MBeanServerLocator.locateJBoss();
      
//      String methodName;
//      String destinationContext;
//      if (type == Topic.class)
//      {
//         destinationContext = "topic";
//         methodName = "createTopic";
//      }
//      else if (type == Queue.class)
//      {
//         destinationContext = "queue";
//         methodName = "createQueue";
//      }
//      else
//      {
//         // type was not a Topic or Queue, bad user
//         throw new IllegalArgumentException
//                 ("Expected javax.jms.Queue or javax.jms.Topic: " + type);
//      }
      
      /* No longer supported (AS 4.x)
      ObjectName destinationManagerName = new ObjectName("jboss.mq:service=DestinationManager");
      
      KernelAbstraction kernel = KernelAbstractionFactory.getInstance();
      // invoke the server to create the destination
      Object result = kernel.invoke(destinationManagerName,
              methodName,
              new Object[]{jndiSuffix},
              new String[]{"java.lang.String"});
      
      InitialContext jndiContext = InitialContextFactory.getInitialContext();
      String binding = destinationContext + "/" + jndiSuffix;
      try
      {
         jndiContext.lookup(binding);
      }
      catch (NamingException e)
      {
         jndiContext.rebind(binding, result);
      }
      */
      
      //throw new UnsupportedOperationException("Can't create destination " + destinationContext + "/" + jndiSuffix);
      JMSDestinationFactory.getInstance().createDestination(type, jndiSuffix);
   }
   
   /**
    * Return the JMSProviderAdapter that should be used.
    *
    * @return The JMSProviderAdapter to use.
    */
   protected JMSProviderAdapter getJMSProviderAdapter()
           throws NamingException
   {
      //todo make this pluggable
      String providerAdapterJNDI = getProviderAdapterJNDI();
      
      log.debug("Looking up provider adapter: " + providerAdapterJNDI);
     
      return (JMSProviderAdapter) getInitialContext().lookup(providerAdapterJNDI);
   }

   /**
    * Try to get a destination type by looking up the destination JNDI, or
    * provide a default if there is not destinationJNDI or if it is not possible
    * to lookup.
    *
    * @param ctx             The naming context to lookup destinations from.
    * @param destinationJNDI The name to use when looking up destinations.
    * @return The destination type, either derived from destinationJDNI or
    *         DEFAULT_DESTINATION_TYPE
    */
   protected String getDestinationType(Context ctx, String destinationJNDI)
   {
      String destType = null;

      if (destinationJNDI != null)
      {
         try
         {
            Destination dest = (Destination) ctx.lookup(destinationJNDI);
            if (dest instanceof javax.jms.Topic)
            {
               destType = "javax.jms.Topic";
            }
            else if (dest instanceof javax.jms.Queue)
            {
               destType = "javax.jms.Queue";
            }
         }
         catch (NamingException ex)
         {
            log.debug("Could not do heristic lookup of destination ", ex);
         }

      }
      if (destType == null)
      {
         log.warn("Could not determine destination type, defaults to: " +
                 DEFAULT_DESTINATION_TYPE);

         destType = DEFAULT_DESTINATION_TYPE;
      }

      return destType;
   }
   
   /**
    * A messaging container is bound to a message destination and
    * thus has no JNDI binding of it's own.
    */
   @Override
   public boolean hasJNDIBinding(String jndiName)
   {
      return false;
   }
   
   public int getMinPoolSize()
   {
      return 1;
   }
   
   public int getMaxPoolSize()
   {
      return pool.getMaxSize();
   }
   
   public int getMaxMessages()
   {
      String maxMessages = activationSpec.get("maxMessages");
      if (maxMessages != null) 
         return Integer.parseInt(maxMessages);
      else
         return 1;
   }
   
   public int getKeepAliveMillis()
   {
      String keepAlive = activationSpec.get("keepAlive");
      if (keepAlive != null) 
         return Integer.parseInt(keepAlive);
      else
         return 60000;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.ejb3.timerservice.spi.TimedObjectInvoker#getTimedObjectId()
    */
   public String getTimedObjectId()
   {
      return getDeploymentQualifiedName();
   }
   
   @Inject
   public void setTimerServiceFactory(TimerServiceFactory factory)
   {
      this.timerServiceFactory = factory;
   }
}