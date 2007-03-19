package xpetstore.web.struts.action.product;

import java.util.Collection;

import xpetstore.web.struts.action.*;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.form
 *      name="searchForm"
 */
public class SearchForm
    extends BaseForm
{
    //~ Instance fields --------------------------------------------------------

    private String     _keyword;
    private Collection _productValues;

    //~ Methods ----------------------------------------------------------------

    public String getKeyword(  )
    {
        return _keyword;
    }

    public void setKeyword( String keyword )
    {
        _keyword = keyword;
    }

    public Collection getProductValues(  )
    {
        return _productValues;
    }

    public void setProductValues( Collection productValues )
    {
        _productValues = productValues;
    }
}
