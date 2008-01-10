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
package org.jboss.ejb3.test.ejbthree959;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

/**
 * A 2.1 stateful bean.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MyStatefulBean implements SessionBean
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger log = Logger.getLogger(MyStatefulBean.class);
   
   @SuppressWarnings("unused")
   private SessionContext ctx;
   
   private String name;
   
   /* plumbing */
   
   public void ejbActivate() throws EJBException, RemoteException
   {
      log.info("activate");
      
      StatusBean.activateCalls++;
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
      log.info("passivate");
      
      StatusBean.passivateCalls++;
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
      log.info("remove");
      
      StatusBean.removeCalls++;
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      log.info("set session context");
      
      this.ctx = ctx;
   }

   /* home interface */
   
   public void ejbCreate() throws CreateException, RemoteException
   {
      log.info("create");
      
      checkCtx();
      
      StatusBean.createCalls++;
   }
   
   public void ejbCreate(String name) throws CreateException, RemoteException
   {
      log.info("create '" + name + "'");
      
      setName(name);
      
      checkCtx();
      
      StatusBean.createCalls++;
   }
   
   /* actual stuff */
   
   private void assertTrue(String msg, boolean condition)
   {
      if(!condition)
         throw new RuntimeException(msg);
   }
   
   public void checkCtx()
   {
      assertTrue("ctx is not set", ctx != null);
   }
   
   public String sayHi()
   {
      return "Hi " + name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
}
