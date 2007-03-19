package xpetstore.web.struts.action.signon;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.customer.ejb.Customer;
import xpetstore.domain.signon.ejb.Account;

import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="signonForm"
 *      path="/login"
 *      input="/login.jsp"
 *      scope="request"
 *      validate="true"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/index.jsp"
 *
 * @struts.action-forward
 *      name="error"
 *      path="/signon.jsp"
 */
public class LoginAction
    extends BaseAction
{
    //~ Methods ----------------------------------------------------------------

    public ActionForward doExecute( ActionMapping       mapping,
                                    ActionForm          form,
                                    HttpServletRequest  request,
                                    HttpServletResponse response )
        throws Exception
    {
        SignonForm    frm = ( SignonForm ) form;
        Account  account = frm.getAccount(  );
        String        redirectUri = frm.getRedirectUri(  );
        Petstore petstore = getPetstore(  );

        if ( petstore.authenticate( account.getUserId(  ), account.getPassword(  ) ) )
        {
            Customer cust = petstore.getCustomer( account.getUserId(  ) );
            initSession( cust, request );

            if ( ( redirectUri == null ) || ( redirectUri.length(  ) == 0 ) )
            {
                return mapping.findForward( SUCCESS );
            }
            else
            {
                _log.info( "...redirecting to: " + redirectUri );
                response.sendRedirect( redirectUri );
                return null;
            }
        }
        else
        {
            request.setAttribute( MESSAGE_KEY, getString( "authentication_failed" ) );

            return mapping.findForward( ERROR );
        }
    }
}
