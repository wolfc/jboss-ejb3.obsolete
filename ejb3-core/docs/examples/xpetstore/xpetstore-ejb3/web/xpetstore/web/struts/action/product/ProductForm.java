package xpetstore.web.struts.action.product;

import java.util.Collection;

import xpetstore.domain.catalog.ejb.Product;

import xpetstore.web.struts.action.BaseForm;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.form
 *      name="productForm"
 */
public class ProductForm
    extends BaseForm
{
    //~ Instance fields --------------------------------------------------------

    private String       _productId;
    private Product     _product = new Product(  );
    private Collection   _items;

    //~ Methods ----------------------------------------------------------------

    /**
     * @return Collection
     */
    public Collection getItems(  )
    {
        return _items;
    }

    /**
     * @return String
     */
    public String getProductId(  )
    {
        return _productId;
    }

    /**
     * @return ProductValue
     */
    public Product getProduct(  )
    {
        return _product;
    }

    /**
     * Sets the itemValues.
     * @param itemValues The itemValues to set
     */
    public void setItems( Collection items )
    {
        _items = items;
    }

    /**
     * Sets the productId.
     * @param productId The productId to set
     */
    public void setProductId( String productId )
    {
        _productId = productId;
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
