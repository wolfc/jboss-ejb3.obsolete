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
package org.jboss.ejb3.core.test.ejbthree1487.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1487.Greeter21;
import org.jboss.ejb3.core.test.ejbthree1487.Greeter21Bean;
import org.jboss.ejb3.core.test.ejbthree1487.Greeter21Home;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Make sure that an EJBContext gets properly injected into a 21 bean.
 * 
 * EJBTHREE-1487 delegates the setting up of ENC to aspects, which
 * must be available on 21 remote view invocations.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InjectEJBContext21TestCase extends AbstractEJB3TestCase
{
   private static List<SessionContainer> containers = new ArrayList<SessionContainer>();
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      for(SessionContainer container : containers)
         undeployEjb(container);
      containers.clear();
      
      AbstractEJB3TestCase.afterClass();
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      containers.add(deploySessionEjb(Greeter21Bean.class));
   }
   
   @Test
   public void testCreate() throws NamingException
   {
      Greeter21Home home = lookup("Greeter21Bean/home", Greeter21Home.class);
      Greeter21 bean = home.create();
      String me = new Date().toString();
      String actual = bean.sayHi(me);
      assertEquals("Hi " + me, actual);
   }
}
