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
package org.jboss.ejb3.test.invalidtxmdb.unit;

import javax.jms.QueueConnectionFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.test.invalidtxmdb.TestStatus;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MDBUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(MDBUnitTestCase.class);

   public MDBUnitTestCase(String name)
   {
      super(name);
   }

   public void testQueue() throws Exception
   {
      try
      {
         TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
         fail("TestStatusBean should not have deployed");
      }
      catch (javax.naming.NameNotFoundException e)
      {
      }
   }
   
   protected QueueConnectionFactory getQueueConnectionFactory()
      throws Exception
   {
      try
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "ConnectionFactory");
      }
      catch (NamingException e)
      {
         return (QueueConnectionFactory) getInitialContext().lookup(
               "java:/ConnectionFactory");
      }
   }
   
   protected InitialContext getInitialContext() throws Exception
   {
      return InitialContextFactory.getInitialContext();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(MDBUnitTestCase.class, "invalidtxmdb-test.jar");
   }

}