/*
 * Created on Feb 22, 2003
 */
package xpetstore.domain;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @hibernate.class
 *         table="T_CATEGORY"
 */
public class Category
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private String _categoryId;
    private String _name;
    private String _description;
    private Set    _products = new HashSet(  );

    //~ Methods ----------------------------------------------------------------

    /**
     * @return String
     *
     * @hibernate.id
     *      generator-class="assigned"
     *      length="10"
     */
    public String getCategoryId(  )
    {
        return _categoryId;
    }

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
     *      length="50"
     */
    public String getName(  )
    {
        return _name;
    }

    /**
     * @return Set
     *
     * @hibernate.set
     *    role="products"
     *    lazy="true"
     *    cascade="delete"
     * @hibernate.collection-key
     *    column="category_fk"
     * @hibernate.collection-one-to-many
     *    class="xpetstore.domain.Product"
     */
    public Set getProducts(  )
    {
        return _products;
    }

    /**
     * Sets the categoryId.
     * @param categoryId The categoryId to set
     */
    public void setCategoryId( String categoryId )
    {
        _categoryId = categoryId;
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
     * Sets the name.
     * @param name The name to set
     */
    public void setName( String name )
    {
        _name = name;
    }

    /**
     * Sets the products.
     * @param products The products to set
     */
    public void setProducts( Set products )
    {
        _products = products;
    }
}
