/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.entity.bean;

import javax.ejb.Remote;
import javax.ejb.Remove;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Remote
        public interface ShoppingCart
{
   void buy(String product, int quantity, double price);

   Order getOrder();

   @Remove void checkout();
}
