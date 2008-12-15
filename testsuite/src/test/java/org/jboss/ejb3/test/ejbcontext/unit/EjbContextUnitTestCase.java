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
package org.jboss.ejb3.test.ejbcontext.unit;

import javax.ejb.EJBException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbcontext.Stateful;
import org.jboss.ejb3.test.ejbcontext.StatefulBean;
import org.jboss.ejb3.test.ejbcontext.StatefulLocalBusiness1;
import org.jboss.ejb3.test.ejbcontext.StatefulRemoteBusiness1;
import org.jboss.ejb3.test.ejbcontext.StatefulRemoteBusiness2;
import org.jboss.ejb3.test.ejbcontext.StatelessBusinessRemote;
import org.jboss.ejb3.test.ejbcontext.StatelessRemote;
import org.jboss.ejb3.test.ejbcontext.StatelessRemoteHome;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>StatelessBusinessRemote
 * @version $Revision$
 */
public class EjbContextUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(EjbContextUnitTestCase.class);

   public EjbContextUnitTestCase(String name)
   {
      super(name);
   }

   public void testEjbContextJndi() throws Exception
   {
      Stateful stateful = (Stateful) getInitialContext().lookup(StatefulBean.class.getSimpleName() + "/remote");
      stateful.testEjbContext();
   }

   public void testEjbContextLookup() throws Exception
   {
      StatelessBusinessRemote stateless = (StatelessBusinessRemote) getInitialContext().lookup(
            StatelessBusinessRemote.JNDI_NAME);
      stateless.testEjbContextLookup();
   }

   public void testSessionContext() throws Exception
   {
      StatelessBusinessRemote stateless = (StatelessBusinessRemote) getInitialContext().lookup(
            StatelessBusinessRemote.JNDI_NAME);
      stateless.testSessionContext();
   }

   public void testConcurrentInvokedBusinessInterface() throws Exception
   {
      for (int i = 0; i < 100; ++i)
      {
         Runnable r = new Runnable()
         {
            public void run()
            {
               try
               {
                  StatelessBusinessRemote stateless1 = (StatelessBusinessRemote) getInitialContext().lookup(
                        StatelessBusinessRemote.JNDI_NAME);
                  StatelessBusinessRemote stateless2 = (StatelessBusinessRemote) getInitialContext().lookup(
                        StatelessBusinessRemote.JNDI_NAME);

                  Class<?> interfc = stateless1.testInvokedBusinessInterface();
                  assertEquals(interfc, StatelessBusinessRemote.class);

                  interfc = stateless2.testInvokedBusinessInterface();
                  assertEquals(interfc, StatelessBusinessRemote.class);

                  StatelessBusinessRemote stateless = (StatelessBusinessRemote) stateless1
                        .testBusinessObject(StatelessBusinessRemote.class);
                  stateless.noop();

                  assertEquals(interfc, StatelessBusinessRemote.class);

                  try
                  {
                     stateless1.testBusinessObject(Stateful.class);
                     fail("IllegalStateException not thrown");
                  }
                  catch (javax.ejb.EJBException e)
                  {
                     if (!(e.getCause() instanceof IllegalStateException))
                        throw e;
                     assertEquals(IllegalStateException.class, e.getCause().getClass());
                  }

                  stateless1.testEjbObject();

                  stateless1.testEjbLocalObject();
               }
               catch (Exception e)
               {
                  e.printStackTrace();
                  fail("caught exception " + e);
               }
            }
         };

         new Thread(r).start();
      }

      Thread.sleep(5 * 1000);
   }

   public void testStatelessInvokedBusinessInterface() throws Exception
   {
      StatelessBusinessRemote stateless1 = (StatelessBusinessRemote) getInitialContext().lookup(
            StatelessBusinessRemote.JNDI_NAME);
      StatelessRemoteHome home = (StatelessRemoteHome) getInitialContext().lookup(StatelessRemoteHome.JNDI_NAME);
      StatelessRemote stateless2 = home.create();

      Class<?> interfc = stateless1.testInvokedBusinessInterface();
      assertEquals(interfc, StatelessBusinessRemote.class);

      try
      {
         interfc = stateless2.testInvokedBusinessInterface();
         fail("EJB 3.0 4.5.2 getInvokedBusinessInterface is illegal when bean is invoked through 2.1 view");
      }
      catch(EJBException e)
      {
         // good
      }

      StatelessBusinessRemote stateless = (StatelessBusinessRemote) stateless1
            .testBusinessObject(StatelessBusinessRemote.class);
      stateless.noop();

      assertEquals(interfc, StatelessBusinessRemote.class);

      try
      {
         stateless1.testBusinessObject(Stateful.class);
         fail("IllegalStateException not thrown");
      }
      catch (javax.ejb.EJBException e)
      {
         if (!(e.getCause() instanceof IllegalStateException))
            throw e;
         assertEquals(IllegalStateException.class, e.getCause().getClass());
      }

      stateless1.testEjbObject();

      stateless1.testEjbLocalObject();
   }

   public void testStatefulInvokedBusinessInterface() throws Exception
   {
      Stateful stateful1 = (Stateful) getInitialContext().lookup(
            StatefulBean.class.getSimpleName() + "/remote-" + Stateful.class.getName());
      StatefulRemoteBusiness2 stateful2 = (StatefulRemoteBusiness2) getInitialContext().lookup(
            StatefulBean.class.getSimpleName() + "/remote-" + StatefulRemoteBusiness2.class.getName());

      Class<?> interfc = stateful1.testInvokedBusinessInterface();
      assertEquals(interfc, Stateful.class);

      interfc = stateful2.testInvokedBusinessInterface2();
      assertEquals(interfc, StatefulRemoteBusiness2.class);

      interfc = stateful2.testLocalInvokedBusinessInterface();
      assertEquals(interfc, StatefulLocalBusiness1.class);

      stateful1.setState("same");
      Stateful stateful3 = (Stateful) stateful1.getBusinessObject();
      assertEquals("same", stateful3.getState());

   }

   public void testGetBusinessObjectNullStateful() throws Exception
   {
      Stateful stateful = (Stateful) getInitialContext().lookup(StatefulBean.class.getSimpleName() + "/remote");
      try
      {
         stateful.testBusinessObject(null);
         fail("Expected an EJBException");
      }
      catch (EJBException e)
      {
         assertNotNull(e.getCause());
         assertEquals(e.getCause().getClass(), IllegalStateException.class);
      }
   }

   public void testGetBusinessObjectNullStateless() throws Exception
   {
      StatelessBusinessRemote stateless = (StatelessBusinessRemote) getInitialContext().lookup(
            StatelessBusinessRemote.JNDI_NAME);
      try
      {
         stateless.testBusinessObject(null);
         fail("Expected an EJBException");
      }
      catch (EJBException e)
      {
         assertNotNull(e.getCause());
         assertEquals(e.getCause().getClass(), IllegalStateException.class);
      }
   }

   public void testLocalOnlyGetBusinessObject() throws Exception
   {
      StatefulRemoteBusiness1 sfsb = (StatefulRemoteBusiness1) getInitialContext().lookup(
            StatefulBean.class.getSimpleName() + "/remote-" + StatefulRemoteBusiness1.class.getName());
      Object o = sfsb.testLocalOnlyGetBusinessObject();
      assertNotNull(o);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EjbContextUnitTestCase.class, "ejbcontext.jar");
   }

}
