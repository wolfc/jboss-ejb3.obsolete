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
package org.jboss.ejb3.test.ejbthree1786;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * StatefulBean
 *
 * A test SFSB which provides access to an internal counter
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Local(StatefulLocalBusiness.class)
@Remote(StatefulRemoteBusiness.class)
@RemoteBinding(jndiBinding = StatefulRemoteBusiness.JNDI_NAME)
public class StatefulBean implements StatefulCommonBusiness
{

   //------------------------------------------------------------------------||
   // Instance Members ------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * The internal counter
    */
   private int counter;

   //------------------------------------------------------------------------||
   // Required Implementations ----------------------------------------------||
   //------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1786.StatefulRemoteBusiness#getCounter()
    */
   public int getCounter()
   {
      return counter;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1786.StatefulRemoteBusiness#incrementCounter()
    */
   public void incrementCounter()
   {
      counter++;
   }

}
