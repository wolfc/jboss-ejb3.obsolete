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
package org.jboss.annotation.ejb;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.ActivationConfigProperty;

import org.jboss.annotation.ejb.RemoteBinding;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class ConsumerImpl implements Consumer
{
   private String name = "";
   private HashMap<String, ActivationConfigProperty> activationConfig = new HashMap<String, ActivationConfigProperty>();
   
   public ConsumerImpl(Consumer consumer)
   {
      if (consumer != null)
      {
         name = consumer.name();
         if (consumer.activationConfig() != null)
         {
            for (ActivationConfigProperty prop : consumer.activationConfig())
            {
               activationConfig.put(prop.propertyName(), prop);
            }
         }
      }
   }
   
   public String name()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public ActivationConfigProperty[] activationConfig()
   {
      ActivationConfigProperty[] result = new ActivationConfigProperty[activationConfig.size()];
      int i = 0;
      for (Iterator<ActivationConfigProperty> it = activationConfig.values().iterator() ; it.hasNext() ; )
      {
         result[i++] = it.next(); 
      }
      return result;
   }
   
   public void addActivationConfig(ActivationConfigProperty config)
   {
      activationConfig.put(config.propertyName(), config);
   }

   public Class<? extends Annotation> annotationType()
   {
      return Consumer.class;
   }
}
