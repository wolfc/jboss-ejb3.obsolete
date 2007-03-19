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
package org.jboss.ejb3.test.regression.ejbthree620;

import javax.ejb.EJB;
//import javax.ejb.EJBException;
import javax.ejb.Stateless;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@Stateless
public class CalculatorBean implements Calculator
{
   @EJB(beanInterface=Calculator.class) Object other;
//   @EJB(businessInterface=Calculator.class) Object deprecatedOther;
   
   public int add(int a, int b)
   {
      int result1 = ((Calculator) other).performAddition(a, b);
//      int result2 = ((Calculator) deprecatedOther).performAddition(a, b);
//      if(result1 != result2)
//         throw new EJBException("something horrible happened");
      return result1;
   }

   public int performAddition(int a, int b)
   {
      return a + b;
   }
}
