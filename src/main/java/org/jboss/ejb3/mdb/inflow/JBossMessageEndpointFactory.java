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
package org.jboss.ejb3.mdb.inflow;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.ejb3.mdb.MessagingContainer;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.spec.ActivationConfigPropertyMetaData;

/**
 * EJBProxyFactory for inflow message driven beans
 *
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @author <a href="mailto:bill@jboss.com">Bill Burke</a>
 */
public class JBossMessageEndpointFactory implements MessageEndpointFactory
{
   private static final Logger log = Logger.getLogger(JBossMessageEndpointFactory.class);

   /*
    * For backwards compatibility it's called DeliveryActive.
    * See http://wiki.jboss.org/wiki/ConfigJBossMDB
    */
   /**
    * The activation config property name to specify activation.
    */
   private static final String ACTIVATE_ON_STARTUP = "DeliveryActive";
   
   /** Whether trace is enabled */
   protected boolean trace = log.isTraceEnabled();
   
   /** Our container */
   protected MessagingContainer container;
   
   /** The activation properties */
   @SuppressWarnings("deprecation")
   protected Map<String, org.jboss.metadata.ActivationConfigPropertyMetaData> properties = new HashMap<String, org.jboss.metadata.ActivationConfigPropertyMetaData>();
   
   /** The messaging type class */
   protected Class<?> messagingTypeClass;
   
   /** The resource adapter name */
   protected String resourceAdapterName;
   
   protected ObjectName resourceAdapterObjectName;
     
   /** The activation spec */
   protected ActivationSpec activationSpec;
   
   /** The interfaces */
   protected Class[] interfaces;
   
   /** Whether the MDB should make the subscription at initial deployment or wait for start/stopDelivery on the MBean */
   private boolean activateOnStartup = true;
   
   /** Is the delivery currently active? */
   private boolean deliveryActive = false;
    
   // Static --------------------------------------------------------
   
   /** The signature for createActivationSpec */
   protected String[] createActivationSpecSig = new String[]
   {
      Class.class.getName(),
      Collection.class.getName()
   };              
   
   /** The signature for activate/deactivateEndpint */
   protected String[] activationSig = new String[]
   {
      MessageEndpointFactory.class.getName(),
      ActivationSpec.class.getName()
   };              
         
   // Constructors --------------------------------------------------
   
