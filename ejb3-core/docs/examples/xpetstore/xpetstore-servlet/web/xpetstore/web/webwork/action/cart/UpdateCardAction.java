/*
 * Created on 24-Feb-2003
 */
package xpetstore.web.webwork.action.cart;

import java.util.Map;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="updateCart"
 *   success="cart.action"
 */
public class UpdateCardAction
    extends BaseCartAction
{
    //~ Instance fields --------------------------------------------------------

    private String _itemId[];
    private int    _quantity[];

    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        Map cart = getCart(  );

        for ( int i = 0; i < _quantity.length; i++ )
        {
            int qty = _quantity[ i ];

            if ( qty <= 0 )
            {
                cart.remove( _itemId[ i ] );
            }
            else
            {
                cart.put( _itemId[ i ], new Integer( qty ) );
            }
        }

        return SUCCESS;
    }

    /**
     * @return String[]
     */
    public String[] getItemId(  )
    {
        return _itemId;
    }

    /**
     * @return int[]
     */
    public int[] getQuantity(  )
    {
        return _quantity;
    }

    /**
     * Sets the itemId.
     * @param itemId The itemId to set
     */
    public void setItemId( String itemId[] )
    {
        _itemId = itemId;
    }

    /**
     * Sets the quantity.
     * @param quantity The quantity to set
     */
    public void setQuantity( int quantity[] )
    {
        this._quantity = quantity;
    }
}
