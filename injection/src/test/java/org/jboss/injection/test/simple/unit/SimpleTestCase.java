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
package org.jboss.injection.test.simple.unit;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.injection.test.common.Counter;
import org.jboss.injection.test.simple.InjectedBean;

/**
 * Run with: -Djava.system.class.loader=org.jboss.aop.standalone.SystemClassLoader
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleTestCase extends TestCase
{
   public void test1() throws Exception
   {
      Counter.reset();
      
//      Class cls = Thread.currentThread().getContextClassLoader().loadClass("org.jboss.injection.test.simple.InjectedBean");
      
//      Object bean1 = cls.newInstance();
      
      InjectedBean bean = new InjectedBean();
      
      bean.check();
      
      assertEquals(1, Counter.postConstructs);
      
      bean = null;
      
      Runtime.getRuntime().gc();
      Runtime.getRuntime().runFinalization();
      
      assertEquals(1, Counter.preDestroys);
   }
   
   public static Test suite() throws Exception
   {
//      AspectManager.verbose = true;
      
//      AspectManager.debugClasses = true;
//      AspectManager.classicOrder = true;
      
//      System.err.println(Thread.currentThread().getContextClassLoader());
//      URLClassLoader ucl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
//      for(URL url : ucl.getURLs())
//         System.err.println(" - " + url);
//      
//      List<URL> urls = new ArrayList<URL>();
//      for(int i = 0; i < ucl.getURLs().length; i++)
//      {
//         System.err.println(ucl.getURLs()[i]);
//         urls.add(ucl.getURLs()[i]);
//      }
      
      //ClassLoader parent = URLClassLoader.newInstance(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader().getParent());
//      ClassLoader parent = URLClassLoader.newInstance(urls.toArray(new URL[0]), null);
//      SystemClassLoader cl = new SystemClassLoader(parent);
//      //AspectManager.instance().registerClassLoader(cl);
//      Thread.currentThread().setContextClassLoader(cl);
      
      URL url = Thread.currentThread().getContextClassLoader().getResource("simple/jboss-aop.xml");
      System.out.println(url);
      AspectXmlLoader.deployXML(url);
      
//      AspectManager.instance().registerClassLoader(cl);
//      System.err.println(AspectManager.getRegisteredCLs());
      
      return new TestSuite(SimpleTestCase.class);
   }
}
