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
package org.jboss.ejb;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.ejb.MessageDriven;

import javax.ejb.ActivationConfigProperty;

/**
 * // *
 *
 * @author <a href="mailto:bill@jboss.org">William DeCoste</a>
 * @version $Revision$
 */
public class MessageDrivenImpl implements MessageDriven
{
   private String name = "";
   private String mn = "";
   private String desc = "";
   private Class listenerInterface = Object.class;

   private HashMap<String, ActivationConfigProperty> activationConfigProperties = new HashMap<String, ActivationConfigProperty>();

   public MessageDrivenImpl(String name, ActivationConfigProperty[] activationConfigProperties)
   {
      this.name = name;
      for (ActivationConfigProperty property : activationConfigProperties)
      {
         this.activationConfigProperties.put(property.propertyName(), property);
      }
   }

   public String name()
   {
      return name;
   }

   public ActivationConfigProperty[] activationConfig()
   {
      ActivationConfigProperty[] properties = new ActivationConfigProperty[activationConfigProperties.size()];
      activationConfigProperties.values().toArray(properties);
      return properties;
   }

   public Class<? extends Annotation> annotationType()
   {
      return MessageDriven.class;
   }
   
   public String mappedName() { return mn;}
   public void setMappedName(String mn) { this.mn = mn; }
   public String description() { return desc;}
   public void setDescription(String desc) { this.desc = desc; }
   public Class messageListenerInterface() { return listenerInterface;}
   public void setMessageListenerInterface(Class clazz) { this.listenerInterface = clazz; }
   
   public void merge(MessageDriven annotation)
   {   
      if (name.length() == 0)
         name = annotation.name();
      
      if (mn.length() == 0)
         mn = annotation.mappedName();
      
      if (desc.length() == 0)
         desc = annotation.description();
      
      if(listenerInterface == Object.class)
         listenerInterface = annotation.messageListenerInterface();
      
      for (ActivationConfigProperty property : annotation.activationConfig())
      {
         if (!activationConfigProperties.containsKey(property.propertyName()))
         {
            activationConfigProperties.put(property.propertyName(), property);
         }
      }
   }
   
   @Override
   public String toString()
   {
      return super.toString() + "{name=" + name + ",description=" + desc + ",mappedName=" + mn + ",messageListenerInterface=" + listenerInterface + "}";
   }
}
