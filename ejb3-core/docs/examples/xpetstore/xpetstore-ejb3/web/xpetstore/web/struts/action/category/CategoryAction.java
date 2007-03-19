package xpetstore.web.struts.action.category;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.catalog.ejb.Category;

import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="categoryForm"
 *      path="/category"
 *      scope="request"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="category.jsp"
 */
public class CategoryAction
    extends BaseAction
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see xpetstore.web.struts.action.BaseAction#doExecute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)
     */
    protected ActionForward doExecute( ActionMapping       mapping,
                                       ActionForm          form,
                                       HttpServletRequest  request,
                                       HttpServletResponse response )
        throws Exception
    {
        CategoryForm  frm = ( CategoryForm ) form;
        String        categoryId = frm.getCategoryId(  );
        Petstore petstore = getPetstore(  );

        /* Category*/
        Category category = petstore.getCategory( categoryId );
        frm.setCategory( category );

        /* Items */
        Collection products = petstore.getProducts( categoryId, 0, Integer.MAX_VALUE ).getList(  );
        frm.setProducts( products );

        return mapping.findForward( SUCCESS );
    }
}
