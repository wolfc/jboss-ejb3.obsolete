package xpetstore.web.struts.action.product;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.catalog.ejb.Product;

import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="productForm"
 *      path="/product"
 *      scope="request"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="product.jsp"
 */
public class ProductAction
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
        ProductForm   frm = ( ProductForm ) form;
        String        productId = frm.getProductId(  );
        Petstore petstore = getPetstore(  );

        /* Product */
        Product product = petstore.getProduct( productId );
        frm.setProduct( product );

        /* Items */
        Collection items = petstore.getItems( productId, 0, Integer.MAX_VALUE ).getList(  );
        frm.setItems( items );

        return mapping.findForward( SUCCESS );
    }
}
