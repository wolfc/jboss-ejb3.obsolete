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

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.ejbthree1512.HybridStatelessBean;
import org.jboss.ejb3.core.test.ejbthree1512.MyStateless21;
import org.jboss.ejb3.core.test.ejbthree1512.MyStatelessRemote;
import org.junit.BeforeClass;

/**
 * Use the backdoor to get the EJB 2.1 remote view.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BackdoorTestCase extends BaseRemoteViewTestCase
{
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      AbstractEJB3TestCase.beforeClass();
      
      containers.add(deploySessionEjb(HybridStatelessBean.class));
   }
   
   @Override
   protected String getEjbName()
   {
      return "HybridStatelessBean";
   }
   
   @Override
   protected MyStateless21 getRemoteView() throws Exception
   {
      return lookup(getEjbName() + "/remote", MyStatelessRemote.class).getEJBObject();
   }
}
