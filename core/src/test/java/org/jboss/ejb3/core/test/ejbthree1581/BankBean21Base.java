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
package org.jboss.ejb3.core.test.ejbthree1581;

import java.rmi.RemoteException;

import javax.annotation.Resource;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.Init;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * Because Ejb3DescriptorHandler isn't exercised all methods are annotated.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BankBean21Base implements SessionBean
{
   private static final long serialVersionUID = 1L;
   
   /** read-only */
   protected SessionContext ctx;
   
   private String activated = "";
   
   @PostActivate
   public void ejbActivate() throws RemoteException
   {
      activated += "_ACTIVATED";
   }

   @Init
   public void ejbCreate() throws CreateException, RemoteException
   {
      activated += "_CREATED";
   }
   
   @PrePassivate
   public void ejbPassivate() throws RemoteException
   {
   }

   @Remove
   public void ejbRemove() throws RemoteException
   {
   }

   public String getActivated()
   {
      return activated;
   }
   
   public boolean hasSessionContext()
   {
      return ctx != null;
   }
   
   // allow write ctx
   @Resource
   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      assert ctx != null : "ctx is null";
      this.ctx = ctx;
   }
}
