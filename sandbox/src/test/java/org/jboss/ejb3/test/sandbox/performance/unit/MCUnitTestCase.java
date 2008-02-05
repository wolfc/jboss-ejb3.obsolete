/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.sandbox.performance.unit;


import junit.framework.TestCase;

import org.jboss.ejb3.sandbox.mc.MC;
import org.jboss.ejb3.test.sandbox.performance.Calculator;
import org.jboss.ejb3.test.sandbox.performance.CalculatorBean;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MCUnitTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(MCUnitTestCase.class);
   
   public void test1() throws Throwable
   {
      MC mc = new MC();
      
      mc.install("calculator", CalculatorBean.class);
      
      Calculator calculator = mc.lookup("calculator", Calculator.class);
      
      //System.out.println(calculator.calculatePi(40));
      
      StressCreator.createStress(calculator);
   }
}
