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
package org.jboss.ejb3.core.test.service.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.service.ServiceBean;
import org.jboss.ejb3.core.test.service.ServiceRemote;
import org.jboss.ejb3.session.SessionContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ServiceContainerTestCase extends AbstractEJB3TestCase
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
      
      containers.add(deploySessionEjb(ServiceBean.class));
   }
   
   @Test
   public void test1() throws Exception
   {
      ServiceRemote bean = lookup("ServiceBean/remote", ServiceRemote.class);
      
      assertEquals(-1, bean.getId());
      
      bean.setId(1);
      
      assertEquals(1, bean.getId());
   }
}
