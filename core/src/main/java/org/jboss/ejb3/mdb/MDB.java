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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.aop.Domain;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.annotation.DefaultActivationSpecs;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.spec.NamedMethodMetaData;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class MDB extends MessagingContainer 
{
   /**
    * Default destination type. Used when no message-driven-destination is given
    * in ejb-jar, and a lookup of destinationJNDI from jboss.xml is not
    * successful. Default value: javax.jms.Topic.
    */
   protected final static String DEFAULT_DESTINATION_TYPE = "javax.jms.Topic";
   
   protected Class<?> messagingType = null;
   
   public MDB(String ejbName, Domain domain, ClassLoader cl, String beanClassName, Hashtable ctxProperties,
              Ejb3Deployment deployment, JBossMessageDrivenBeanMetaData beanMetaData) throws ClassNotFoundException
   {
      super(ejbName, domain, cl, beanClassName, ctxProperties, deployment, beanMetaData);
   }
   
   public Class<?> getMessagingType()
   {
      if (messagingType == null)
      {
         MessageDriven annotation = (MessageDriven) resolveAnnotation(MessageDriven.class);
         messagingType = annotation.messageListenerInterface();
         if (messagingType.getName().equals(Object.class.getName()))
         {
            Set<Class<?>> businessInterfaces = ProxyFactoryHelper.getBusinessInterfaces(getBeanClass(), false);
            if (businessInterfaces.size() > 1 || businessInterfaces.size() == 0) 
               throw new RuntimeException("Unable to choose messagingType interface for MDB " + getEjbName() + " from " + businessInterfaces);
            messagingType = businessInterfaces.iterator().next();
         }
      }

      return messagingType;
   }
   
   /*
   public MethodInfo getMethodInfo(Method method)
   {
      long hash = MethodHashing.calculateHash(method);
      MethodInfo info = super.getMethodInfo(hash);
      return info;
   }
   */

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
   
   protected JBossMessageDrivenBeanMetaData getMetaData()
   {
      // TODO: use generics
      return (JBossMessageDrivenBeanMetaData) super.getMetaData();
   }
   
   @Override
   protected NamedMethodMetaData getTimeoutMethodMetaData()
   {
      JBossMessageDrivenBeanMetaData metaData = getMetaData();
      if(metaData != null)
         return metaData.getTimeoutMethod();
      return null;
   }
   
   protected List<Class<?>> resolveBusinessInterfaces()
   {
      List<Class<?>> list = new ArrayList<Class<?>>();
      list.add(getMessagingType());
      return list;
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
