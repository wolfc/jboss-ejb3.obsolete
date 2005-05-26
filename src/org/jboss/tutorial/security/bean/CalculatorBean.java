/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.security.bean;

import javax.ejb.MethodPermissions;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.Unchecked;
import javax.ejb.RemoteInterface;
import org.jboss.ejb3.security.SecurityDomain;

@Stateless
@SecurityDomain("other")
@RemoteInterface(Calculator.class)
public class CalculatorBean implements Calculator
{
   @Unchecked
   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
   public int add(int x, int y)
   {
      return x + y;
   }

   @MethodPermissions({"student"})
   public int subtract(int x, int y)
   {
      return x - y;
   }

   @MethodPermissions({"teacher"})
   public int divide(int x, int y)
   {
      return x / y;
   }
}
