/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.locator.client.jndihostequality;

import junit.framework.TestCase;

import org.jboss.ejb3.locator.client.JndiHost;

public class JndiHostEqualityTestCase extends TestCase
{
   // Overridden Implementations
   /**
    * Setup
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   // Test Methods
   public void testEqualByValue()
   {
      // Obtain 2 objects of different references, same value
      JndiHost jndi1 = this.getJndiHost1();
      JndiHost jndi2 = this.getJndiHost1();
      // Ensure equal by value
      TestCase.assertTrue(jndi1.equals(jndi2));
      // Ensure hashCodes equal
      TestCase.assertTrue(new Integer(jndi1.hashCode()).equals(jndi2.hashCode()));
   }

   public void testNotEqualByValue()
   {
      // Obtain 2 objects of different values
      JndiHost jndi1 = this.getJndiHost1();
      JndiHost jndi2 = this.getJndiHost2();
      TestCase.assertFalse(jndi1.equals(jndi2));
   }

   // Internal Helper Methods
   private JndiHost getJndiHost1()
   {
      // Initialize
      JndiHost host = new JndiHost();

      // Set Properties
      host.setAddress("localhost");
      host.setPort(1098);

      // Return
      return host;
   }

   private JndiHost getJndiHost2()
   {
      // Initialize
      JndiHost host = new JndiHost();

      // Set Properties
      host.setAddress("localhost");
      host.setPort(1198);

      // Return
      return host;
   }
}
