package xpetstore.web.struts.action.customer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.customer.ejb.Customer;

import xpetstore.services.petstore.exceptions.DuplicateEmailException;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="customerForm"
 *      path="/updateCustomer"
 *      scope="request"
 *      validate="true"
 *      input="/myaccount.jsp"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/index.jsp"
 *
 * @struts.action-forward
 *      name="error"
 *      path="/myaccount.jsp"
 */
public class UpdateCustomerAction
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
        CustomerForm  frm = ( CustomerForm ) form;
        Customer cust = frm.getCustomer(  );

        try
        {
            getPetstore(  ).updateCustomer( cust );

            initSession( cust, request );

            return mapping.findForward( SUCCESS );
        }
        catch ( DuplicateEmailException de )
        {
            request.setAttribute( MESSAGE_KEY, "duplicate_email" );

            return mapping.findForward( ERROR );
        }
    }
}
