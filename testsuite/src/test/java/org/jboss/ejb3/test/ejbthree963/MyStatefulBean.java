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
package org.jboss.ejb3.test.ejbthree963;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.RemoteHome;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteHomeBinding;

/**
 * @author carlo
 *
 */
@Stateful
@RemoteHome(MyStatefulHome.class)
@RemoteHomeBinding(jndiBinding = MyStatefulHome.JNDI_NAME)
public class MyStatefulBean implements SessionBean
{
   private static final long serialVersionUID = 1L;

   private SessionContext ctx;

   private String name;

   public String sayHi() throws RemoteException
   {
      if (ctx == null)
         throw new IllegalStateException("ctx is null");
      return "Hi " + name;
   }

   public void setName(String name) throws RemoteException
   {
      this.name = name;
   }

   // this one is optional
   //   public void ejbCreate() //throws EJBException, RemoteException
   //   {
   //   }

   public void ejbActivate() //throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() //throws EJBException, RemoteException
   {
   }

   public void ejbRemove() //throws EJBException, RemoteException
   {
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      this.ctx = ctx;
   }

}
