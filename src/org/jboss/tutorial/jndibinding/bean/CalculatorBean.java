/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.jndibinding.bean;

import org.jboss.ejb3.remoting.RemoteBinding;
import org.jboss.ejb3.LocalBinding;

import javax.ejb.Stateless;

@Stateless
@RemoteBinding(jndiBinding="Calculator")
@LocalBinding(jndiBinding="CalculatorLocal")
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
