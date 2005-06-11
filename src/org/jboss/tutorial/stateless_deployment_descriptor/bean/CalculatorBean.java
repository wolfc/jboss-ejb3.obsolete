/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.stateless_deployment_descriptor.bean;

import javax.ejb.Stateless;
import javax.ejb.RemoteInterface;
import javax.ejb.LocalInterface;
   
public class CalculatorBean implements CalculatorRemote, CalculatorLocal
{
   public int add(int x, int y)
   {
      return x + y;
   }

   public int subtract(int x, int y)
   {
      return x - y;
   }
}
