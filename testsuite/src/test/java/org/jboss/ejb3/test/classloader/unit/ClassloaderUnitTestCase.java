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
package org.jboss.ejb3.test.classloader.unit;

import junit.framework.Test;

import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;

import org.jboss.ejb3.test.classloader.Session30;

/** 
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
public class ClassloaderUnitTestCase extends JBossTestCase
{
   
   public ClassloaderUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testEJBOverride() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Session30 stateless = (Session30)jndiContext.lookup("Session30");
      assertNull(stateless.checkVersion());
   }
   
   public void testSharedRepository() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Session30 stateless = (Session30)jndiContext.lookup("Shared");
      assertNull(stateless.checkVersion());
   }
   
   public void testUnharedRepository() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Session30 stateless = (Session30)jndiContext.lookup("Unshared");
      Throwable t = stateless.checkVersion();
      assertNotNull(t);
      assertTrue(t instanceof java.lang.NoSuchMethodException);
   }
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(ClassloaderUnitTestCase.class, "classloader.jar, classloader-shared.jar, classloader-unshared.jar");
   }


}
