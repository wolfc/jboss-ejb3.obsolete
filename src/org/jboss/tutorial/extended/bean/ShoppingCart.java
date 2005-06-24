package org.jboss.tutorial.extended.bean;

import javax.ejb.Remove;
import javax.persistence.FlushMode;
import javax.persistence.FlushModeType;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public interface ShoppingCart
{
   long createCustomer();

   void update();

   Customer find(long id);

   @Remove void checkout();

   void update2();

   void update3();

   @FlushMode(FlushModeType.NEVER) void never();
}
