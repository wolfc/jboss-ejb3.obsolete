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
package org.jboss.ejb3.core.test.ejbthree1512.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Handle;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1512.MyStateless21;
import org.jboss.ejb3.core.test.ejbthree1512.MyStateless21Home;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class BaseRemoteViewTestCase extends AbstractEJB3TestCase
{
   protected static List<SessionContainer> containers = new ArrayList<SessionContainer>();
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      for(SessionContainer container : containers)
         undeployEjb(container);
      containers.clear();
      
      AbstractEJB3TestCase.afterClass();
   }
   
   protected abstract String getEjbName();
   
   protected MyStateless21 getRemoteView() throws Exception
   {
      MyStateless21Home home = lookup(getEjbName() + "/home", MyStateless21Home.class);
      MyStateless21 bean = home.create();
      return bean;
   }
   
   @Test
   public void testCreate() throws Exception
   {
      MyStateless21 bean = getRemoteView();
      assertNotNull(bean);
   }
   
   @Test
   public void testInvocation() throws Exception
   {
      MyStateless21 bean = getRemoteView();
      String now = new Date().toString();
      String actual = bean.sayHi(now);
      assertEquals("Hi " + now, actual);
   }
   
   @Test
   public void testGetHandle() throws Exception
   {
      MyStateless21 bean = getRemoteView();
      Handle handle = bean.getHandle();
      assertNotNull(handle);
   }
}
