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
package org.jboss.ejb3.test.regression.ejbthree670.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.regression.ejbthree670.MyStateful;
import org.jboss.ejb3.test.regression.ejbthree670.MyStateful21;
import org.jboss.ejb3.test.regression.ejbthree670.MyStateful21Home;
import org.jboss.test.JBossTestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PreDestroyCallsTestCase extends JBossTestCase
{

   public PreDestroyCallsTestCase(String name)
   {
      super(name);
   }

   public void test1() throws Exception
   {
      MyStateful session = (MyStateful) getInitialContext().lookup("MyStatefulBean/remote");
      session.setName("Test");
      String actual = session.sayHello();
      assertEquals("Hi Test", actual);
      try
      {
         session.remove();
      }
      catch(RuntimeException e)
      {
         if(e.getCause().getMessage().equals("pre destroy called multiple times"))
            fail("pre destroy called multiple times");
         throw e;
      }
   }
   
   public void test21() throws Exception
   {
      MyStateful21Home home = (MyStateful21Home) getInitialContext().lookup("MyStateful21Bean/home");
      MyStateful21 session = home.create();
      session.setName("Test");
      String actual = session.sayHello();
      assertEquals("Hi Test", actual);
      try
      {
         session.remove();
      }
      catch(RuntimeException e)
      {
         if(e.getCause().getMessage().equals("pre destroy called multiple times"))
            fail("pre destroy called multiple times");
         throw e;
      }
   }
   
   public void testRemoveByHandle() throws Exception
   {
      MyStateful21Home home = (MyStateful21Home) getInitialContext().lookup("MyStateful21Bean/home");
      MyStateful21 session = home.create();
      session.setName("Test");
      String actual = session.sayHello();
      assertEquals("Hi Test", actual);
      try
      {
         home.remove(session.getHandle());
      }
      catch(RuntimeException e)
      {
         if(e.getCause().getMessage().equals("pre destroy called multiple times"))
            fail("pre destroy called multiple times");
         throw e;
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(PreDestroyCallsTestCase.class, "ejbthree670.jar");
   }
}
