/*
 * Created on 24-Feb-2003
 */
package xpetstore.web.webwork.action.item;

import cirrus.hibernate.Session;

import xpetstore.domain.Item;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="item"
 *   success="item.vm"
 */
public class ItemAction
    extends BaseAction
{
    //~ Instance fields --------------------------------------------------------

    private String _itemId;
    private Item   _item;

    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        Session s = getHibernateSession(  );

        try
        {
            _item = ( Item ) s.load( Item.class, _itemId );

            return SUCCESS;
        }
        finally
        {
            s.close(  );
        }
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

    /**
     * @return Item
     */
    public Item getItem(  )
    {
        return _item;
    }

    /**
     * Sets the item.
     * @param item The item to set
     */
    public void setItem( Item item )
    {
        _item = item;
    }
}
