package xpetstore.web.struts.action.signon;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.signon.ejb.Account;

import xpetstore.web.struts.action.*;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.form
 *      name="signonForm"
 */
public class SignonForm
    extends BaseForm
{
    //~ Instance fields --------------------------------------------------------

    private Account _account = new Account(  );

    //~ Methods ----------------------------------------------------------------

    public ActionErrors validate( ActionMapping      mapping,
                                  HttpServletRequest request )
    {
        ActionErrors errors = new ActionErrors(  );

        checkNotEmpty( _account.getUserId(  ), "userId_required", errors );
        checkNotEmpty( _account.getPassword(  ), "password_required", errors );

        return errors;
    }

    public Account getAccount(  )
    {
        return _account;
    }

    public void setAccount( Account account )
    {
        _account = account;
    }
}
