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
package org.jboss.ejb3.test.standalone.unit;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;


import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.embedded.EJB3StandaloneDeployer;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class StandardTestCase extends TestCase
{
   private static final Logger log = Logger
   .getLogger(StandardTestCase.class);
   
   private static boolean booted = false;
   
   private static List tests = new LinkedList();
   
   static
   {
      tests.add(new StandardTestCaseTest("org.jboss.ejb3.test.jca.inflowmdb.unit.InflowUnitTestCase", "jmsinflowmdb.jar", "testJMS", "standard/jca-inflowmdb-beans.xml"));
      tests.add(new StandardTestCaseTest("org.jboss.ejb3.test.mdb.unit.MDBUnitTestCase", "mdb-test.jar", null, "security-beans.xml,standard/testjms.xml"));
   }

   public StandardTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(StandardTestCase.class);
      
      return suite;
   }

   public static void startupEmbeddedJboss(StandardTestCaseTest test)
   {
      EJB3StandaloneBootstrap.boot(null);
      EJB3StandaloneBootstrap.deployXmlResource("jboss-jms-beans.xml");
      
      if (test.xmlResources != null)
      {
         StringTokenizer tokenizer = new StringTokenizer(test.xmlResources, ",");
         while (tokenizer.hasMoreTokens())
         {
            String testXml = tokenizer.nextToken();
            EJB3StandaloneBootstrap.deployXmlResource(testXml);
         }
      }
   }

   public static void shutdownEmbeddedJboss()
   {
      EJB3StandaloneBootstrap.shutdown();
   }


   protected InitialContext getInitialContext() throws Exception
   {
      return new InitialContext(getInitialContextProperties());
   }

   protected Hashtable getInitialContextProperties()
   {
      return EJB3StandaloneBootstrap.getInitialContextProperties();
   }

   private Properties getDefaultPersistenceProperties()
           throws IOException
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("default.persistence.properties");
      assertNotNull(is);
      Properties defaults = new Properties();
      defaults.load(is);
      return defaults;
   }

   public void testStandardTests() throws Throwable
   {  
      Iterator standardTests = tests.iterator();
      while (standardTests.hasNext())
      {
         StandardTestCaseTest test = (StandardTestCaseTest)standardTests.next();
         
         System.out.println("Testing standard test " + test.testClass);
         
         startupEmbeddedJboss(test);
         EJB3StandaloneDeployer deployer = new EJB3StandaloneDeployer();
         deployer.setKernel(EJB3StandaloneBootstrap.getKernel());
         deployer.setJndiProperties(getInitialContextProperties());
         
         if (test.deployments != null)
         {
            StringTokenizer tokenizer = new StringTokenizer(test.deployments, ",");
            while (tokenizer.hasMoreTokens())
            {
               String testJar = tokenizer.nextToken();
               URL archive = Thread.currentThread().getContextClassLoader().getResource(testJar);
               deployer.getArchives().add(archive);
            }
         }
         
         if (test.testMethods != null)
            runTest(deployer, test.testClass, test.testMethods);
         else
            runTest(deployer, test.testClass);
         
         shutdownEmbeddedJboss();
      }
   }
   
   private void runTest(EJB3StandaloneDeployer deployer, String testClassName, String methods)
      throws Exception
   {   
      startTest(deployer);
           
      Class testClass = Thread.currentThread().getContextClassLoader().loadClass(testClassName);
      String[] constructorParams = {testClass.getName()};
      Class[] constructorSignature = {String.class};
      Constructor constructor = testClass.getConstructor(constructorSignature);
      Object testCase = constructor.newInstance(constructorParams);
      
      Class[] signature = new Class[0];
      StringTokenizer methodTokenizer = new StringTokenizer(methods, ",");
      Object[] params = new Object[0];
      while (methodTokenizer.hasMoreTokens())
      {
         String methodName = methodTokenizer.nextToken();
         Method method = testClass.getMethod(methodName, signature);
         System.out.println("-- executing method " + method.getName());
         method.invoke(testCase, params);
      }
   
      stopTest(deployer);
   }
   
   private void runTest(EJB3StandaloneDeployer deployer, String testClassName)
      throws Exception
   {     
      startTest(deployer);
       
      Class testClass = Thread.currentThread().getContextClassLoader().loadClass(testClassName);
      String[] constructorParams = {testClass.getName()};
      Class[] signature = {String.class};
      Constructor constructor = testClass.getConstructor(signature);
      Object testCase = constructor.newInstance(constructorParams);
      
      Object[] params = new Object[0];
      for (Method method: testClass.getMethods())
      {
         if (method.getName().startsWith("test") && method.getParameterTypes().length == 0)
         {
            System.out.println("-- executing method " + method.getName());
            method.invoke(testCase, params);
         }
      }

      stopTest(deployer);
   }
   
   private void startTest(EJB3StandaloneDeployer deployer)
      throws Exception
   {
      deployer.create();
      deployer.start();
   }
   
   private void stopTest(EJB3StandaloneDeployer deployer)
      throws Exception
   {
      deployer.stop();
      deployer.destroy();
   }
   
   private void lookup(String name)
   {
      System.out.println("lookup " + name);
      try {
         InitialContext jndiContext = new InitialContext();
         NamingEnumeration names = jndiContext.list(name);
         if (names != null){
            while (names.hasMore()){
               System.out.println("  " + names.next());
            }
         }
      } catch (Exception e){
      }
   }
   
   static class StandardTestCaseTest
   {
      public String testClass;
      public String deployments;
      public String testMethods;
      public String xmlResources;
      
      public StandardTestCaseTest(String testClass, String deployments, String testMethods, String xmlResources)
      {
         this.testClass = testClass;
         this.deployments = deployments;
         this.testMethods = testMethods;
         this.xmlResources = xmlResources;
      }
   }
}