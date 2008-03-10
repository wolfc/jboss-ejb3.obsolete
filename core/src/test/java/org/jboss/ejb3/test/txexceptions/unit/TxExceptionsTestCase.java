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
package org.jboss.ejb3.test.txexceptions.unit;

import javax.ejb.EJBException;
import org.jboss.ejb3.test.txexceptions.AnnotatedAppException;
import org.jboss.ejb3.test.txexceptions.DeploymentDescriptorAppException;
import org.jboss.ejb3.test.txexceptions.AppException;
import org.jboss.ejb3.test.txexceptions.CheckedRollbackException;
import org.jboss.ejb3.test.txexceptions.DeploymentDescriptorCheckedRollbackException;
import org.jboss.ejb3.test.txexceptions.Dao;
import org.jboss.ejb3.test.txexceptions.NoRollbackRemoteException;
import org.jboss.ejb3.test.txexceptions.NoRollbackRuntimeException;
import org.jboss.ejb3.test.txexceptions.RollbackError;
import org.jboss.ejb3.test.txexceptions.RollbackRemoteException;
import org.jboss.ejb3.test.txexceptions.RollbackRuntimeException;
import org.jboss.ejb3.test.txexceptions.SimpleEntity;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public class TxExceptionsTestCase extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   public TxExceptionsTestCase(String name)
   {
      super(name);
   }

   public void testRequiresNewWithLookedUpEntityManager() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");
      dao.testRequiresNewWithLookedUpEntityManager();
   }
   public void testAnnotatedAppException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      boolean exceptionThrown = false;
      try
      {
         dao.createThrowAnnotatedAppException(1);
      }
      catch (AnnotatedAppException e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);
      assertNotNull(dao.get(1));
      dao.remove(1);
   }
   
   public void testDeploymentDescriptorAppException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      boolean exceptionThrown = false;
      try
      {
         dao.createThrowDeploymentDescriptorAppException(1);
      }
      catch (DeploymentDescriptorAppException e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);
      assertNotNull(dao.get(1));
      dao.remove(1);
   }

   public void testAppException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      boolean exceptionThrown = false;
      try
      {
         dao.createThrowAppException(1);
      }
      catch (AppException e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);
      assertNotNull(dao.get(1));
      dao.remove(1);
   }

   public void testNoRollbackRemoteException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      boolean exceptionThrown = false;
      try
      {
         dao.createThrowNoRollbackRemoteException(1);
      }
      catch (NoRollbackRemoteException e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);
      assertNotNull(dao.get(1));
      dao.remove(1);
   }

   public void testNoRollbackRuntimexception() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      boolean exceptionThrown = false;
      try
      {
         dao.createThrowNoRollbackRuntimeException(1);
      }
      catch (NoRollbackRuntimeException e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);
      assertNotNull(dao.get(1));
      dao.remove(1);
   }

   public void testDeploymentDescriptorCheckedRollbackException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      try
      {
         dao.createThrowDeploymentDescriptorCheckedRollbackException(1);
         fail();
      }
      catch (DeploymentDescriptorCheckedRollbackException e)
      {
      }

      SimpleEntity entity = dao.get(1);
      
      if (entity != null)
         dao.remove(1);
      
      assertNull(entity);
   }
   
   public void testCheckedRollbackException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      try
      {
         dao.createThrowCheckedRollbackException(1);
         fail();
      }
      catch (CheckedRollbackException e)
      {
      }

      SimpleEntity entity = dao.get(1);
      
      if (entity != null)
         dao.remove(1);
      
      assertNull(entity);
   }

   public void testRollbackRemoteException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      try
      {
         dao.createThrowRollbackRemoteException(1);
         fail();
      }
      catch (RollbackRemoteException e)
      {
      }
  
      SimpleEntity entity = dao.get(1);
      
      if (entity != null)
         dao.remove(1);
      
      assertNull(entity);
   }

   public void testRollbackRuntimeException() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      try
      {
         dao.createThrowRollbackRuntimeException(1);
         fail();
      }
      catch (EJBException e)
      {
         assertTrue(e.getCausedByException() instanceof RollbackRuntimeException);
      }
     
      SimpleEntity entity = dao.get(1);
      
      if (entity != null)
         dao.remove(1);
      
      assertNull(entity);
   }

   public void testRollbackError() throws Exception
   {
      Dao dao = (Dao) getInitialContext().lookup("DaoBean/remote");

      try
      {
         dao.createThrowRollbackError(1);
         fail();
      }
      catch (EJBException e)
      {
         // AFAIK, the spec doesn't define how the causing error should be delivered
         // so, this is based on the current impl
         assertTrue(e.getCausedByException() instanceof RuntimeException);
         assertTrue(((RuntimeException)e.getCausedByException()).getCause() instanceof RollbackError);
      }
     
      SimpleEntity entity = dao.get(1);
      
      if (entity != null)
         dao.remove(1);
      
      assertNull(entity);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(TxExceptionsTestCase.class, "txexceptions-test.jar");
   }
}