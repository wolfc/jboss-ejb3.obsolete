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

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class BasicTestRunnerWrapper extends TestCase
{
   private static final Logger log = Logger.getLogger(BasicTestRunnerWrapper.class);

   private static BasicTestSetup setup;
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      if (setup == null)
      {
         //We only want to set the classloader and deploy the aop stuff once
         setup = new BasicTestSetup();
         setup.setUp();
      }
   }

   public void test2() throws Exception
   {
      Method m = setup.getRunner().getClass().getMethod("test2");
      m.invoke(setup.getRunner());
   }
   
   public void testInstances() throws Exception
   {
      Method m = setup.getRunner().getClass().getMethod("testInstances");
      m.invoke(setup.getRunner());
   }

}
