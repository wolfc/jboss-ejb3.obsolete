/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.stateful_deployment_descriptor.bean;

import java.util.HashMap;
import javax.ejb.Remove;

public interface ShoppingCart
{
   void buy(String product, int quantity);

   HashMap<String, Integer> getCartContents();

   void checkout();
}
