/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.jndibinding.client;

import org.jboss.tutorial.jndibinding.bean.Calculator;

import javax.naming.InitialContext;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      Calculator calculator = (Calculator) ctx.lookup("Calculator");

      System.out.println("1 + 1 = " + calculator.add(1, 1));
      System.out.println("1 - 1 = " + calculator.subtract(1, 1));
   }
}
