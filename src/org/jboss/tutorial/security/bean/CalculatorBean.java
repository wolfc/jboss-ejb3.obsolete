/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.security.bean;

import org.jboss.ejb3.security.SecurityDomain;

import javax.ejb.MethodPermissions;
import javax.ejb.Stateless;
import javax.ejb.Tx;
import javax.ejb.TxType;
import javax.ejb.Unchecked;

@Stateless
@SecurityDomain("other")
public class CalculatorBean implements Calculator
{
   @Unchecked
   @Tx(TxType.REQUIRESNEW)
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
