/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.proxy.impl.ejbthree1868.unit;

import java.lang.reflect.Proxy;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.proxy.impl.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.ejb3.test.proxy.impl.ejbthree1868.Calculator;
import org.jboss.ejb3.test.proxy.impl.ejbthree1868.CalculatorImpl;
import org.jboss.ejb3.test.proxy.impl.ejbthree1868.IllegalInvocationException;
import org.jboss.ejb3.test.proxy.impl.ejbthree1868.NotToBeInvokedInterceptor;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * IsLocalProxyFactoryInterceptorTestCase
 *
 * Testcase for EJBTHREE-1868 which tests that the {@link IsLocalProxyFactoryInterceptor}
 * correctly detects local invocations. The bug that was identified in EJBTHREE-1868
 * showed that the {@link IsLocalProxyFactoryInterceptor} always considered the local
 * calls are remote (resulting in the call going through network stack). The bug was a
 * result of incorrect ordering of static fields in the {@link IsLocalProxyFactoryInterceptor}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class IsLocalProxyFactoryInterceptorTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(IsLocalProxyFactoryInterceptorTestCase.class);

   /**
    *
    */
   private static final String KEY = "SomeKey";

   /**
     * Test that the local calls are handled locally.
     * @throws Exception
     */
   @Test
   public void testIsLocal() throws Exception
   {

      // Too AOP oriented stuff, since the IsLocalProxyFactoryInterceptor is heavily
      // into AOP.
      // Had to copy over from
      // org.jboss.ejb3.proxy.impl.objectfactory.ProxyObjectFactory#getObjectInstance

      // Create a POJI Proxy to the Registrar

      // Intentionally add a dummy NotToBeInvokedInterceptor to check whether the IsLocalProxyFactoryInterceptor
      // passes the call to the next interceptor. If it's passed then it indicates that the call was (incorrectly) considered
      // remote
      Interceptor[] interceptors =
      {IsLocalProxyFactoryInterceptor.singleton, new NotToBeInvokedInterceptor()};
      PojiProxy handler = new PojiProxy(KEY, null, interceptors);
      Class<?>[] interfaces = new Class<?>[]
      {Calculator.class};
      // create the proxy
      Calculator calculator = (Calculator) Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
      // register with dispatcher too, since the IsLocalProxyFactoryInterceptor
      // looks for the key in the dispatcher
      Dispatcher.singleton.registerTarget(KEY, new CalculatorImpl());

      try
      {
         int result = calculator.add(1, 2);
         logger.info("Successfully invoked method. Result = " + result);
         Assert.assertEquals("Incorrect result from calculator", result, 3);
      }
      catch (IllegalInvocationException iie)
      {
         logger.error("Local call was considered as remote", iie);
         // If the call was treated as remote then the dummy NotToBeInvokedInterceptor
         // will be called and it would throw a IllegalInvocationException
         Assert
               .fail("A local invocation was treated as a remote invocation by " + IsLocalProxyFactoryInterceptor.class);
      }

   }

}
