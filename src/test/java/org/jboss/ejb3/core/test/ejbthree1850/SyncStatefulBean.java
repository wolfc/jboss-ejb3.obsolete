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
package org.jboss.ejb3.core.test.ejbthree1850;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Stateful
public class SyncStatefulBean implements SyncStateful, SessionSynchronization
{
   private static final Logger log = Logger.getLogger(SyncStatefulBean.class);
   
   // for unit tests only
   public static int afterBegins = 0;
   public static int afterCompletions = 0;
   public static int beforeCompletions = 0;
   
   private Serializable state;
   
   public void afterBegin() throws EJBException, RemoteException
   {
      log.info("afterBegin");
      afterBegins++;
   }

   public void afterCompletion(boolean committed) throws EJBException, RemoteException
   {
      log.info("afterCompletion " + committed);
      afterCompletions++;
   }

   public void beforeCompletion() throws EJBException, RemoteException
   {
      log.info("beforeCompletion");
      beforeCompletions++;
   }

   public Serializable getState()
   {
      return state;
   }

   @Remove
   public void remove()
   {
      log.info("remove");
   }

   public void setState(Serializable state)
   {
      this.state = state;
   }
}
