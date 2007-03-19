package xpetstore.web.struts.action.customer;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.customer.ejb.Customer;
import xpetstore.domain.signon.ejb.Account;

import xpetstore.web.struts.action.*;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.form
 *      name="customerForm"
 */
public class CustomerForm
    extends BaseForm
{
    //~ Instance fields --------------------------------------------------------

    private Customer _customer = new Customer(  );

    //~ Constructors -----------------------------------------------------------

    public CustomerForm(  )
    {
        _customer.setAccount( new Account(  ) );
    }

    //~ Methods ----------------------------------------------------------------

    public ActionErrors validate( ActionMapping      mapping,
                                  HttpServletRequest request )
    {
        ActionErrors errors = new ActionErrors(  );

        Account account = getCustomer(  ).getAccount(  );
        String       userId = account.getUserId(  );
        String       passwd = account.getPassword(  );
        checkNotEmpty( userId, "userId_required", errors );
        checkLength( userId, 4, "userId_length", errors );
        checkNotEmpty( passwd, "password_required", errors );
        checkLength( passwd, 4, "password_length", errors );

        checkNotEmpty( _customer.getEmail(  ), "email_required", errors );
        checkNotEmpty( _customer.getCreditCardType(  ), "ccType_required", errors );
        checkNotEmpty( _customer.getCreditCardNumber(  ), "ccNumber_required", errors );
        checkNotEmpty( _customer.getCreditCardExpiryDate(  ), "ccExpiryDate_required", errors );
        checkCreditCardDateFormat( _customer.getCreditCardExpiryDate(  ), "ccExpiryDate_bad_format", errors );

        return errors;
    }

    public Customer getCustomer(  )
    {
        return _customer;
    }

    public void setCustomer( Customer customer )
    {
        _customer = customer;
    }
}
