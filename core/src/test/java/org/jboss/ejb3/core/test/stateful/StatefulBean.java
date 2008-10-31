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
package org.jboss.ejb3.core.test.stateful;

import javax.annotation.PreDestroy;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;

/**
 * StatefulBean
 * 
 * A SFSB for use in testing
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Local(StatefulLocalBusiness.class)
@Remote(StatefulRemoteBusiness.class)
@LocalHome(StatefulLocalHome.class)
@RemoteHome(StatefulRemoteHome.class)
public class StatefulBean implements StatefulRemoteBusiness, StatefulLocalBusiness
{
   private static Logger log = Logger.getLogger(StatefulBean.class);
   
   public static int preDestroys = 0;
   
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private int counter = 0;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Returns the next sequence in the counter; starts at 
    * 0 (ie. first invocation will return 1, subsequent 
    * invocations = last++)
    * 
    * @return
    */
   public int getNextCounter()
   {
      return ++counter;
   }

   @PreDestroy
   public void preDestroy()
   {
      log.info("preDestroy");
      preDestroys++;
   }
   
   @Remove
   public void remove()
   {
      log.info("remove");
   }
}
