/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1066.unit;

import javax.naming.NameNotFoundException;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1066.Adder;
import org.jboss.ejb3.test.ejbthree1066.Subtractor;
import org.jboss.test.JBossTestCase;

/**
 * Business interfaces in the deployment descriptor are totally ignored.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 67223 $
 */
public class MultiBusinessDescriptorUnitTestCase extends JBossTestCase
{
   public MultiBusinessDescriptorUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(MultiBusinessDescriptorUnitTestCase.class, "ejbthree1066.jar");
   }
   
   public void testCalculator() throws Exception
   {
      try
      {
         Object bean = getInitialContext().lookup("CalculatorBean/remote");
         
         assertTrue("bean must have " + Adder.class.getName() + " as business interface", bean instanceof Adder);
         assertTrue("bean must have " + Subtractor.class.getName()+" as business interface", bean instanceof Subtractor);
         
         int actual;
         
         actual = ((Adder) bean).add(1, 2);
         assertEquals(3, actual);
         
         actual = ((Subtractor) bean).subtract(5, 1);
         assertEquals(4, actual);
      }
      catch(NameNotFoundException e)
      {
         fail("CalculatorBean was not deployed properly");
      }
   }
}
