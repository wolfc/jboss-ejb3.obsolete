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

import org.jboss.annotation.ejb.AcknowledgementMode;
import org.jboss.annotation.ejb.DefaultActivationSpecs;
import org.jboss.annotation.ejb.ResourceAdapter;
import org.jboss.aop.AspectManager;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.util.MethodHashing;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb3.*;
import org.jboss.ejb3.mdb.inflow.JBossMessageEndpointFactory;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.timerservice.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.TimerServiceFactory;
import org.jboss.jms.ConnectionFactoryHelper;
import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.logging.Logger;
import org.jboss.metadata.ActivationConfigPropertyMetaData;

import javax.ejb.*;
import javax.ejb.Timer;
import javax.jms.*;
import javax.jms.Queue;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class MDB extends MessagingContainer 
{
   private static final Logger log = Logger.getLogger(MDB.class);
   
   protected Class messagingType = null;
   /**
    * Default destination type. Used when no message-driven-destination is given
    * in ejb-jar, and a lookup of destinationJNDI from jboss.xml is not
    * successfull. Default value: javax.jms.Topic.
    */
   protected final static String DEFAULT_DESTINATION_TYPE = "javax.jms.Topic";

   public MDB(String ejbName, AspectManager manager, ClassLoader cl, String beanClassName, Hashtable ctxProperties,
              InterceptorInfoRepository interceptorRepository, Ejb3Deployment deployment)
   {
      super(ejbName, manager, cl, beanClassName, ctxProperties, interceptorRepository, deployment);
   }
   
   public Class getMessagingType()
   {
      if (messagingType == null)
      {
         MessageDriven annotation = (MessageDriven) resolveAnnotation(MessageDriven.class);
         messagingType = annotation.messageListenerInterface();
         if (messagingType.getName().equals(Object.class.getName()))
         {
            ArrayList<Class> list = ProxyFactoryHelper.getBusinessInterfaces(clazz);
            if (list.size() > 1 || list.size() == 0) throw new RuntimeException("unable to determine messagingType interface for MDB");
            messagingType = list.get(0);
         }
      }

      return messagingType;
   }
   
   public MethodInfo getMethodInfo(Method method)
   {
      long hash = MethodHashing.calculateHash(method);
      MethodInfo info = (MethodInfo) methodInterceptors.get(hash);
      return info;
   }

   public Map getActivationConfigProperties()
   {
      HashMap result = new HashMap();
      MessageDriven mdAnnotation = (MessageDriven) resolveAnnotation(MessageDriven.class);
      for (ActivationConfigProperty property : mdAnnotation.activationConfig())
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
   
   public void start() throws Exception
   {
      super.start();
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
   
   protected void populateActivationSpec()
   {
      DefaultActivationSpecs defaultSpecs = (DefaultActivationSpecs) resolveAnnotation(DefaultActivationSpecs.class);
      if (defaultSpecs != null)
      {
         activationSpec.merge(defaultSpecs.value());
      }

      MessageDriven md = (MessageDriven) resolveAnnotation(MessageDriven.class);

      activationSpec.merge(md.activationConfig());
   }
}
