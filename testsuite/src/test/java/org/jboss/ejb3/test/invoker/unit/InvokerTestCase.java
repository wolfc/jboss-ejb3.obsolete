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
package org.jboss.ejb3.test.invoker.unit;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.test.invoker.StatelessRemote;
import org.jboss.logging.Logger;
import org.jboss.remoting.transport.http.WebServerError;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public class InvokerTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(InvokerTestCase.class);

   public InvokerTestCase(String name)
   {
      super(name);
   }

   public void testHttp() throws Exception
   {
      Properties props = new Properties();
      props.put("java.naming.factory.initial", "org.jboss.naming.HttpNamingContextFactory");
      props.put("java.naming.provider.url", "http://localhost:8080/invoker/JNDIFactory");
      props.put("java.naming.factory.url.pkgs", "org.jboss.naming");
      InitialContext jndiContext = new InitialContext(props);
      //InitialContext jndiContext = new InitialContext();

      StatelessRemote stateless = null;
      try
      {
         stateless = (StatelessRemote) jndiContext.lookup("StatelessHttp");
      }
      catch (NamingException ne)
      {
         Throwable namingCause = ne.getCause();
         if (namingCause != null && namingCause instanceof UndeclaredThrowableException)
         {
            Throwable undeclaredCause = namingCause.getCause();
            if (undeclaredCause != null && undeclaredCause instanceof WebServerError)
            {
               String error = "Test failed due to problem w/ remoting transport";
               log.error(error + " :" + undeclaredCause);
               TestCase.fail(error);
            }
         }
      }
      assertNotNull(stateless);

      try
      {
         assertEquals("echo", stateless.echo("echo"));
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(InvokerTestCase.class, "invoker-test.jar");
   }
}