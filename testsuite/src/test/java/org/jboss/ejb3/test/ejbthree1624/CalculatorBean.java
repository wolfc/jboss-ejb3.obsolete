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

import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

/**
 * CalculatorBean
 * 
 * The injection target for an MC Bean, provides the
 * actual implementation of the Calculator Service
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Local(CalculatorLocalBusiness.class)
@Remote(CalculatorRemoteBusiness.class)
@LocalHome(CalculatorLocalHome.class)
@RemoteHome(CalculatorHome.class)
public class CalculatorBean implements CalculatorLocalBusiness, CalculatorRemoteBusiness
{

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthreexxx.CalculatorLocalBusiness#add(int[])
    */
   public int add(int... args)
   {
      // Initialize
      int sum = 0;

      // Add all arguments
      for (int arg : args)
      {
         sum += arg;
      }

      // Return
      return sum;
   }

}
