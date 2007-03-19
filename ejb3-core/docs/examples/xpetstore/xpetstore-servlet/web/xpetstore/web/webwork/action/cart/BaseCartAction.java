/*
 * Created on 25-Feb-2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 */
package xpetstore.web.webwork.action.cart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import cirrus.hibernate.Session;

import xpetstore.domain.Item;
import xpetstore.domain.OrderItem;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public abstract class BaseCartAction
    extends BaseAction
{
    //~ Instance fields --------------------------------------------------------

    private Collection _cartItems;

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns all the {@link xpetstore.web.cart.CartItem}s
     * @return Collection
     * @throws Exception
     */
    public Collection getCartItems(  )
        throws Exception
    {
        Session s = null;
        Map     cart = getCart(  );

        try
        {
            if ( _cartItems == null )
            {
                _cartItems = new ArrayList(  );

                if ( cart.size(  ) > 0 )
                {
                    s = getHibernateSession(  );

                    Iterator it = cart.keySet(  ).iterator(  );

                    while ( it.hasNext(  ) )
                    {
                        String  itemId = ( String ) it.next(  );
                        Integer quantity = ( Integer ) cart.get( itemId );
                        Item    item = ( Item ) s.load( Item.class, itemId );

                        _cartItems.add( new OrderItem( item, quantity.intValue(  ) ) );
                    }
                }
            }

            return _cartItems;
        }
        catch ( Exception e )
        {
            e.printStackTrace(  );
            throw e;
        }
        finally
        {
            if ( s != null )
            {
                s.close(  );
            }
        }
    }
}
