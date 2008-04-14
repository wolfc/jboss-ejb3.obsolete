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
package org.jboss.ejb3.test.interceptors.basic.unit;

import java.net.URL;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.classpool.AOPClassLoaderScopingPolicy;
import org.jboss.ejb3.interceptors.aop.DomainClassLoader;
import org.jboss.ejb3.interceptors.lang.ScopedClassLoader;
import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class BasicTestSetup
{
   private static final Logger log = Logger.getLogger(BasicTestSetup.class);

   Object runner = null;

   public BasicTestSetup() throws Exception
   {
      log.info("BasicTestSetup constructor");
   }
   
   public void setUp() throws Exception
   {
      log.info("===== Setting up");
      
      // Neat trick to find the deployment URL
      // We can't use '.' as resource, because on some machines the classpath order is different,
      // so we might get target/classes instead of target/test-classes.
      String resourceName = "log4j.xml";
      String spec = getClass().getClassLoader().getResource(resourceName).toString();
      URL deploymentURL = new URL(spec.substring(0, spec.length() - resourceName.length()));
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
      
      runner = classLoader.loadClass("org.jboss.ejb3.test.interceptors.basic.unit.BasicTestRunner").newInstance();
      log.info("===== Done setting up");
   }
   
   public Object getRunner()
   {
      return runner;
   }
}
