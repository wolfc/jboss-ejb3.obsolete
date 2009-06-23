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
package org.jboss.ejb3.core.test.ejbthree1850.unit;

import static junit.framework.Assert.assertEquals;

import javax.naming.NamingException;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1850.SyncStateful;
import org.jboss.ejb3.core.test.ejbthree1850.SyncStatefulBean;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class TxSyncTestCase extends AbstractEJB3TestCase
{
   @Before
   public void before()
   {
      SyncStatefulBean.afterBegins = 0;
      SyncStatefulBean.afterCompletions = 0;
      SyncStatefulBean.beforeCompletions = 0;
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      deploySessionEjb(SyncStatefulBean.class);
   }
   
   @Test
   public void testRemove() throws NamingException
   {
      SyncStateful bean = lookup("SyncStatefulBean/local", SyncStateful.class);
      // if the remove sync runs before session sync, then SessionSync.afterCompletion won't have run
      bean.remove();
      
      assertEquals("afterBegin has not run", 1, SyncStatefulBean.afterBegins);
      assertEquals("beforeCompletion has not run", 1, SyncStatefulBean.beforeCompletions);
      assertEquals("afterCompletion has not run", 1, SyncStatefulBean.afterCompletions);
   }
}
