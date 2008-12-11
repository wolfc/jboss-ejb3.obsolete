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
package org.jboss.ejb3.test.initial;

import java.util.Map;

import javax.naming.InitialContext;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Tester implements TesterMBean
{
   public void testSLSBCollocation() throws Exception
   {
      InitialContext ctx = new InitialContext();
      TestLocal local = (TestLocal) ctx.lookup("initial-ejb3-test/TestBean/local");
      TestRemote remote = (TestRemote) ctx.lookup("initial-ejb3-test/TestBean/remote");

      if (local.getObject() != TestBean.obj) throw new RuntimeException("Local call not equal");
      if (local.getObject() == remote.getObject()) throw new RuntimeException("Remote should not be equal");
      Map map = remote.getObject();
      map.put("hello", "world");

      Object obj = local.echo(map);
      if (obj != map) throw new RuntimeException("argument and return should be the same");

      map = (Map)obj;
      if (!map.containsKey("hello")) throw new RuntimeException("not good return");

      obj = remote.echo(map);
      if (obj == map) throw new RuntimeException("argument and return should not be the same");

      map = (Map)obj;
      if (!map.containsKey("hello")) throw new RuntimeException("not good return");

   }

   public void testSFSBCollocation() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatefulTestLocal local = (StatefulTestLocal) ctx.lookup("initial-ejb3-test/StatefulTestBean/local");
      StatefulTestRemote remote = (StatefulTestRemote) ctx.lookup("initial-ejb3-test/StatefulTestBean/remote");

      if (local.getObject() != StatefulTestBean.obj) throw new RuntimeException("Local call not equal");
      if (local.getObject() == remote.getObject()) throw new RuntimeException("Remote should not be equal");
      Map map = remote.getObject();
      map.put("hello", "world");

      Object obj = local.echo(map);
      if (obj != map) throw new RuntimeException("argument and return should be the same");
      map = (Map)obj;
      if (!map.containsKey("hello")) throw new RuntimeException("not good return");

      obj = remote.echo(map);
      if (obj == map) throw new RuntimeException("argument and return should not be the same");

      map = (Map)obj;
      if (!map.containsKey("hello")) throw new RuntimeException("not good return");
   }


   public void test() throws Exception
   {
      InitialContext ctx = new InitialContext();
      Test test = (Test) ctx.lookup("initial-ejb3-test/TestBean/local");
      String echo = test.testMe("echo");
      if (!"echo".equals(echo)) throw new RuntimeException("ECHO FAILED!");
   }

   public void statefulTest() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatefulTestLocal test = (StatefulTestLocal) ctx.lookup("initial-ejb3-test/StatefulTestBean/local");
      test.setState("hello world");
      if (!test.getState().equals("hello world")) throw new Exception("state was not retained");
   }

   public void testInterceptors() throws Exception
   {
      InitialContext ctx = new InitialContext();
      InterceptedSLTest test = (InterceptedSLTest) ctx.lookup("initial-ejb3-test/InterceptedSLTestBean/local");
      int ret = test.testMethod(5);
      int expected = 3010;

      if (ret != expected) throw new Exception("return value was not " + expected + ", it was: " + ret);

      boolean exception = false;
      try
      {
         test.throwsThrowable(1);
      }
      catch (Throwable e)
      {
         exception = true;
      }
      if (!exception) throw new Exception("Exception was not thrown");
   }

   public void testCallbacks() throws Exception
   {
      //Check the correct callbacks get invoked
      InitialContext ctx = new InitialContext();
      TestStatus status = (TestStatus) ctx.lookup("initial-ejb3-test/TestStatusBean/remote");
      status.clear();
      InterceptedSLTest test = (InterceptedSLTest) ctx.lookup("initial-ejb3-test/InterceptedSLTestBean/local");
      test.testMethod(5);
      // PostConstruct test is invalid, because you can't assume that postConstruct is called during the lookup
      // the instance might already be available from a previous call or prefill of the pool.
      //if (!status.postConstruct()) throw new Exception("PostConstruct should be called for SLSB");
      if (status.prePassivate()) throw new Exception("PrePassivate should not be called for SLSB");
      if (status.postActivate()) throw new Exception("PostActivate should not be called for SLSB");

      //TODO: Figure out when destroy gets called
      //if (!status.preDestroy()) throw new Exception("PreDestroy should be called for SLSB");
   }
}



