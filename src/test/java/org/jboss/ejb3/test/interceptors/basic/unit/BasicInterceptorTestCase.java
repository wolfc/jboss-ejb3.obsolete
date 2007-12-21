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
package org.jboss.ejb3.test.interceptors.basic.unit;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import junit.framework.TestCase;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.classpool.AOPClassLoaderScopingPolicy;
import org.jboss.ejb3.interceptors.aop.DomainClassLoader;
import org.jboss.ejb3.interceptors.lang.ScopedClassLoader;
import org.jboss.logging.Logger;

/**
 * TODO: this is no longer true<br/>
 * Run with: -Djava.system.class.loader=org.jboss.aop.standalone.SystemClassLoader
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BasicInterceptorTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(BasicInterceptorTestCase.class);
   
   public void _test1() throws Exception
   {
      System.setProperty("java.io.tmpdir", "/tmp/aop");
      
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      
//      AspectManager.verbose = true;
      
      // Bootstrap AOP
      URL url = Thread.currentThread().getContextClassLoader().getResource("basic/jboss-aop.xml");
      System.out.println(url);
      AspectXmlLoader.deployXML(url);

      // Bootstrap the basic container
      String containerName = "BasicContainer";
      
      DomainDefinition domain = AspectManager.instance().getContainer(containerName);
      if(domain == null)
         throw new IllegalArgumentException("Domain definition for container '" + containerName + "' can not be found");
      
      AspectManager manager = domain.getManager();
      
//      ClassContainer container = new ClassContainer("BasicBean", BasicBean.class, manager);
      
      //container.setClass(classLoader.loadClass("org.jboss.ejb3.test.interceptors.basic.BasicBean"));
      //container.setClass(BasicBean.class);
      
      //container.initializeClassContainer();
      //container.rebuildInterceptors();
      
//      Object obj = classLoader.loadClass("org.jboss.ejb3.test.interceptors.basic.BasicBean").newInstance();
      
      log.info("pointCuts = " + domain.getManager().getPointcuts());
      
//      obj = container.getConstructors()[0].newInstance();
      
//      BasicBean bean;
//      {
//         bean = (BasicBean) container.getConstructors()[0].newInstance();
//         String result = bean.sayHi("Test");
//      }
      
//      {
//         BasicBean bean = (BasicBean) container.construct();
//         String result = bean.sayHi("Test 2");
//      }
   }
   
   public void _test2() throws Throwable
   {
      //System.setProperty("java.io.tmpdir", "/tmp/aop");
      //System.setProperty("jboss.aop.optimized", "false"); // no effect
      
//      AspectManager.verbose = true;
      //AspectManager.debugClasses = true;
      //AspectManager.classicOrder = true;
      
      // A trick to get a nice 'deployment' class loader
      // eclipse
//      URLClassLoader ucl = (URLClassLoader) ClassLoader.getSystemClassLoader();
//      URL deploymentURL = ucl.getURLs()[0];
      // both eclipse & maven
      URL deploymentURL = getClass().getClassLoader().getResource(".");
      System.out.println(deploymentURL);
      ScopedClassLoader deploymentClassLoader = new ScopedClassLoader(new URL[] { deploymentURL });
      
      // Bootstrap AOP
      URL url = Thread.currentThread().getContextClassLoader().getResource("basic/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);

      // Bootstrap the basic container
      String containerName = "InterceptorContainer";
      
      DomainDefinition domainDefinition = AspectManager.instance().getContainer(containerName);
      if(domainDefinition == null)
         throw new IllegalArgumentException("Domain definition for container '" + containerName + "' can not be found");
      
      final Domain domain = (Domain) domainDefinition.getManager();
      
      ClassLoader classLoader = new DomainClassLoader(Thread.currentThread().getContextClassLoader(), deploymentClassLoader, domain);
      Thread.currentThread().setContextClassLoader(classLoader);
      
      // The moment an instrumented class comes up it does AspectManager.instance(classLoader), which
      // should return the classLoader created above. So we tie in a class loader scoping policy.
      // Note that this won't work in AS.
      AOPClassLoaderScopingPolicy classLoaderScopingPolicy = new AOPClassLoaderScopingPolicy() {
         public Domain getDomain(ClassLoader classLoader, AspectManager parent)
         {
            //log.debug("getDomain");
            if(classLoader instanceof DomainClassLoader)
               return ((DomainClassLoader) classLoader).getDomain();
            return null;
         }

         public Domain getTopLevelDomain(AspectManager parent)
         {
            throw new RuntimeException("NYI");
         }

         public boolean isScoped(ClassLoader classLoader)
         {
            throw new RuntimeException("NYI");
         }
      };
      AspectManager.setClassLoaderScopingPolicy(classLoaderScopingPolicy);
      
      Class<?> testRunnerClass = classLoader.loadClass("org.jboss.ejb3.test.interceptors.basic.BasicTestRunner");
      Object testRunner = testRunnerClass.newInstance();
      
      try
      {
         testRunnerClass.getMethod("test2").invoke(testRunner);
      }
      catch(InvocationTargetException e)
      {
         throw e.getCause();
      }
      
//      Class<?> beanClass = classLoader.loadClass("org.jboss.ejb3.test.interceptors.basic.BasicBean");
//      Object obj = beanClass.newInstance();
//      System.out.println(obj.getClass() + " " + obj.getClass().getClassLoader());
//      System.out.println("  " + Arrays.toString(obj.getClass().getInterfaces()));
//      Method method = obj.getClass().getMethod("sayHi", String.class);
//      String result = (String) method.invoke(obj, "Test");
//      System.out.println(result);
//      
//      obj.getClass().getMethod("intercepted").invoke(obj);
      
//      System.out.println(AdvisorFactory.getClassAdvisor(obj.getClass(), domain));
   }
}