   public JBossMessageEndpointFactory()
   {
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Get the message driven container
    * 
    * @return the container
    */
   public MessagingContainer getContainer()
   {
      return container;
   }
   
   // MessageEndpointFactory implementation -------------------------

   public MessageEndpoint createEndpoint(XAResource resource) throws UnavailableException
   {   
      trace = log.isTraceEnabled(); 
      
      if (trace)
         log.trace("createEndpoint " + this + " xaResource=" + resource);
          
      MessageEndpoint endpoint = createProxy(resource);
        
      if (trace)
         log.trace("Created endpoint " + endpoint + " from " + this);

      return endpoint;
   }
   
   protected MessageEndpoint createProxy(XAResource resource)
   {
      try 
      {
         Class proxyClass = java.lang.reflect.Proxy.getProxyClass(container.getBeanClass().getClassLoader(), interfaces);
            
         final Class[] constructorParams = {InvocationHandler.class};
         java.lang.reflect.Constructor proxyConstructor = proxyClass.getConstructor(constructorParams);
         
         MessageInflowLocalProxy proxy = new MessageInflowLocalProxy(container);
         proxy.setXaResource(resource);
         proxy.setMessageEndpointFactory(this);
         
         Object[] args = {proxy};
         MessageEndpoint endpoint = (MessageEndpoint)proxyConstructor.newInstance(args);
         return endpoint;
         
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public boolean isDeliveryActive()
   {
      return deliveryActive;
   }
   
   public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException
   {
      TransactionManagementType mtype = TxUtil.getTransactionManagementType(container.getAdvisor());
      if (mtype == javax.ejb.TransactionManagementType.BEAN) return false;


      TransactionAttribute attr = (TransactionAttribute)container.resolveAnnotation(method, TransactionAttribute.class);
      if (attr == null)
      {
         attr =(TransactionAttribute)container.resolveAnnotation(TransactionAttribute.class);
      }
      TransactionAttributeType type = TransactionAttributeType.REQUIRED;
      if (attr != null) type = attr.value();
      return type == javax.ejb.TransactionAttributeType.REQUIRED;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   public void start() throws Exception
   {
      // Resolve the message listener
      resolveMessageListener();
      resolveResourceAdapterName();
      // Resolve the resource adapter
      resolveResourceAdapter();
      // Create the activation config
      createActivationSpec();
      // Set up proxy parameters
      // Set the interfaces
      interfaces = new Class[] { MessageEndpoint.class, messagingTypeClass};
      if(activateOnStartup)
      {
         // Activate
         activate();
      }
   }
   
   public void stop() throws Exception
   {
      if(deliveryActive)
      {
         // Deactivate
         deactivate();
      }
   }
   // ContainerService implementation -------------------------------
   
   /**
    * Set the container for which this is an invoker to.
    *
    * @param container  The container for which this is an invoker to.
    */
   public void setContainer(final Container container)
   {
      this.container = (MessagingContainer) container;
   }
   
   // Object overrides ----------------------------------------------
   
   /**
    * Return a string representation of the current config state.
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(super.toString());
      buffer.append("{ resourceAdapter=").append(resourceAdapterName);
      buffer.append(", messagingType=").append(container.getMessagingType());
      buffer.append(", ejbName=").append(container.getEjbName());
      buffer.append(", activationConfig=").append(properties.values());
      buffer.append(", activationSpec=").append(activationSpec);
      buffer.append("}");
      return buffer.toString();
   }   
   
   // Protected -----------------------------------------------------

   /**
    * Resolve message listener class
    * 
    * @throws DeploymentException for any error
    */
   protected void resolveMessageListener() throws DeploymentException
   {
      messagingTypeClass = container.getMessagingType();
   }

   /**
    * Resolve the resource adapter name
    * 
    * @return the resource adapter name
    * @throws DeploymentException for any error
    */
   protected void resolveResourceAdapterName() throws DeploymentException
   {
      resourceAdapterName = container.getResourceAdaptorName();
   }

   protected void resolveResourceAdapter()
   {
      try
      {
         resourceAdapterObjectName = new ObjectName("jboss.jca:service=RARDeployment,name='" + resourceAdapterName + "'");
         // todo register with kernel and push dependencies to kernel
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Create the activation spec
    * 
    * @throws DeploymentException for any error
    */
   @SuppressWarnings("deprecation")
   protected void createActivationSpec() throws DeploymentException
   {
      properties = new HashMap<String, org.jboss.metadata.ActivationConfigPropertyMetaData>();
      
      for(ActivationConfigPropertyMetaData property : container.getActivationConfigProperties().values())
      {
         properties.put(property.getName(), new org.jboss.metadata.ActivationConfigPropertyMetaData(property));
      }
      
      // Filter out the MDB features
      if(properties.containsKey(ACTIVATE_ON_STARTUP))
      {
         this.activateOnStartup = Boolean.parseBoolean(properties.get(ACTIVATE_ON_STARTUP).getValue());
         properties.remove(ACTIVATE_ON_STARTUP);
      }
      
      Object[] params = new Object[] 
      {
         messagingTypeClass,
         properties.values()
      };
    
      try
      {
         activationSpec = (ActivationSpec) KernelAbstractionFactory.getInstance().invoke(resourceAdapterObjectName, "createActivationSpec", params, createActivationSpecSig);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Unable to create activation spec ra=" + resourceAdapterObjectName + 
               " messaging-type=" + messagingTypeClass.getName() + " properties=" + container.getActivationConfigProperties(), t);
      }
   }
   
   /**
    * Activate
    * 
    * @throws DeploymentException for any error
    */
   public synchronized void activate() throws DeploymentException
   {
      if(deliveryActive)
         throw new IllegalStateException("Delivery is already active");
      
      Object[] params = new Object[] { this, activationSpec };
      try
      {
         KernelAbstractionFactory.getInstance().invoke(resourceAdapterObjectName, "endpointActivation", params, activationSig);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Endpoint activation failed ra=" + resourceAdapterObjectName + 
               " activationSpec=" + activationSpec, t);
      }
      deliveryActive = true;
   }
   
   /**
    * Deactivate
    */
   public synchronized void deactivate()
   {
      if(!deliveryActive)
         throw new IllegalStateException("Delivery is already deactivated");
      
      Object[] params = new Object[] { this, activationSpec };
      try
      {
         KernelAbstractionFactory.getInstance().invoke(resourceAdapterObjectName, "endpointDeactivation", params, activationSig);
      }
      catch (Throwable t)
      {
         log.warn("Endpoint activation failed ra=" + resourceAdapterObjectName + 
               " activationSpec=" + activationSpec, t);
      }
      deliveryActive = false;
   }
}
