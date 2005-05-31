/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.security.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.annotation.security.Unchecked;
import javax.annotation.security.RolesAllowed;
import javax.ejb.RemoteInterface;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.annotation.security.SecurityDomain;

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

   @RolesAllowed({"student"})
   public int subtract(int x, int y)
   {
      return x - y;
   }

   @RolesAllowed({"teacher"})
   public int divide(int x, int y)
   {
      return x / y;
   }
}
