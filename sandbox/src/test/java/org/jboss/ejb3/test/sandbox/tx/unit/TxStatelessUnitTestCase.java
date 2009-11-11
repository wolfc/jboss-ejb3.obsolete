/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.sandbox.tx.unit;

import java.net.URL;

import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.ejb3.interceptors.container.BeanContext;
import org.jboss.ejb3.interceptors.direct.DirectContainer;
import org.jboss.ejb3.sandbox.interceptorcontainer.InterceptorContainer;
import org.jboss.ejb3.test.sandbox.tx.TxStatelessBean;
import org.jboss.ejb3.test.sandbox.tx.TxStatelessLocal;
import org.jboss.logging.Logger;
import org.jnp.server.SingletonNamingServer;
import org.objectweb.jotm.Jotm;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class TxStatelessUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(TxStatelessUnitTestCase.class);
   
   public void test1() throws Throwable
   {
//      AspectManager.verbose = true;
      
      SingletonNamingServer namingServer = new SingletonNamingServer();
      
      // Bootstrap AOP
      URL url = Thread.currentThread().getContextClassLoader().getResource("stateless/jboss-aop.xml");
      log.info("deploying AOP from " + url);
      AspectXmlLoader.deployXML(url);

      InitialContext ctx = new InitialContext();
      
      //TransactionManagerService service = new TransactionManagerService();
      //System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      Jotm jotm = new Jotm(true, false);
      ctx.rebind("java:/TransactionManager", jotm.getTransactionManager());
      System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      
      DirectContainer<InterceptorContainer> interceptorContainerContainer = new DirectContainer<InterceptorContainer>("FIXME", "InterceptorContainer", InterceptorContainer.class);
      Object args[] = { TxStatelessBean.class };
      Class<?> parameterTypes[] = { Class.class };
      BeanContext<InterceptorContainer> interceptorContainer = interceptorContainerContainer.construct(args, parameterTypes);
      
      TxStatelessLocal bean = (TxStatelessLocal) ctx.lookup("TxStatelessBean/local");
      assertNotNull(bean);
      
      TransactionManager tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
      
      tm.begin();
      try
      {
         String result = bean.sayHi("Test");
         assertEquals("Hi Test", result);
         tm.commit();
      }
      catch(Throwable t)
      {
         tm.rollback();
         throw t;
      }
      
      namingServer.destroy();
   }
}
