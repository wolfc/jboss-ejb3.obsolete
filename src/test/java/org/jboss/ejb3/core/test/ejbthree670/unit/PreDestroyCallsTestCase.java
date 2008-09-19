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
package org.jboss.ejb3.core.test.ejbthree670.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.NoSuchEJBException;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree670.MyStateful;
import org.jboss.ejb3.core.test.ejbthree670.MyStateful21;
import org.jboss.ejb3.core.test.ejbthree670.MyStateful21Bean;
import org.jboss.ejb3.core.test.ejbthree670.MyStateful21Home;
import org.jboss.ejb3.core.test.ejbthree670.MyStatefulBean;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PreDestroyCallsTestCase extends AbstractEJB3TestCase
{
   private static List<SessionContainer> containers = new ArrayList<SessionContainer>();
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      for(SessionContainer container : containers)
         undeployEjb(container);
      containers.clear();
      
      AbstractEJB3TestCase.afterClass();
   }
   
   @Before
   public void before()
   {
      MyStateful21Bean.preDestroyCalls = 0;
      MyStatefulBean.preDestroyCalls = 0;
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      containers.add(deploySessionEjb(MyStatefulBean.class));
      containers.add(deploySessionEjb(MyStateful21Bean.class));
   }
   
   private MyStateful runMyStatefulTest() throws NamingException
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
      return session;
   }
   
   @Test
   public void test1() throws Exception
   {
      MyStateful session = runMyStatefulTest();
      try
      {
         session.remove();
         fail("Should have thrown NoSuchEJBException");
      }
      catch(NoSuchEJBException e)
      {
         // okay
      }
      assertEquals("number of PreDestroy calls", 1, MyStatefulBean.preDestroyCalls);
   }
   
   @Test
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
      assertEquals("number of PreDestroy calls", 1, MyStateful21Bean.preDestroyCalls);
   }
   
   @Test
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
      assertEquals("number of PreDestroy calls", 1, MyStateful21Bean.preDestroyCalls);
   }
   
   @Test
   public void testWithInTransaction() throws Exception
   {
      MyStateful session;
      TransactionManager tm = lookup("java:/TransactionManager", TransactionManager.class);
      tm.begin();
      try
      {
         session = runMyStatefulTest();
      }
      finally
      {
         tm.rollback();
      }
      try
      {
         session.remove();
         fail("Should have thrown NoSuchEJBException");
      }
      catch(NoSuchEJBException e)
      {
         // okay
      }
      assertEquals("number of PreDestroy calls", 1, MyStatefulBean.preDestroyCalls);
   }
}
