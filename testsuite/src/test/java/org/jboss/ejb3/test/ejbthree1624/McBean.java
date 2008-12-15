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

import javax.ejb.EJB;

/**
 * McBean
 * 
 * A Simple POJO for testing, to be installed as an MC Bean
 * 
 * Business methods are delegated to the underlying instance,
 * an EJB which is to be injected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class McBean
{
   /**
    * Injected member
    */
   //TODO Field member cannot be named the same
   // as accessor / mutator methods?
   // http://www.jboss.com/index.html?module=bb&op=viewtopic&t=147055
   @EJB
   private CalculatorLocalBusiness calc;

   /**
    * Adds the specified arguments by way of the injected EJB
    * 
    * @param args
    * @return
    */
   public int add(int... args)
   {
      // Precondition check
      CalculatorLocalBusiness calc = this.getCalculator();
      if (calc == null)
      {
         throw new RuntimeException("Test fails, EJB instance not injected");
      }

      // Return the sum
      return calc.add(args);
   }

   public CalculatorLocalBusiness getCalculator()
   {
      return calc;
   }

   public void setCalculator(CalculatorLocalBusiness calc)
   {
      this.calc = calc;
   }
   

   
   
}
