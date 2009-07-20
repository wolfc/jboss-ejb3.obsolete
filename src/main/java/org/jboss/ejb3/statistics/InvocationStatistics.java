/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.statistics;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/** A method invocation statistics collection class.
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class InvocationStatistics implements Serializable
{
   private static final long serialVersionUID = -1637309757441812924L;

   /** The method invocations */
   private Map<String, TimeStatistic> methodStats;

   public long concurrentCalls = 0;
   public long maxConcurrentCalls = 0;
   public long lastResetTime = System.currentTimeMillis();

   public class TimeStatistic implements Serializable
   {
      private static final long serialVersionUID = -3717837456831579570L;
      
      public volatile long count;
      public volatile long minTime = Long.MAX_VALUE;
      public volatile long maxTime;
      public volatile long totalTime;

      public void reset()
      {
         count = 0;
         minTime = Long.MAX_VALUE;
         maxTime = 0;
         totalTime = 0;
      }
   }

   public InvocationStatistics()
   {
      methodStats = new ConcurrentHashMap<String, TimeStatistic>();
   }

   /** Update the TimeStatistic for the given method. This synchronizes on
    * m to ensure that the TimeStatistic for m is updated atomically.
    *
    * @param m the method to update the statistics for.
    * @param elapsed the elapsed time in milliseconds for the invocation.
    */
   public void updateStats(Method m, long elapsed)
   {
      TimeStatistic stat = (TimeStatistic) methodStats.get(m.getName());
      if (stat == null)
      {
         stat = new TimeStatistic();
         methodStats.put(m.getName(), stat);
      }
      stat.count++;
      stat.totalTime += elapsed;
      if (stat.minTime > elapsed)
         stat.minTime = elapsed;
      if (stat.maxTime < elapsed)
         stat.maxTime = elapsed;
   }

   public synchronized void callIn()
   {
      concurrentCalls++;
      if (concurrentCalls > maxConcurrentCalls)
         maxConcurrentCalls = concurrentCalls;
   }

   public synchronized void callOut()
   {
      concurrentCalls--;
   }

   /** Resets all current TimeStatistics.
    *
    */
   public void resetStats()
   {
      synchronized (methodStats)
      {
         Iterator<TimeStatistic> iter = methodStats.values().iterator();
         while (iter.hasNext())
         {
            TimeStatistic stat = (TimeStatistic) iter.next();
            stat.reset();
         }
      }
      maxConcurrentCalls = 0;
      lastResetTime = System.currentTimeMillis();
   }

   /** Access the current collection of method invocation statistics
    *
    * @return A HashMap<Method, TimeStatistic> of the method invocations
    */
   public Map<String,TimeStatistic> getStats()
   {
      return methodStats;
   }

   /** Generate an XML fragement for the InvocationStatistics. The format is
    * <InvocationStatistics concurrentCalls="c">
    *    <method name="aMethod" count="x" minTime="y" maxTime="z" totalTime="t" />
    *    ...
    * </InvocationStatistics>
    *
    * @return an XML representation of the InvocationStatistics
    */
   public String toString()
   {
      StringBuffer tmp = new StringBuffer("InvocationStatistics concurrentCalls='");
      tmp.append(concurrentCalls);
      tmp.append("'\n");

      HashMap<String,TimeStatistic> copy = new HashMap<String,TimeStatistic>(methodStats);
      Iterator<Entry<String,TimeStatistic>> iter = copy.entrySet().iterator();
      while (iter.hasNext())
      {
         Entry<String,TimeStatistic> entry = iter.next();
         final TimeStatistic stat = entry.getValue();
         if (stat != null)
         {
            tmp.append("method name='");
            tmp.append(entry.getKey());
            tmp.append("' count='");
            tmp.append(stat.count);
            tmp.append("' minTime='");
            tmp.append(stat.minTime);
            tmp.append("' maxTime='");
            tmp.append(stat.maxTime);
            tmp.append("' totalTime='");
            tmp.append(stat.totalTime);
            tmp.append("' \n");
         }
      }
      return tmp.toString();
   }
   
   public String toXmlString()
   {
      StringBuffer tmp = new StringBuffer("<InvocationStatistics concurrentCalls='");
      tmp.append(concurrentCalls);
      tmp.append("' >\n");

      HashMap<String,TimeStatistic> copy = new HashMap<String,TimeStatistic>(methodStats);
      Iterator<Entry<String,TimeStatistic>> iter = copy.entrySet().iterator();
      while (iter.hasNext())
      {
         Entry<String,TimeStatistic> entry = iter.next();
         TimeStatistic stat = (TimeStatistic) entry.getValue();
         if (stat != null)
         {
            tmp.append("<method name='");
            tmp.append(entry.getKey());
            tmp.append("' count='");
            tmp.append(stat.count);
            tmp.append("' minTime='");
            tmp.append(stat.minTime);
            tmp.append("' maxTime='");
            tmp.append(stat.maxTime);
            tmp.append("' totalTime='");
            tmp.append(stat.totalTime);
            tmp.append("' />\n");
         }
      }
      tmp.append("</InvocationStatistics>");
      return tmp.toString();
   }
}
