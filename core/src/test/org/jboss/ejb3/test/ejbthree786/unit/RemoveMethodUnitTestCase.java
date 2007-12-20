/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree786.unit;

import javax.ejb.EJBObject;
import javax.ejb.NoSuchEJBException;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree786.AbstractRemoveBean;
import org.jboss.ejb3.test.ejbthree786.Delegate;
import org.jboss.ejb3.test.ejbthree786.DelegateBean;
import org.jboss.ejb3.test.ejbthree786.Ejb21View;
import org.jboss.ejb3.test.ejbthree786.Ejb21ViewBean;
import org.jboss.ejb3.test.ejbthree786.Ejb21ViewHome;
import org.jboss.ejb3.test.ejbthree786.RemoveStatefulRemote;
import org.jboss.ejb3.test.ejbthree786.RemoveStatelessRemote;
import org.jboss.ejb3.test.ejbthree786.StatefulRemoveBean;
import org.jboss.ejb3.test.ejbthree786.StatelessRemoveBean;
import org.jboss.test.JBossTestCase;

/**
 * We want a remove action on a backing bean.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:arubinge@redhat.com">ALR</a>
 * @version $Revision: 65870 $
 */
public class RemoveMethodUnitTestCase extends JBossTestCase
{

   public RemoveMethodUnitTestCase(String name)
   {
      super(name);
   }

   public void testRemoveStatefulRemote() throws Exception
   {
      RemoveStatefulRemote session = (RemoveStatefulRemote) getInitialContext().lookup(
            StatefulRemoveBean.JNDI_NAME_REMOTE);
      String result = session.remove();
      assertEquals(AbstractRemoveBean.RETURN_STRING, result);
   }

   public void testRemoveStatelessRemote() throws Exception
   {
      RemoveStatelessRemote session = (RemoveStatelessRemote) getInitialContext().lookup(
            StatelessRemoveBean.JNDI_NAME_REMOTE);
      String result = session.remove();
      assertEquals(AbstractRemoveBean.RETURN_STRING, result);
   }

   public void testRemoveStatefulLocalViaDelegate() throws Exception
   {
      Delegate session = (Delegate) getInitialContext().lookup(DelegateBean.JNDI_NAME_REMOTE);
      String result = session.invokeStatefulRemove();
      assertEquals(AbstractRemoveBean.RETURN_STRING, result);
   }

   public void testRemoveStatelessLocalViaDelegate() throws Exception
   {
      Delegate session = (Delegate) getInitialContext().lookup(DelegateBean.JNDI_NAME_REMOTE);
      String result = session.invokeStatelessRemove();
      assertEquals(AbstractRemoveBean.RETURN_STRING, result);
   }

   public void testExplicitExtensionEjbObjectInProxy() throws Exception
   {
      // Obtain stub
      //Ejb21View session = (Ejb21View) getInitialContext().lookup(Ejb21ViewBean.JNDI_NAME_REMOTE);
      Object obj = getInitialContext().lookup("Ejb21ViewBean/home");
      Ejb21ViewHome home = (Ejb21ViewHome) PortableRemoteObject.narrow(obj, Ejb21ViewHome.class);
      Ejb21View session = home.create();

      // Ensure EJBObject 
      assertTrue(session instanceof EJBObject);

      // Cast and remove appropriately, ensuring removed
      boolean removed = false;
      String result = session.test();
      assertEquals(Ejb21ViewBean.TEST_STRING, result);
      session.remove();
      try
      {
         session.test();
      }
      catch (NoSuchEJBException nsee)
      {
         removed = true;
      }
      assertTrue(removed);

   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(RemoveMethodUnitTestCase.class, "ejbthree786.jar");
   }
}
