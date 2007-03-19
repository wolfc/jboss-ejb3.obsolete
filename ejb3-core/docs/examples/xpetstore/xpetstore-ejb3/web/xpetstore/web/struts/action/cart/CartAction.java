package xpetstore.web.struts.action.cart;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.services.cart.ejb.Cart;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="cartForm"
 *      path="/cart"
 *      scope="request"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/cart.jsp"
 */
public class CartAction
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
        CartForm  frm = ( CartForm ) form;

        Cart cart = getCart( request );

        /* Cart items */
        Collection items = cart.getCartItems(  );
        frm.setCartItems( items );

        /* Total */
        double total = cart.getTotal(  );
        frm.setTotal( total );

        return mapping.findForward( SUCCESS );
    }
}
