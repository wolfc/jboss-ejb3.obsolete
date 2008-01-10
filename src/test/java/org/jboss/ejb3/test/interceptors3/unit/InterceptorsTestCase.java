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
package org.jboss.ejb3.test.interceptors3.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.interceptors3.AssemblyRemoteIF;
import org.jboss.test.JBossTestCase;

/**
 * From TCK: ejb30/assembly/librarydirectory/defaultname
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InterceptorsTestCase extends JBossTestCase
{
   public InterceptorsTestCase(String name)
   {
      super(name);
   }

   public void testIntercept() throws Exception
   {
      interceptTest("AssemblyBean/remote");
   }

   public void testInterceptAnnotated() throws Exception
   {
      interceptTest("AnnotatedAssemblyBean/remote");
   }
   
   private void interceptTest(String jndi) throws Exception
   {
      AssemblyRemoteIF bean = (AssemblyRemoteIF) getInitialContext().lookup(jndi);
      int actual = bean.remoteAdd(1, 2);
      assertEquals(203, actual);      
   }

   public void testInterceptDifferentMethods() throws Exception
   {
      interceptDifferentMethodsTest("AssemblyBean/remote");
   }

   public void testInterceptDifferentMethodsAnnotated() throws Exception
   {
      interceptDifferentMethodsTest("AnnotatedAssemblyBean/remote");
   }
   
   private void interceptDifferentMethodsTest(String jndi) throws Exception
   {
      AssemblyRemoteIF bean = (AssemblyRemoteIF) getInitialContext().lookup(jndi);
      int actual = bean.remoteAdd(1, 2);
      assertEquals(203, actual);
      
      actual = bean.remoteMultiply(0, 0);
      assertEquals(10000, actual);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(InterceptorsTestCase.class, "interceptors3-test.jar");
   }
}