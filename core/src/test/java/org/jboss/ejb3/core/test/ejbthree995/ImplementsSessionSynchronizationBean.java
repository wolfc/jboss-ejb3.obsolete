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
package org.jboss.ejb3.core.test.ejbthree995;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;

/**
 * ImplementsSessionSynchronizationBean
 * 
 * A test SFSB that directly implements SessionSynchronization
 * 
 * EJBTHREE-995
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Remote(ImplementsSessionSynchronizationRemoteBusiness.class)
public class ImplementsSessionSynchronizationBean
      implements
         ImplementsSessionSynchronizationRemoteBusiness,
         SessionSynchronization
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * Define some counters to be used by the test.  Ensure that
    * we're only testing *one instance at a time* because these are 
    * in class scope
    */
   public static int CALLS_AFTER_BEGIN = 0;

   public static int CALLS_AFTER_COMPLETION = 0;

   public static int CALLS_BEFORE_COMPLETION = 0;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * A simple invocation that implements no business logic
    */
   public void call()
   {
   }

   public void afterBegin() throws EJBException, RemoteException
   {
      CALLS_AFTER_BEGIN++;
   }

   public void afterCompletion(boolean committed) throws EJBException, RemoteException
   {
      CALLS_AFTER_COMPLETION++;
   }

   public void beforeCompletion() throws EJBException, RemoteException
   {
      CALLS_BEFORE_COMPLETION++;
   }

   // --------------------------------------------------------------------------------||
   // Helper Methods -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Resets the SessionSynchronization counters
    */
   public static void resetCounters()
   {
      CALLS_AFTER_BEGIN = 0;
      CALLS_AFTER_COMPLETION = 0;
      CALLS_BEFORE_COMPLETION = 0;
   }
}
