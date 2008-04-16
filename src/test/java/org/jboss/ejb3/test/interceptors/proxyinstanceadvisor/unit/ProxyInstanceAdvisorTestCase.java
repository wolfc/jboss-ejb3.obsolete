/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.unit;

import junit.framework.TestCase;

import org.jboss.aspects.common.AOPDeployer;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.Interceptions;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.MyInterface;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.PerInstanceInterceptor;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.PerJoinpointInterceptor;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.ProxiedBean;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.ProxyContainerWithPool;
import org.jboss.ejb3.test.interceptors.proxyinstanceadvisor.SimplePoolInterceptor;
import org.jboss.logging.Logger;

/**
 * Make sure that PER_INSTANCE and PER_JOINPOINT container interceptors work with EJB 3 
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ProxyInstanceAdvisorTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(ProxyInstanceAdvisorTestCase.class);

   AOPDeployer deployer = new AOPDeployer("proxyinstanceadvisor/jboss-aop.xml");
   
   @Override
   protected void setUp() throws Exception
   {
      log.info(deployer.deploy());
   }

   @Override
   protected void tearDown() throws Exception
   {
      log.info(deployer.undeploy());
   }

   public void test1() throws Throwable
   {
      log.info("======= ProxyInstanceAdvisor.test1()");
      //AspectManager.verbose = true;
      
      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      ProxyContainerWithPool<ProxiedBean> container = new ProxyContainerWithPool<ProxiedBean>("ProxyInstanceAdvisorTestCase", "InterceptorContainer", ProxiedBean.class);
      
      
      Class<?> interfaces[] = { MyInterface.class };
      MyInterface proxy = container.constructProxy(interfaces);
      
      
      reset(true);
      String result = proxy.sayHi("Me");
      assertEquals("Hi Me", result);
      ProxiedBean bean1hi = Interceptions.getProxiedBean();
      PerInstanceInterceptor pi1hi = Interceptions.getPerInstanceInterceptor();
      PerJoinpointInterceptor pj1hi = Interceptions.getPerJoinpointInterceptor();
      assertEquals(1, Interceptions.getProxiedBeanCalls());
      assertEquals(1, Interceptions.getPerInstanceCalls());
      assertEquals(1, Interceptions.getPerJoinpointCalls());
      
      reset(false);
      result = proxy.sayBye("Me");
      assertEquals("Bye Me", result);
      ProxiedBean bean1bye = Interceptions.getProxiedBean();
      PerInstanceInterceptor pi1bye = Interceptions.getPerInstanceInterceptor();
      PerJoinpointInterceptor pj1bye = Interceptions.getPerJoinpointInterceptor();
      assertEquals(1, Interceptions.getProxiedBeanCalls());
      assertEquals(1, Interceptions.getPerInstanceCalls());
      assertEquals(1, Interceptions.getPerJoinpointCalls());
      
      assertSame(bean1hi, bean1bye);
      assertSame(pi1hi, pi1bye);
      assertNotSame(pj1hi, pj1bye);

      reset(true);
      result = proxy.sayHi("Me");
      assertEquals("Hi Me", result);
      ProxiedBean bean2hi = Interceptions.getProxiedBean();
      PerInstanceInterceptor pi2hi = Interceptions.getPerInstanceInterceptor();
      PerJoinpointInterceptor pj2hi = Interceptions.getPerJoinpointInterceptor();
      assertNotSame(bean1hi, bean2hi);
      assertEquals(1, Interceptions.getProxiedBeanCalls());
      assertEquals(1, Interceptions.getPerInstanceCalls());
      assertEquals(1, Interceptions.getPerJoinpointCalls());
      
      //FIXME - These must be enabled to start the test
      assertNotSame(pi2hi, pi1hi);
      assertNotSame(pj2hi, pj1hi);

      log.info("======= Done");
   }
   
   public void testThreadedDifferentInstance() throws Throwable
   {
      log.info("======= ProxyInstanceAdvisor.test1()");
      runThreadedTest(true);
      log.info("======= Done");
   }
   
   public void testThreadedSameInstance() throws Throwable
   {
      log.info("======= ProxyInstanceAdvisor.test1()");
      runThreadedTest(false);
      log.info("======= Done");
   }
   
   private void runThreadedTest(boolean differentInstances) throws Throwable
   {
      //AspectManager.verbose = true;
      
      Thread.currentThread().setContextClassLoader(MyInterface.class.getClassLoader());
      
      ProxyContainerWithPool<ProxiedBean> container = new ProxyContainerWithPool<ProxiedBean>("ProxyInstanceAdvisorTestCase", "InterceptorContainer", ProxiedBean.class);

      Class<?> interfaces[] = { MyInterface.class };
      MyInterface proxy = container.constructProxy(interfaces);

      CallSleepyHelloRunnable sleepyRunner = new CallSleepyHelloRunnable(proxy, 5000);
      Thread thread = new Thread(sleepyRunner);
      System.out.println("My thread " + Thread.currentThread().getName() + " new thread " + thread.getName());
      thread.start();
      
      //Give other thread a chance to start
      Thread.sleep(1000);         
      
      reset(differentInstances);
      
      String result = proxy.sleepyHello(0, "Me");
      assertEquals("Hi Me", result);
      ProxiedBean beanMine = Interceptions.getProxiedBean();
      PerInstanceInterceptor piMine = Interceptions.getPerInstanceInterceptor();
      PerJoinpointInterceptor pjMine = Interceptions.getPerJoinpointInterceptor();
      assertEquals(1, Interceptions.getProxiedBeanCalls());
      assertEquals(1, Interceptions.getPerInstanceCalls());
      assertEquals(1, Interceptions.getPerJoinpointCalls());

      while(thread.isAlive())
      {
         Thread.sleep(500);
      }
      
      ProxiedBean beanThread = sleepyRunner.getProxiedBean();
      PerInstanceInterceptor piThread = sleepyRunner.getPerInstanceInterceptor();
      PerJoinpointInterceptor pjThread = sleepyRunner.getPerJoinpointInterceptor();
      assertEquals(1, sleepyRunner.getProxiedBeanCalls());
      assertEquals(1, sleepyRunner.getPerInstanceCalls());
      assertEquals(1, sleepyRunner.getPerJoinpointCalls());
      
      assertNotNull(beanThread);
      assertNotNull(piThread);
      assertNotNull(pjThread);
      
      if (differentInstances)
      {
         assertNotSame(beanMine, beanThread);
         assertNotSame(piMine, piThread);
         assertNotSame(pjMine, pjThread);
      }
      else
      {
         assertSame(beanMine, beanThread);
         assertSame(piMine, piThread);
         assertSame(pjMine, pjThread);
      }
      log.info("======= Done");
   }
   
   
   private static void reset(boolean createNewInstance)
   {
      Interceptions.reset();
      System.out.println("Setting createNewInstance " + createNewInstance + " for thread " + Thread.currentThread().getName());
      SimplePoolInterceptor.createNewInstance = createNewInstance;      
   }
   
   private static class CallSleepyHelloRunnable implements Runnable
   {
      MyInterface proxy;
      long sleepTime;
    
      PerInstanceInterceptor perInstanceInterceptor;
      PerJoinpointInterceptor perJoinpointInterceptor;
      ProxiedBean proxiedBean;
      int proxiedBeanCalls;
      int perInstanceCalls;
      int perJoinpointCalls;
      
      public CallSleepyHelloRunnable(MyInterface proxy, long sleepTime)
      {
         this.proxy = proxy;
         this.sleepTime = sleepTime;
      }

      public void run()
      {
         reset(true);
         proxy.sleepyHello(sleepTime, "Kabir");
         perInstanceInterceptor = Interceptions.getPerInstanceInterceptor();
         perJoinpointInterceptor = Interceptions.getPerJoinpointInterceptor();
         proxiedBean = Interceptions.getProxiedBean();
         proxiedBeanCalls = Interceptions.getProxiedBeanCalls();
         perInstanceCalls = Interceptions.getPerInstanceCalls();
         perJoinpointCalls = Interceptions.getPerJoinpointCalls();
      }

      public PerInstanceInterceptor getPerInstanceInterceptor()
      {
         return perInstanceInterceptor;
      }

      public PerJoinpointInterceptor getPerJoinpointInterceptor()
      {
         return perJoinpointInterceptor;
      }

      public ProxiedBean getProxiedBean()
      {
         return proxiedBean;
      }

      public int getProxiedBeanCalls()
      {
         return proxiedBeanCalls;
      }

      public int getPerInstanceCalls()
      {
         return perInstanceCalls;
      }

      public int getPerJoinpointCalls()
      {
         return perJoinpointCalls;
      }
   }
}
