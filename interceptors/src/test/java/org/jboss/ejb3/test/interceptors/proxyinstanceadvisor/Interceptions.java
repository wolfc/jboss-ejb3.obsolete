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
package org.jboss.ejb3.test.interceptors.proxyinstanceadvisor;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class Interceptions
{
   private static ThreadLocal<PerInstanceInterceptor> perInstanceInterceptor = new ThreadLocal<PerInstanceInterceptor>();
   
   private static ThreadLocal<PerJoinpointInterceptor> perJoinpointInterceptor = new ThreadLocal<PerJoinpointInterceptor>();
   
   private static ThreadLocal<ProxiedBean> proxiedBean = new ThreadLocal<ProxiedBean>();
   
   private static ThreadLocal<Integer> perInstanceCalls = new ThreadLocal<Integer>()
   {
      @Override
      protected Integer initialValue()
      {
         return 0;
      }
   };
   
   private static ThreadLocal<Integer> perJoinpointCalls = new ThreadLocal<Integer>()
   {
      @Override
      protected Integer initialValue()
      {
         return 0;
      }
   };

   private static ThreadLocal<Integer> proxiedBeanCalls = new ThreadLocal<Integer>()
   {
      @Override
      protected Integer initialValue()
      {
         return 0;
      }
   };

   public static PerInstanceInterceptor getPerInstanceInterceptor()
   {
      return perInstanceInterceptor.get();
   }
   
   public static void setPerInstanceInterceptor(PerInstanceInterceptor perInstanceInterceptor)
   {
      Interceptions.perInstanceInterceptor.set(perInstanceInterceptor);
      addPerInstanceCall();
   }
   
   public static PerJoinpointInterceptor getPerJoinpointInterceptor()
   {
      return perJoinpointInterceptor.get();
   }
   
   public static void setPerJoinpointInterceptor(PerJoinpointInterceptor perJoinpointInterceptor)
   {
      Interceptions.perJoinpointInterceptor.set(perJoinpointInterceptor);
      addPerJoinpointCall();
   }
   
   public static ProxiedBean getProxiedBean()
   {
      return proxiedBean.get();
   }
   
   public static void setProxiedBean(ProxiedBean proxiedBean)
   {
      Interceptions.proxiedBean.set(proxiedBean);
      addProxiedBeanCall();
   }
   
   public static synchronized int getPerInstanceCalls()
   {
      return perInstanceCalls.get();
   }
   
   private static synchronized void addPerInstanceCall()
   {
      int calls = getPerInstanceCalls();
      calls++;
      Interceptions.perInstanceCalls.set(calls);
   }
   
   public static synchronized int getPerJoinpointCalls()
   {
      return perJoinpointCalls.get();
   }
   
   private static void addPerJoinpointCall()
   {
      int calls = getPerJoinpointCalls();
      calls++;
      Interceptions.perJoinpointCalls.set(calls);
   }
   
   public static synchronized int getProxiedBeanCalls()
   {
      return proxiedBeanCalls.get();
   }
   
   private static void addProxiedBeanCall()
   {
      int calls = getProxiedBeanCalls();
      calls++;
      Interceptions.proxiedBeanCalls.set(calls);
   }
   
   public static void reset()
   {
      perInstanceInterceptor.set(null);
      perJoinpointInterceptor.set(null);
      proxiedBean.set(null);
      perInstanceCalls.set(0);
      perJoinpointCalls.set(0);
      proxiedBeanCalls.set(0);
   }
   
}
