/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.stateless_deployment_descriptor.client;

import org.jboss.tutorial.stateless_deployment_descriptor.bean.Calculator;
import org.jboss.tutorial.stateless_deployment_descriptor.bean.CalculatorRemote;

import javax.naming.InitialContext;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      Calculator calculator = (Calculator) ctx.lookup(CalculatorRemote.class.getName());

      System.out.println("1 + 1 = " + calculator.add(1, 1));
      System.out.println("1 - 1 = " + calculator.subtract(1, 1));
   }
}
