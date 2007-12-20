/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree785;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class TesterBean implements Tester
{

   @EJB
   private MyStatelessLocal local;

   public String sayHiTo(String name)
   {
      return local.sayHiTo(name);
   }
}
