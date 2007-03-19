/*
 * Created on Feb 23, 2003
 */
package xpetstore.web.webwork.action.category;

import java.util.Iterator;

import cirrus.hibernate.Session;

import xpetstore.domain.Category;
import xpetstore.domain.Product;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="category"
 *   success="category.vm"
 */
public class CategoryAction
    extends BaseAction
{
    //~ Instance fields --------------------------------------------------------

    private Category _category = null;
    private String   _categoryId = "";

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
            _category = ( Category ) s.load( Category.class, _categoryId );

            /*
             * Since category.product is lazy loaded,
             * traverse the collection to load all the product to display
             */
            Iterator it = _category.getProducts(  ).iterator(  );

            while ( it.hasNext(  ) )
            {
                Product p = ( Product ) it.next(  );
                p.getName(  );
            }

            return SUCCESS;
        }
        finally
        {
            s.close(  );
        }
    }

    /**
     * @return Category
     */
    public Category getCategory(  )
    {
        return _category;
    }

    /**
     * @return String
     */
    public String getCategoryId(  )
    {
        return _categoryId;
    }

    /**
     * Sets the category.
     * @param category The category to set
     */
    public void setCategory( Category category )
    {
        _category = category;
    }

    /**
     * Sets the categoryId.
     * @param categoryId The categoryId to set
     */
    public void setCategoryId( String categoryId )
    {
        _categoryId = categoryId;
    }
}
