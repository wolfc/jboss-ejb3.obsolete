package xpetstore.web.struts.action.product;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author Herve Tchepannou
 *
 * @struts.action
 *      name="searchForm"
 *      path="/search"
 *      scope="request"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="search.jsp"
 */
public class SearchAction
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
        SearchForm    frm = ( SearchForm ) form;
        String        keyword = frm.getKeyword(  );
        Petstore petstore = getPetstore(  );

        Collection    products = petstore.searchProducts( keyword, 0, Integer.MAX_VALUE ).getList(  );
        frm.setProductValues( products );

        return mapping.findForward( SUCCESS );
    }
}
