/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.timerservice.quartz;

import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class TimedObjectInvokerRegistry
{
   private static final Logger log = Logger.getLogger(TimedObjectInvokerRegistry.class);
   
   private static Map<String, TimedObjectInvoker> registry = new HashMap<String, TimedObjectInvoker>();
   
   static TimedObjectInvoker getTimedObjectInvoker(String id)
   {
      TimedObjectInvoker invoker = registry.get(id);
      if(invoker == null)
         throw new IllegalStateException("TimedObjectInvoker " + id + " + is not registered");
      return invoker;
   }
   
   static boolean hasTimedObjectInvoker(String id)
   {
      return registry.containsKey(id);
   }
   
   static void register(TimedObjectInvoker invoker)
   {
      String id = invoker.getTimedObjectId();
      if(hasTimedObjectInvoker(id))
         throw new IllegalStateException("TimedObjectInvoker " + id + " + is already registered");
      
      registry.put(id, invoker);
      
      log.debug("Registered " + id);
   }
   
   static void unregister(TimedObjectInvoker invoker)
   {
      String id = invoker.getTimedObjectId();
      if(!hasTimedObjectInvoker(id))
         throw new IllegalStateException("TimedObjectInvoker " + id + " + is not registered");
      
      registry.put(id, invoker);
      
      log.debug("Unregistered " + id);
   }
}
