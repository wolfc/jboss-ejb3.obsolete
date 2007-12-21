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

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.classpool.AOPClassLoaderScopingPolicy;
import org.jboss.ejb3.interceptors.aop.DomainClassLoader;
import org.jboss.ejb3.interceptors.lang.ScopedClassLoader;
import org.jboss.logging.Logger;

/**
 * Bootstrap the interceptor container.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BasicTestSuite extends TestSuite
{
   private static final Logger log = Logger.getLogger(BasicTestSuite.class);
   
   private ClassLoader classLoader;
   
   public BasicTestSuite() throws Exception
   {
      // Neat trick to find the deployment URL
      URL deploymentURL = getClass().getClassLoader().getResource(".");
      log.info(deploymentURL);
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
      
      this.classLoader = new DomainClassLoader(Thread.currentThread().getContextClassLoader(), deploymentClassLoader, domain);
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
      
      // We're done
   }
   
   public static Test suite() throws Exception
   {
      BasicTestSuite suite = new BasicTestSuite();
      suite.addTestSuite(BasicTestRunner.class);
      return suite;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void addTestSuite(Class testClass)
   {
      // trick or treat
      try
      {
         super.addTestSuite(classLoader.loadClass(testClass.getName()));
      }
      catch(final ClassNotFoundException e)
      {
         Test test = new TestCase("error") {
            @Override
            protected void runTest() throws Throwable
            {
               fail("Failed to load class" + e.getMessage());
            }
         };
         super.addTest(test);
      }
   }
}
