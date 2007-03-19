package xpetstore.web.struts.action.category;

import java.util.Collection;

import xpetstore.domain.catalog.ejb.Category;

import xpetstore.web.struts.action.*;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.form
 *      name="categoryForm"
 */
public class CategoryForm
    extends BaseForm
{
    //~ Instance fields --------------------------------------------------------

    private String      _categoryId = "";
    private Category    _category = new Category(  );
    private Collection  _products;

    public CategoryForm()
    {
    }

    /**
     * @return String
     */
    public String getCategoryId(  )
    {
        return _categoryId;
    }

    /**
     * @return CategoryValue
     */
    public Category getCategory(  )
    {
       if (_category == null)
          _category = new Category();
       System.out.println("!! CategoryForm.getCategory " + _category);
        return _category;
    }

    /**
     * @return Collection
     */
    public Collection getProducts(  )
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
     * Sets the categoryValue.
     * @param categoryValue The categoryValue to set
     */
    public void setCategory( Category category )
    {
        _category = category;
    }

    /**
     * Sets the productValues.
     * @param productValues The productValues to set
     */
    public void setProducts( Collection products )
    {
        _products = products;
    }
}
