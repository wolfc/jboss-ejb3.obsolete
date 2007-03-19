/*
 * Created on 24-Feb-2003
 */
package xpetstore.web.webwork.action.cart;

import java.util.Map;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="addItem"
 *   success="cart.action"
 */
public class AddItemAction
    extends BaseCartAction
{
    //~ Instance fields --------------------------------------------------------

    private String _itemId;

    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        Map     cart = getCart(  );
        Integer qty = ( Integer ) cart.get( _itemId );

        if ( qty == null )
        {
            cart.put( _itemId, new Integer( 1 ) );
        }
        else
        {
            cart.put( _itemId, new Integer( 1 + qty.intValue(  ) ) );
        }

        return SUCCESS;
    }

    /**
     * @return String
     */
    public String getItemId(  )
    {
        return _itemId;
    }

    /**
     * Sets the itemId.
     * @param itemId The itemId to set
     */
    public void setItemId( String itemId )
    {
        _itemId = itemId;
    }
}
