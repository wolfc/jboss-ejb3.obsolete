package xpetstore.web.struts.action.item;

import xpetstore.domain.catalog.ejb.Item;
import xpetstore.domain.catalog.ejb.Product;

import xpetstore.web.struts.action.*;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.form
 *      name="itemForm"
 */
public class ItemForm
    extends BaseForm
{
    //~ Instance fields --------------------------------------------------------

    private String       _itemId;
    private Item    _item = new Item(  );
    private Product _product;

    //~ Methods ----------------------------------------------------------------

    /**
     * @return String
     */
    public String getItemId(  )
    {
        return _itemId;
    }

    /**
     * @return ItemValue
     */
    public Item getItem(  )
    {
        return _item;
    }

    /**
     * @return ProductValue
     */
    public Product getProduct(  )
    {
        return _product;
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
     * Sets the itemValue.
     * @param itemValue The itemValue to set
     */
    public void setItem( Item item )
    {
        _item = item;
    }

    /**
     * Sets the productValue.
     * @param productValue The productValue to set
     */
    public void setProduct( Product product )
    {
        _product = product;
    }
}
