/*
 * Created on Feb 22, 2003
 */
package xpetstore.domain;

import java.io.Serializable;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @hibernate.class
 *         table="T_ORDER_ITEM"
 */
public class OrderItem
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private Item   _item;
    private long   _orderItemId;
    private int    _quantity;
    private double _unitPrice;

    //~ Constructors -----------------------------------------------------------

    public OrderItem(  ) {}

    public OrderItem( Item item,
                      int  quantity )
    {
        _item      = item;
        _quantity  = quantity;
        _unitPrice = item.getListPrice(  );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @return Item
     *
     * @hibernate.many-to-one
     *      column="item_fk"
     *      cascade="none"
     */
    public Item getItem(  )
    {
        return _item;
    }

    /**
     * @return long
     *
     * @hibernate.id
     *      generator-class="vm.long"
     */
    public long getOrderItemId(  )
    {
        return _orderItemId;
    }

    /**
     * @return int
     *
     * @hibernate.property
     */
    public int getQuantity(  )
    {
        return _quantity;
    }

    public double getSubTotal(  )
    {
        return _quantity * _unitPrice;
    }

    /**
     * @return double
     *
     * @hibernate.property
     */
    public double getUnitPrice(  )
    {
        return _unitPrice;
    }

    /**
     * Sets the item.
     * @param item The item to set
     */
    public void setItem( Item item )
    {
        _item = item;
    }

    /**
     * Sets the orderItemUId.
     * @param orderItemUId The orderItemUId to set
     */
    public void setOrderItemId( long orderItemUId )
    {
        _orderItemId = orderItemUId;
    }

    /**
     * Sets the quantity.
     * @param quantity The quantity to set
     */
    public void setQuantity( int quantity )
    {
        this._quantity = quantity;
    }

    /**
     * Sets the unitPrice.
     * @param unitPrice The unitPrice to set
     */
    public void setUnitPrice( double unitPrice )
    {
        _unitPrice = unitPrice;
    }
}
