package xpetstore.web.struts.action.cart;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.customer.ejb.Customer;

import xpetstore.services.cart.ejb.Cart;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="cartForm"
 *      path="/checkout"
 *      scope="request"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/checkout.jsp"
 */
public class CheckoutAction
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
        String    userId = ( String ) request.getSession(  ).getAttribute( USERID_KEY );
        CartForm  frm = ( CartForm ) form;
        Cart cart = getCart( request );

        /* Order */
        Customer cust = getPetstore(  ).getCustomer( userId );
        frm.setCustomer( cust );

        /* Cart items */
        Collection items = cart.getCartItems(  );
        frm.setCartItems( items );

        /* Total */
        frm.setTotal( cart.getTotal(  ) );

        return mapping.findForward( SUCCESS );
    }
}
