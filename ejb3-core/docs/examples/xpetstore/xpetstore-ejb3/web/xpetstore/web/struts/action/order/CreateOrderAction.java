package xpetstore.web.struts.action.order;

import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.order.ejb.Order;
import xpetstore.services.cart.ejb.Cart;
import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="orderForm"
 *      path="/createOrder"
 *      scope="request"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/confirmation.jsp"
 */
public class CreateOrderAction
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
       try 
       {
       System.out.println("!!CreateOrderAction.doExecute");
        OrderForm     frm = ( OrderForm ) form;
        String        userId = ( String ) request.getSession(  ).getAttribute( USERID_KEY );
        Petstore petstore = getPetstore(  );

        /* Proceed the order */
        HashMap items = new HashMap(  );

        for ( int i = 0, len = frm.getItemId(  ).length; i < len; i++ )
        {
            items.put( frm.getItemId(  )[ i ], new Integer( frm.getQuantity(  )[ i ] ) );
        }

        Integer orderUId = petstore.createOrder( userId, new Date(  ), items );
        frm.getOrder(  ).setOrderUId( orderUId );

        /* Invalidating the current cart */
        Cart cart = getCart( false, request );

        if ( cart != null )
        {
            request.getSession(  ).removeAttribute( CART_KEY );
            cart.remove(  );
        }

        return mapping.findForward( SUCCESS );
       }
       catch (Exception e)
       {
          e.printStackTrace();
          throw e;
       }
    }
}
