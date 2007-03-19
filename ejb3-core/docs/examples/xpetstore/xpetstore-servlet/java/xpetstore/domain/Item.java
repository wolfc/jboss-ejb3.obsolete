/*
 * Created on Feb 22, 2003
 */
package xpetstore.domain;

import java.io.Serializable;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @hibernate.class
 *         table="T_ITEM"
 */
public class Item
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private String  _itemId;
    private double  _listPrice;
    private double  _unitCost;
    private String  _description;
    private String  _imagePath;
    private Product _product;

    //~ Methods ----------------------------------------------------------------

    /**
     * @return String
     *
     * @hibernate.property
     *      length="255"
     */
    public String getDescription(  )
    {
        return _description;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *      length="255"
     */
    public String getImagePath(  )
    {
        return _imagePath;
    }

    /**
     * @return String
     *
     * @hibernate.id
     *      generator-class="assigned"
     *      length="10"
     */
    public String getItemId(  )
    {
        return _itemId;
    }

    /**
     * @return double
     *
     * @hibernate.property
     */
    public double getListPrice(  )
    {
        return _listPrice;
    }

    /**
     * @return Product
     *
     * @hibernate.many-to-one
     *      cascade="none"
     *      column="product_fk"
     */
    public Product getProduct(  )
    {
        return _product;
    }

    /**
     * @return double
     *
     * @hibernate.property
     */
    public double getUnitCost(  )
    {
        return _unitCost;
    }

    /**
     * Sets the description.
     * @param description The description to set
     */
    public void setDescription( String description )
    {
        _description = description;
    }

    /**
     * Sets the imagePath.
     * @param imagePath The imagePath to set
     */
    public void setImagePath( String imagePath )
    {
        _imagePath = imagePath;
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
     * Sets the listPrice.
     * @param listPrice The listPrice to set
     */
    public void setListPrice( double listPrice )
    {
        _listPrice = listPrice;
    }

    /**
     * Sets the product.
     * @param product The product to set
     */
    public void setProduct( Product product )
    {
        _product = product;
    }

    /**
     * Sets the unitCost.
     * @param unitCost The unitCost to set
     */
    public void setUnitCost( double unitCost )
    {
        _unitCost = unitCost;
    }
}
