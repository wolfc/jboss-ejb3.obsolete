/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.tutorial.security.client;

import java.util.Properties;
import javax.ejb.EJBAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.jboss.tutorial.security.bean.Calculator;

/**
 * @version $Revision$
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      // Establish the proxy with an incorrect security identity
      Properties env = new Properties();
      env.setProperty(Context.SECURITY_PRINCIPAL, "kabir");
      env.setProperty(Context.SECURITY_CREDENTIALS, "invalidpassword");
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      InitialContext ctx = new InitialContext(env);
      Calculator calculator = (Calculator) ctx.lookup("CalculatorBean/remote");

      System.out.println("Kabir is a student.");
      System.out.println("Kabir types in the wrong password");
      try
      {
         System.out.println("1 + 1 = " + calculator.add(1, 1));
      }
      catch (EJBAccessException ex)
      {
         System.out.println("Saw expected SecurityException: " + ex.getMessage());
      }

      System.out.println("Kabir types in correct password.");
      System.out.println("Kabir does unchecked addition.");

      // Re-establish the proxy with the correct security identity
      env.setProperty(Context.SECURITY_CREDENTIALS, "validpassword");
      ctx = new InitialContext(env);
      calculator = (Calculator) ctx.lookup("CalculatorBean/remote");

      System.out.println("1 + 1 = " + calculator.add(1, 1));

      System.out.println("Kabir is not a teacher so he cannot do division");
      try
      {
         calculator.divide(16, 4);
      }
      catch (javax.ejb.EJBAccessException  ex)
      {
         System.out.println(ex.getMessage());
      }

      System.out.println("Students are allowed to do subtraction");
      System.out.println("1 - 1 = " + calculator.subtract(1, 1));
   }
}
