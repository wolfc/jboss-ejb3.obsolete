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
package org.jboss.ejb3.test.asynchronous;

import java.util.Collection;
import java.util.concurrent.Future;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.RollbackException;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.common.proxy.plugins.async.AsyncProvider;
import org.jboss.ejb3.common.proxy.plugins.async.AsyncUtils;
import org.jboss.tm.TransactionManagerLocator;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class Tester implements TesterMBean
{
   public void testSLLocalAsynchronous() throws Exception
   {
      Context ctx = new InitialContext();
      StatelessLocal tester =
            (StatelessLocal) ctx.lookup("StatelessBean/local");
      int ret = tester.method(111);
      if (ret != 111) throw new RuntimeException("Wrong return for stateless local "+ ret);

      StatelessLocal asynchTester = AsyncUtils.mixinAsync(tester);
      ret = asynchTester.method(112);
      if (ret != 0) throw new RuntimeException("Wrong return value for stateless local "+ ret);
      AsyncProvider ap = (AsyncProvider) asynchTester;
      Future<?> future = ap.getFutureResult();
      ret = (Integer) future.get();
      if (ret != 112) throw new RuntimeException("Wrong async return value for stateless local "+ ret);
   }


   public void testSFLocalAsynchronous() throws Exception
   {
      Context ctx = new InitialContext();
      StatefulLocal tester =
            (StatefulLocal) ctx.lookup("StatefulBean/local");
      int ret = tester.method(121);
      if (ret != 121) throw new RuntimeException("Wrong return for stateful local "+ ret);

      StatefulLocal asynchTester = AsyncUtils.mixinAsync(tester);
      ret = asynchTester.method(122);
      if (ret != 0) throw new RuntimeException("Wrong return value for stateful local "+ ret);
      AsyncProvider ap = (AsyncProvider) asynchTester;
      Future<?> future = ap.getFutureResult();
      ret = (Integer) future.get();
      if (ret != 122) throw new RuntimeException("Wrong async return value for stateful local "+ ret);
   }

   public void testServiceLocalAsynchronous() throws Exception
   {
      Context ctx = new InitialContext();
      ServiceLocal tester =
            (ServiceLocal) ctx.lookup("ServiceBean/local");
      int ret = tester.method(131);
      if (ret != 131) throw new RuntimeException("Wrong return for service local "+ ret);

      ServiceLocal asynchTester = AsyncUtils.mixinAsync(tester);
      ret = asynchTester.method(132);
      if (ret != 0) throw new RuntimeException("Wrong return value for service local "+ ret);
      AsyncProvider ap = (AsyncProvider) asynchTester;
      Future<?> future = ap.getFutureResult();
      ret = (Integer) future.get();
      if (ret != 132) throw new RuntimeException("Wrong async return value for service local "+ ret);
   }

   private void waitForFuture(Future<?> future) throws InterruptedException
   {
      while (!future.isDone())
      {
         Thread.sleep(100);
      }
   }

}
