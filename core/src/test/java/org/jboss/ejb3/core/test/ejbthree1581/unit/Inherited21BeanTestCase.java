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
package org.jboss.ejb3.core.test.ejbthree1581.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1581.Bank21;
import org.jboss.ejb3.core.test.ejbthree1581.Bank21Home;
import org.jboss.ejb3.core.test.ejbthree1581.BankBean21;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class Inherited21BeanTestCase extends AbstractEJB3TestCase
{
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      deploySessionEjb(BankBean21.class);
   }
   
   @Test
   public void testCreation() throws Exception
   {
      Bank21Home home = lookup("BankBean21/localHome", Bank21Home.class);
      Bank21 bean = home.create();
      String actual = bean.getActivated();
      assertEquals("_CREATED", actual);
   }
   
   @Test
   public void testInjection() throws Exception
   {
      Bank21Home home = lookup("BankBean21/localHome", Bank21Home.class);
      Bank21 bean = home.create();
      boolean injected = bean.hasSessionContext();
      assertTrue(injected);
   }
}
