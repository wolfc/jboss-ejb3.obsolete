/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.mdbtopic;

import javax.ejb.Stateless;
import javax.ejb.Remote;

import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 58399 $
 */
@Stateless
@Remote(TestStatus.class)
public class TestStatusBean implements TestStatus
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(TestStatusBean.class);
   
   public static int queueRan = 0;
   public static int overrideQueueRan = 0;
   public static int defaultedQueueRan = 0;
   public static int overrideDefaultedQueueRan = 0;
   public static int nondurableQueueRan = 0;
   public static int topicRan = 0;
   public static int bmtQueueRan = 0;
   static int cmtQueueRan = 0;
   public static int messageCount = 0;
   public static boolean interceptedTopic = false;
   public static boolean interceptedQueue = false;
   public static boolean postConstruct = false;
   public static boolean preDestroy = false;

   public void clear()
   {
      queueRan = 0;
      topicRan = 0;
      bmtQueueRan = 0;
      cmtQueueRan = 0;
      overrideQueueRan = 0;
      defaultedQueueRan = 0;
      overrideDefaultedQueueRan = 0;
      messageCount = 0;
      nondurableQueueRan = 0;
      interceptedTopic = false;
      interceptedQueue = false;
      postConstruct = false;
      preDestroy = false;
   }

   public int queueFired()
   {
      return queueRan;
   }
   
   public int overrideQueueFired()
   {
      return overrideQueueRan;
   }
   
   public int overrideDefaultedQueueFired()
   {
      return overrideDefaultedQueueRan;
   }
   
   public int defaultedQueueFired()
   {
      return defaultedQueueRan;
   }
   
   public int nondurableQueueFired()
   {
      return nondurableQueueRan;
   }

   public int topicFired()
   {
      return topicRan;
   }
   
   public int bmtQueueRan()
   {
      return bmtQueueRan;
   }

   public int cmtQueueRan()
   {
      return cmtQueueRan;
   }
   
   public boolean interceptedTopic()
   {
      return interceptedTopic;
   }

   public boolean interceptedQueue()
   {
      return interceptedQueue;
   }

   public boolean postConstruct()
   {
      return postConstruct;
   }

   public boolean preDestroy()
   {
      return preDestroy;
   }
   
   public int messageCount()
   {
      return messageCount;
   }
}
