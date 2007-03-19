package xpetstore.web.struts.action;

import java.util.ResourceBundle;

import javax.naming.InitialContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.customer.ejb.Customer;

import xpetstore.services.cart.ejb.Cart;
import xpetstore.services.petstore.ejb.Petstore;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public abstract class BaseAction
    extends Action
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String BUNDLE_NAME = "Resources";
    public static final String CART_KEY = "cart";
    public static final String ERROR = "error";
    public static final String MESSAGE_KEY = "message";
    public static final String SUCCESS = "success";
    public static final String USERID_KEY = "userId";
    public static final String USERNAME_KEY = "username";

    //~ Instance fields --------------------------------------------------------

    protected Log _log = LogFactory.getLog( getClass(  ) );

    //~ Methods ----------------------------------------------------------------

    protected void clearSession( HttpServletRequest request )
        throws Exception
    {
        HttpSession session = request.getSession(  );

        session.removeAttribute( USERID_KEY );
        session.removeAttribute( USERNAME_KEY );

        Cart cart = getCart( false, request );

        if ( cart != null )
        {
            session.removeAttribute( CART_KEY );
            cart.remove(  );
        }
    }

    protected abstract ActionForward doExecute( ActionMapping       mapping,
                                                ActionForm          form,
                                                HttpServletRequest  request,
                                                HttpServletResponse response )
        throws Exception;

    /**
     *
     * @see org.apache.struts.action.Action#execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)
     */
    public ActionForward execute( ActionMapping       mapping,
                                  ActionForm          form,
                                  HttpServletRequest  request,
                                  HttpServletResponse response )
        throws Exception
    {
        _log.info( "execute()" );

        return doExecute( mapping, form, request, response );
    }

    public Cart getCart( boolean            create,
                              HttpServletRequest request )
        throws Exception
    {
        HttpSession session = request.getSession(  );
        Cart   cart = ( Cart ) session.getAttribute( CART_KEY );
        if ( ( cart == null ) && create )
        {
            cart = (Cart)new InitialContext().lookup("ejb/Cart");
            session.setAttribute( CART_KEY, cart );
        }

        return cart;
    }

    public Cart getCart( HttpServletRequest request )
        throws Exception
    {
        return getCart( true, request );
    }

    public Petstore getPetstore(  )
        throws Exception
    {
        return (Petstore)new InitialContext().lookup("ejb/Petstore");
    }

    public String getString( String key )
    {
        try
        {
            return ResourceBundle.getBundle( BUNDLE_NAME ).getString( key );
        }
        catch ( Exception e )
        {
            return "???" + key + "???";
        }
    }

    protected void initSession( Customer      customer,
                                HttpServletRequest request )
    {
        HttpSession session = request.getSession(  );
        session.setAttribute( USERID_KEY, customer.getUserId(  ) );
        session.setAttribute( USERNAME_KEY, customer.getFirstname(  ) + " " + customer.getLastname(  ) );
    }
}
