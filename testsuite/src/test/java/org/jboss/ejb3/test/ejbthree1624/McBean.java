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
package org.jboss.ejb3.test.ejbthree1624;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJB;

import org.jboss.logging.Logger;

/**
 * McBean
 * 
 * A Simple POJO for testing, to be installed as an MC Bean
 * 
 * Business methods are delegated to the underlying instances,
 * EJBs which are to be injected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class McBean
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(McBean.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * Injected members
    */
   //TODO Field member cannot be named the same
   // as accessor / mutator methods?
   // http://www.jboss.com/index.html?module=bb&op=viewtopic&t=147055
   @EJB
   private CalculatorLocalBusiness calcLocalBusiness;

   @EJB
   private CalculatorRemoteBusiness calcRemoteBusiness;

   @EJB
   private CalculatorLocalHome calcLocalHome;

   @EJB
   private CalculatorHome calcRemoteHome;

   // --------------------------------------------------------------------------------||
   // Business Methods ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Adds the specified arguments by way of the 
    * local business delegate
    * 
    * @param args
    * @return
    */
   public int addUsingLocalBusinessView(int... args)
   {
      // Use the local business delegate
      log.info("Adding using local business view...");
      return this.add(calcLocalBusiness, args);
   }

   /**
    * Adds the specified arguments by way of the 
    * remote business delegate
    * 
    * @param args
    * @return
    */
   public int addUsingRemoteBusinessView(int... args)
   {
      // Use the remote business delegate
      log.info("Adding using remote business view...");
      return this.add(calcRemoteBusiness, args);
   }

   /**
    * Adds the specified arguments by way of the 
    * local component (EJB2.x) delegate
    * 
    * @param args
    * @return
    */
   public int addUsingLocalComponentView(int... args) throws DelegateNotInjectedException
   {
      // Precondition check
      if (calcLocalHome == null)
      {
         throw new DelegateNotInjectedException();
      }

      // Use the local component delegate via local home
      log.info("Adding using local component view...");
      CalculatorLocal local = null;
      try
      {
         local = calcLocalHome.create();
      }
      catch (CreateException e)
      {
         throw new RuntimeException(e);
      }
      return this.add(local, args);
   }

   /**
    * Adds the specified arguments by way of the 
    * remote component (EJB2.x) delegate
    * 
    * @param args
    * @return
    */
   public int addUsingRemoteComponentView(int... args) throws DelegateNotInjectedException
   {
      // Precondition check
      if (calcRemoteHome == null)
      {
         throw new DelegateNotInjectedException();
      }

      // Use the remote component delegate via remote home
      log.info("Adding using remote component view...");
      CalculatorRemote remote = null;
      try
      {
         remote = calcRemoteHome.create();
      }
      catch (CreateException e)
      {
         throw new RuntimeException(e);
      }
      catch (RemoteException re)
      {
         throw new RuntimeException(re);
      }
      return this.add(remote, args);
   }

   // --------------------------------------------------------------------------------||
   // Internal Helper Methods --------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Uses the specified delegate to add the specified arguments
    * 
    * @param delegate
    * @param args
    * @return
    */
   private int add(CalculatorService delegate, int... args) throws DelegateNotInjectedException
   {
      // Precondition check
      if (delegate == null)
      {
         throw new DelegateNotInjectedException();
      }

      // Log
      log.info("Adding using " + delegate);

      // Return
      return delegate.add(args);
   }

}
