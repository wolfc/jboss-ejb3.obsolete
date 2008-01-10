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
package org.jboss.ejb3.test.entitycallback;

import java.util.HashMap;
import javax.ejb.Stateful;
import javax.ejb.Remote;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Stateful
@Remote(CallbackCounter.class)
public class CallbackCounterBean implements CallbackCounter, java.io.Serializable
{
   public static HashMap<String, HashMap> callbacks = new HashMap<String, HashMap>();

   public static void addCallback(String className, Class callbackAnnotation)
   {
      System.out.println("***** Adding " + callbackAnnotation.getName() + " callback to " + className);

      HashMap<Class, Integer> classCallbacks = callbacks.get(className);
      if (classCallbacks == null)
      {
         classCallbacks = new HashMap<Class, Integer>();

         callbacks.put(className, classCallbacks);
      }
      Integer i = classCallbacks.get(callbackAnnotation);
      if (i == null)
      {
         i = 0;
      }
      i++;
      System.out.println("count: " + i);
      classCallbacks.put(callbackAnnotation, i);
   }

   public int getCallbacks(String beanClass, Class callbackAnnotation)
   {
      HashMap<Class, Integer> classCallbacks = callbacks.get(beanClass);
      if (classCallbacks == null)
      {
         return 0;
      }

      Integer i = classCallbacks.get(callbackAnnotation);
      if (i != null)
      {
         return i;
      }

      return 0;
   }

   public static void clear()
   {
      callbacks = new HashMap<String, HashMap>();
   }
}
