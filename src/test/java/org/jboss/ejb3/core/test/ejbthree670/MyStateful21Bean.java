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
package org.jboss.ejb3.core.test.ejbthree670;

import java.rmi.RemoteException;

import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.ejb.RemoteHome;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Stateful
@RemoteHome(MyStateful21Home.class)
public class MyStateful21Bean implements SessionBean
{
   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(MyStateful21Bean.class);
   
   private String name;
   public static int preDestroyCalls = 0;
   
   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
      log.info("remove");
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
   }

   @PreDestroy
   public void preDestroy()
   {
      //new Exception().printStackTrace();
      preDestroyCalls++;
      log.info("pre destroy");
      if(preDestroyCalls > 1)
         throw new IllegalStateException("pre destroy called multiple times");
   }
   
   public String sayHello()
   {
      return "Hi " + name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

}
