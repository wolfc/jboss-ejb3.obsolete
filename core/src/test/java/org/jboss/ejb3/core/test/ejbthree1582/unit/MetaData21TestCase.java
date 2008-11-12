/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.ejbthree1582.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBMetaData;
import javax.ejb.HomeHandle;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1582.Greeter;
import org.jboss.ejb3.core.test.ejbthree1582.GreeterBean;
import org.jboss.ejb3.core.test.ejbthree1582.GreeterHome;
import org.jboss.ejb3.core.test.ejbthree1582.ValueHolder;
import org.jboss.ejb3.core.test.ejbthree1582.ValueHolderBean;
import org.jboss.ejb3.core.test.ejbthree1582.ValueHolderHome;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MetaData21TestCase extends AbstractEJB3TestCase
{
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      deploySessionEjb(ValueHolderBean.class);
      deploySessionEjb(GreeterBean.class);
   }
   
   private void exercise(GreeterHome home) throws RemoteException, CreateException
   {
      Greeter bean = home.create();
      String name = new Date().toString();
      String actual = bean.sayHi(name);
      assertEquals("Hi " + name, actual);
   }
   
   private void exercise(ValueHolderHome home) throws RemoteException, CreateException
   {
      ValueHolder bean = home.create("test");
      String actual = bean.getValue();
      assertEquals("test", actual);
   }
   
   @Test
   public void testStatefulGetEJBMetaData() throws Exception
   {
      ValueHolderHome home = lookup("ValueHolderBean/home", ValueHolderHome.class);
      EJBMetaData metaData = home.getEJBMetaData();
      assertNotNull("metaData is null", metaData);
      assertTrue(metaData.isSession());
      assertFalse(metaData.isStatelessSession());
      Class<?> homeInterfaceClass = metaData.getHomeInterfaceClass();
      assertEquals(ValueHolderHome.class, homeInterfaceClass);
      Class<?> remoteInterfaceClass = metaData.getRemoteInterfaceClass();
      assertEquals(ValueHolder.class, remoteInterfaceClass);
      ValueHolderHome otherHome = (ValueHolderHome) metaData.getEJBHome();
      exercise(otherHome);
   }
   
   @Test
   public void testStatefulGetHomeHandle() throws Exception
   {
      ValueHolderHome home = lookup("ValueHolderBean/home", ValueHolderHome.class);
      HomeHandle handle = home.getHomeHandle();
      assertNotNull("handle is null", handle);
      ValueHolderHome otherHome = (ValueHolderHome) handle.getEJBHome();
      exercise(otherHome);
   }
   
   @Test
   public void testStatelessGetEJBMetaData() throws Exception
   {
      GreeterHome home = lookup("GreeterBean/home", GreeterHome.class);
      EJBMetaData metaData = home.getEJBMetaData();
      assertNotNull("metaData is null", metaData);
      assertTrue(metaData.isSession());
      assertFalse(metaData.isStatelessSession());
      Class<?> homeInterfaceClass = metaData.getHomeInterfaceClass();
      assertEquals(GreeterHome.class, homeInterfaceClass);
      Class<?> remoteInterfaceClass = metaData.getRemoteInterfaceClass();
      assertEquals(Greeter.class, remoteInterfaceClass);
      GreeterHome otherHome = (GreeterHome) metaData.getEJBHome();
      exercise(otherHome);
   }
   
   @Test
   public void testStatelessGetHomeHandle() throws Exception
   {
      GreeterHome home = lookup("GreeterBean/home", GreeterHome.class);
      HomeHandle handle = home.getHomeHandle();
      assertNotNull("handle is null", handle);
      GreeterHome otherHome = (GreeterHome) handle.getEJBHome();
      exercise(otherHome);
   }
   
   
}
