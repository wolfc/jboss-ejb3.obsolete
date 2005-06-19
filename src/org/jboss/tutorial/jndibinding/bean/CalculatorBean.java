/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.jndibinding.bean;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;

@Stateless
@RemoteBinding(jndiBinding="Calculator")
@LocalBinding(jndiBinding="CalculatorLocal")
@Remote(CalculatorRemote.class)
@Local(CalculatorLocal.class)
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
