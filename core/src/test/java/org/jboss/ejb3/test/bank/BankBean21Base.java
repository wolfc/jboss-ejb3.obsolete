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
package org.jboss.ejb3.test.bank;

import java.rmi.RemoteException;

import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

/**
 * @author $Author: alex@jboss.com $
 * @version $Revision: 61136 $
 */
public class BankBean21Base implements javax.ejb.SessionBean
{
   protected static final Logger log = Logger.getLogger(BankBean21Base.class);
   protected String initialized = "";
   private String activated = "";
   protected SessionContext ctx;

   public BankBean21Base()
   {
      super();
   }
   
   public String isInitialized()
   {
      return initialized;
   }

   public String isActivated()
   {
      return activated;
   }

   public boolean hasSessionContext() throws RemoteException
   {
      return ctx != null;
   }

   public void ejbCreate()
   {
      activated += "_CREATED";
   }

   public void ejbActivate()
   {
      activated += "_ACTIVATED";
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext context)
   {
      this.ctx = context;
   }
}