/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.jndibinding.bean;

import javax.ejb.Stateless;
import javax.ejb.RemoteInterface;
import javax.ejb.LocalInterface;
import org.jboss.ejb3.LocalBinding;
import org.jboss.ejb3.remoting.RemoteBinding;

@Stateless
@RemoteBinding(jndiBinding="Calculator")
@LocalBinding(jndiBinding="CalculatorLocal")
@RemoteInterface(CalculatorRemote.class)
@LocalInterface(CalculatorLocal.class)        
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
