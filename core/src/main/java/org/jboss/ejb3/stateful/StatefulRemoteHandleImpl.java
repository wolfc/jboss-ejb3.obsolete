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
package org.jboss.ejb3.stateful;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.jboss.logging.Logger;

/**
 * An EJB stateful session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="bdecoste@jboss.com">William DeCoste</a>
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a> 
 * @version $Revision$
 */
public class StatefulRemoteHandleImpl implements Handle
{
   private static final Logger log = Logger.getLogger(StatefulRemoteHandleImpl.class);

   /** Serial Version Identifier. */
   static final long serialVersionUID = -6324520755180597156L;

   // Instance Members

   private EJBObject proxy;

   // Constructor

   public StatefulRemoteHandleImpl(EJBObject proxy)
   {
      this.proxy = proxy;
   }

   // Required Implementations

   /**
    * Handle implementation.
    *
    * Returns the proxy  
    *
    * @throws RemoteException 
    */
   public EJBObject getEJBObject() throws RemoteException
   {
      return this.proxy;
   }
}
