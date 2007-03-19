/*
 * Created on Feb 26, 2003
 */
package xpetstore.web.webwork.action.order;

import java.util.Iterator;
import java.util.Map;

import cirrus.hibernate.Session;
import cirrus.hibernate.Transaction;

import xpetstore.domain.Customer;
import xpetstore.domain.Item;
import xpetstore.domain.Order;

import xpetstore.util.MailUtil;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="createOrder"
 *   success="confirmation.vm"
 */
public class CreateOrderAction
    extends BaseAction
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        Session     s = getHibernateSession(  );
        Transaction tx = null;

        try
        {
            tx = s.beginTransaction(  );

            /* Customer */
            Customer cst = ( Customer ) s.load( Customer.class, getUserId(  ) );

            /* Order + Items*/
            Order    order = new Order( cst );
            Map      cart = getCart(  );
            Iterator it = cart.keySet(  ).iterator(  );

            while ( it.hasNext(  ) )
            {
                String  itemId = ( String ) it.next(  );
                Item    item = ( Item ) s.load( Item.class, itemId );
                Integer quantity = ( Integer ) cart.get( itemId );
                order.add( item, quantity.intValue(  ) );
            }

            /* Save */
            s.save( order );
            tx.commit(  );

            /* Empty the cart */
            getCart(  ).clear(  );

            /* send the email */
            String subject = "[xpetstore] Order Confimation";
            String body = "Your order has been submitted.\nThe order number is: " + order.getOrderId(  );

            try
            {
                MailUtil.send( cst.getEmail(  ), subject, body );
            }
            catch ( Exception e )
            {
                _log.error( "Unexpected error while sending email", e );
            }

            return SUCCESS;
        }
        catch ( Exception e )
        {
            _log.error( "Unexpected error", e );

            if ( tx != null )
            {
                tx.rollback(  );
            }

            throw e;
        }
        finally
        {
            s.close(  );
        }
    }
}
