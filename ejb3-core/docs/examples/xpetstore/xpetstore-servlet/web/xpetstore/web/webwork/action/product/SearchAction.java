/*
 * Created on Feb 27, 2003
 */
package xpetstore.web.webwork.action.product;

import java.util.ArrayList;
import java.util.Collection;

import cirrus.hibernate.Session;

import xpetstore.domain.Product;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="search"
 *   success="search.vm"
 */
public class SearchAction
    extends BaseAction
{
    //~ Instance fields --------------------------------------------------------

    private String     _keyword = "";
    private Collection _products = new ArrayList(  );

    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        if ( ( _keyword == null ) || ( _keyword.length(  ) == 0 ) )
        {
            return SUCCESS;
        }

        Session s = getHibernateSession(  );
        try
        {
            String oql = "FROM p IN CLASS " + Product.class + " WHERE" + " ( p.productId LIKE '%" + _keyword + "%' ) OR" + " ( p.name LIKE '%" + _keyword + "%' ) OR" + " ( p.description LIKE '%" + _keyword + "%' )";
            _products = s.find( oql );

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
    public String getKeyword(  )
    {
        return _keyword;
    }

    /**
     * @return Collection
     */
    public Collection getProducts(  )
    {
        return _products;
    }

    /**
     * Sets the keyword.
     * @param keyword The keyword to set
     */
    public void setKeyword( String keyword )
    {
        _keyword = keyword;
    }

    /**
     * Sets the products.
     * @param products The products to set
     */
    public void setProducts( Collection products )
    {
        _products = products;
    }
}
