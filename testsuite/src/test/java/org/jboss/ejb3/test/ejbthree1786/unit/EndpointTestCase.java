/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1786.unit;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import javax.ejb.NoSuchEJBException;
import javax.naming.Context;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.ejb3.proxy.impl.handler.session.SessionProxyInvocationHandler;
import org.jboss.ejb3.test.ejbthree1786.EndpointAccessRemoteBusiness;
import org.jboss.ejb3.test.ejbthree1786.StatefulRemoteBusiness;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * EndpointTestCase
 * 
 * Test Cases to ensure that the Endpoint API is working as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EndpointTestCase extends JBossTestCase
{
   //------------------------------------------------------------------------||
   // Class Members ---------------------------------------------------------||
   //------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EndpointTestCase.class);

   //------------------------------------------------------------------------||
   // Instance Members ------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * The EJB used to hook into the endpoint
    */
   private EndpointAccessRemoteBusiness endpointAccessBean;

   //------------------------------------------------------------------------||
   // Constructor -----------------------------------------------------------||
   //------------------------------------------------------------------------||

   public EndpointTestCase()
   {
      super(EndpointTestCase.class.getName());
   }

   //------------------------------------------------------------------------||
   // Suite -----------------------------------------------------------------||
   //------------------------------------------------------------------------||

   public static Test suite() throws Exception
   {
      return getDeploySetup(EndpointTestCase.class, "ejbthree1786.jar");
   }

   //------------------------------------------------------------------------||
   // Tests -----------------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Tests that we may create/destroy a session
    */

   public void testRemoteSfsb() throws Throwable
   {
      // Log
      log.info("testRemoteSfsb");

      // Create a new Session
      final StatefulRemoteBusiness sfsb = this.getSfsb();
      log.info("Got SFSB: " + sfsb);

      // Test state
      int i = 0;
      int counter = sfsb.getCounter();
      TestCase.assertEquals(i++, counter);
      log.info("First counter check as expected");

      // Invoke to increment the counter, and test
      sfsb.incrementCounter();
      counter = sfsb.getCounter();
      TestCase.assertEquals(i++, counter);
      log.info("Increment, then counter check as expected");

      /*
       *  Try a backdoor removal through the endpoint
       */

      // Get the endpoint access EJB
      final EndpointAccessRemoteBusiness endpointAccess = this.getEndpointAccessBean();
      log.info("Got endpoint access bean: " + endpointAccess);

      // Get the ID of the SFSB
      final SessionProxyInvocationHandler handler = (SessionProxyInvocationHandler) Proxy.getInvocationHandler(sfsb);
      final Serializable id = (Serializable) handler.getTarget();

      // Remove
      endpointAccess.destroySession(id);

      // Ensure Removed
      boolean removed = false;
      try
      {
         sfsb.getCounter();
      }
      catch (final NoSuchEJBException nsee)
      {
         removed = true;
      }
      TestCase.assertTrue("The test SFSB was not removed", removed);

   }

   //------------------------------------------------------------------------||
   // Internal Helper Methods -----------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Obtains the EJB via which we'll access the endpoint
    */
   private EndpointAccessRemoteBusiness getEndpointAccessBean() throws Throwable
   {
      // If we haven't yet getten the bean
      if (endpointAccessBean == null)
      {
         // Look it up and set
         final Context context = this.getInitialContext();
         endpointAccessBean = (EndpointAccessRemoteBusiness) context.lookup(EndpointAccessRemoteBusiness.JNDI_NAME);
      }

      // Return
      return endpointAccessBean;
   }

   /**
    * Creates a new test SFSB session and returns the reference
    * @return
    * @throws Throwable
    */
   private StatefulRemoteBusiness getSfsb() throws Throwable
   {
      // Look up and return
      final Context context = this.getInitialContext();
      return (StatefulRemoteBusiness) context.lookup(StatefulRemoteBusiness.JNDI_NAME);
   }
}
