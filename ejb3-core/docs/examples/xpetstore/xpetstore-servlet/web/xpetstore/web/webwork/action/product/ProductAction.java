/*
 * Created on Feb 23, 2003
 */
package xpetstore.web.webwork.action.product;

import java.util.Iterator;

import cirrus.hibernate.Session;

import xpetstore.domain.Item;
import xpetstore.domain.Product;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="product"
 *   success="product.vm"
 */
public class ProductAction
    extends BaseAction
{
    //~ Instance fields --------------------------------------------------------

    private Product _product = null;
    private String  _productId = "";

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
            _product = ( Product ) s.load( Product.class, _productId );

            /*
             * Since product.item is lazy loaded,
             * traverse the collection to load all the product to display
             */
            Iterator it = _product.getItems(  ).iterator(  );

            while ( it.hasNext(  ) )
            {
                Item i = ( Item ) it.next(  );
                i.getDescription(  );
            }

            return SUCCESS;
        }
        finally
        {
            s.close(  );
        }
    }

    /**
     * @return Product
     */
    public Product getProduct(  )
    {
        return _product;
    }

    /**
     * @return String
     */
    public String getProductId(  )
    {
        return _productId;
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
     * Sets the productId.
     * @param productId The productId to set
     */
    public void setProductId( String productId )
    {
        _productId = productId;
    }
}
