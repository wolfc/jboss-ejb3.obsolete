/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.security.client;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.tutorial.security.bean.Calculator;

import javax.naming.InitialContext;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      Calculator calculator = (Calculator) ctx.lookup(Calculator.class.getName());

      SecurityAssociation.setPrincipal(new SimplePrincipal("kabir"));

      System.out.println("Kabir is a student.");
      System.out.println("Kabir types in the wrong password");
      SecurityAssociation.setCredential("invalidpassword".toCharArray());
      try
      {
         System.out.println("1 + 1 = " + calculator.add(1, 1));
      }
      catch (SecurityException ex)
      {
         System.out.println(ex.getMessage());
      }

      System.out.println("Kabir types in correct password.");
      System.out.println("Kabir does unchecked addition.");
      SecurityAssociation.setCredential("validpassword".toCharArray());
      System.out.println("1 + 1 = " + calculator.add(1, 1));

      System.out.println("Kabir is not a teacher so he cannot do division");
      try
      {
         calculator.divide(16, 4);
      }
      catch (SecurityException ex)
      {
         System.out.println(ex.getMessage());
      }

      System.out.println("Students are allowed to do subtraction");
      System.out.println("1 - 1 = " + calculator.subtract(1, 1));
   }
}
