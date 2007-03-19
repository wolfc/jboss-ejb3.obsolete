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

import org.jboss.ejb3.test.ejbcontext.Stateful;
import org.jboss.ejb3.test.ejbcontext.StatefulRemote;
import org.jboss.ejb3.test.ejbcontext.Stateless;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public class EjbContextUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(EjbContextUnitTestCase.class);

   public EjbContextUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testEjbContextJndi() throws Exception
   {
     Stateful stateful = (Stateful)getInitialContext().lookup("Stateful");
     stateful.testEjbContext();
   }
   
   public void testEjbContextLookup() throws Exception
   {
     Stateless stateless = (Stateless)getInitialContext().lookup("Stateless");
     stateless.testEjbContextLookup();
   }
   
   public void testStatelessInvokedBusinessInterface() throws Exception
   {
      Stateless stateless1 = (Stateless)getInitialContext().lookup("Stateless");
      Stateless stateless2 = (Stateless)getInitialContext().lookup("Stateless");
      
      Class interfc = stateless1.testInvokedBusinessInterface();
      assertEquals(interfc, Stateless.class);
      
      interfc = stateless2.testInvokedBusinessInterface();
      assertEquals(interfc, Stateless.class);
      
      Stateless stateless = (Stateless)stateless1.testBusinessObject(Stateless.class);
      stateless.noop();

      assertEquals(interfc, Stateless.class);
      
      try{
         stateless1.testBusinessObject(Stateful.class);
         fail("IllegalStateException not thrown");
      }
      catch (javax.ejb.EJBException e)
      {
         if (!(e.getCause() instanceof IllegalStateException)) throw e;
         assertEquals(IllegalStateException.class, e.getCause().getClass());
      }
      
      stateless1.testEjbObject();
      
      stateless1.testEjbLocalObject();
   }
   
   public void testStatefulInvokedBusinessInterface() throws Exception
   {
      Stateful stateful1 = (Stateful)getInitialContext().lookup("Stateful");
      StatefulRemote stateful2 = (StatefulRemote)getInitialContext().lookup("StatefulRemote");
      
      Class interfc = stateful1.testInvokedBusinessInterface();
      assertEquals(interfc, Stateful.class);
      
      interfc = stateful2.testInvokedBusinessInterface2();
      assertEquals(interfc, StatefulRemote.class);
      
      interfc = stateful1.testLocalInvokedBusinessInterface();
      assertEquals(interfc, StatefulRemote.class);
      
      stateful1.setState("same");
      Stateful stateful3 = (Stateful)stateful1.getBusinessObject();
      assertEquals("same", stateful3.getState());

      
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EjbContextUnitTestCase.class, "ejbcontext.jar");
   }

}
