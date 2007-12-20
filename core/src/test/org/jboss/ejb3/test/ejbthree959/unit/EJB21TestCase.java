/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree959.unit;

import javax.ejb.NoSuchEJBException;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree959.MyStateful;
import org.jboss.ejb3.test.ejbthree959.MyStatefulHome;
import org.jboss.ejb3.test.ejbthree959.Status;
import org.jboss.test.JBossTestCase;


/**
 * Test if an EJB 2.1 bean is properly deployed.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EJB21TestCase extends JBossTestCase
{

   public EJB21TestCase(String name)
   {
      super(name);
   }

   private MyStatefulHome getMyStatefulHome() throws Exception
   {
      return (MyStatefulHome) PortableRemoteObject.narrow(getInitialContext().lookup("MyStateful/home"), MyStatefulHome.class);
   }
   
   private Status getStatus() throws Exception
   {
      return (Status) getInitialContext().lookup("StatusBean/remote");
   }
   
   public void testCreateNoArgs() throws Exception
   {
      MyStatefulHome home = getMyStatefulHome();
      MyStateful bean = home.create();
      bean.setName("testCreateNoArgs");
      String expected = "Hi testCreateNoArgs";
      String actual = bean.sayHi();
      assertEquals(expected, actual);
      bean.remove();
   }
   
   public void testCreateWithArgs() throws Exception
   {
      MyStatefulHome home = getMyStatefulHome();
      MyStateful bean = home.create("testCreateWithArgs");
      String expected = "Hi testCreateWithArgs";
      String actual = bean.sayHi();
      assertEquals(expected, actual);
      bean.remove();
   }
   
   public void testCtx() throws Exception
   {
      MyStatefulHome home = getMyStatefulHome();
      MyStateful bean = home.create();
      bean.checkCtx();
      bean.remove();
   }
   
   public void testLifeCycle() throws Exception
   {
      Status status = getStatus();
      status.reset();
      
      MyStatefulHome home = getMyStatefulHome();
      MyStateful bean = home.create();
      
      assertEquals(1, status.getCreateCalls());
      
      bean.setName("testLifeCycle");
      String expected = "Hi testLifeCycle";
      String actual = bean.sayHi();
      assertEquals(expected, actual);
      
      sleep(10000);
      
      assertEquals(1, status.getPassivateCalls());
      
      actual = bean.sayHi();
      assertEquals(expected, actual);
      
      assertEquals(1, status.getActivateCalls());
      
      bean.remove();
      
      assertEquals(1, status.getRemoveCalls());
      
      try
      {
         bean.sayHi();
         fail("expected no such ejb exception");
      }
      catch(NoSuchEJBException e)
      {
         // good
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EJB21TestCase.class, "ejbthree959.jar");
   }

}
