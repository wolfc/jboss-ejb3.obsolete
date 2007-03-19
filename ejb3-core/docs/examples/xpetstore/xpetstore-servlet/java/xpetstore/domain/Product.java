/*
 * Created on Feb 22, 2003
 */
package xpetstore.domain;

import java.io.Serializable;

import java.util.Set;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @hibernate.class
 *         table="T_PRODUCT"
 */
public class Product
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private String _productId;
    private String _name;
    private String _description;
    private Set    _items;

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
     * @return Set
     *
     * @hibernate.set
     *    role="items"
     *    lazy="true"
     *    cascade="delete"
     * @hibernate.collection-key
     *    column="product_fk"
     * @hibernate.collection-one-to-many
     *    class="xpetstore.domain.Item"
     */
    public Set getItems(  )
    {
        return _items;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *      length="50"
     */
    public String getName(  )
    {
        return _name;
    }

    /**
     * @return String
     *
     * @hibernate.id
     *      generator-class="assigned"
     *      length="10"
     */
    public String getProductId(  )
    {
        return _productId;
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
     * Sets the items.
     * @param items The items to set
     */
    public void setItems( Set items )
    {
        _items = items;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName( String name )
    {
        _name = name;
    }

    /**
     * Sets the productId.
     * @param productId The productId to set
     */
    public void setProductId( String productId )
    {
        _productId = productId;
    }
}
