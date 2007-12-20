package org.jboss.ejb3.test.servicedependency;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.logging.Logger;

/**
 * AccountBean
 *
 * @author <a href="galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $$Revision: 67628 $$
 */
@Stateless
@Remote(Account.class)
public class AccountBean implements Account {

   private static final Logger log = Logger.getLogger(AccountBean.class);
   
   @Depends ("acme:service=uniqueid")
   private UniqueIdMBean uniqueId;
   
   public void debit(String account, int amount)
   {
      log.info("debiting " + amount + " swiss francs from account " + account);
      log.info("transaction id: " + uniqueId.generate());
   }
}
