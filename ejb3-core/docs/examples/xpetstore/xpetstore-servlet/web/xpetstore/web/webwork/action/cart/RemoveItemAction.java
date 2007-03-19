/*
 * Created on 24-Feb-2003
 */
package xpetstore.web.webwork.action.cart;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="removeItem"
 *   success="cart.action"
 */
public class RemoveItemAction
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
        getCart(  ).remove( _itemId );

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
