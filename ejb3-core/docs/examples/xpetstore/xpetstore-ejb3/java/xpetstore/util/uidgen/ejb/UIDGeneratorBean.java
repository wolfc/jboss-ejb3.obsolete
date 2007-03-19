package xpetstore.util.uidgen.ejb;

import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.ejb.Local;
import javax.ejb.Stateless;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @ ejb.bean
 *      name="UIDGenerator"
 *      type="Stateless"
 *      view-type="local"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.ejb-ref
 *      ejb-name="Counter"
 *      view-type="local"
 *      ref-name="ejb/CounterLocal"
 */

@Stateless(name = "UIDGenerator")
@Local(UIDGenerator.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UIDGeneratorBean
{
   @PersistenceContext
   private EntityManager manager;

   /**
    * @ ejb.interface-method
    */
   public int getUniqueId(String idPrefix)
   {
      return getCounter(idPrefix).nextValue();
   }

   private Counter getCounter(String name)
   {
      Counter counter = null;
      try
      {
         counter = manager.find(Counter.class, name);
         if (counter == null)
         {
            counter = new Counter(name);
            manager.persist(counter);
         }
      } catch (Exception e)
      {
         e.printStackTrace();
         throw new EJBException(e);
      }

      return counter;
   }
}
