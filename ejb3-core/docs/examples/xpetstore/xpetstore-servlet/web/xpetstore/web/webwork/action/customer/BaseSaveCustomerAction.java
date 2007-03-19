/*
 * Created on Feb 25, 2003
 */
package xpetstore.web.webwork.action.customer;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import cirrus.hibernate.Hibernate;
import cirrus.hibernate.ObjectNotFoundException;
import cirrus.hibernate.Session;
import cirrus.hibernate.Transaction;

import webwork.action.ServletResponseAware;

import xpetstore.domain.Account;
import xpetstore.domain.CreditCard;
import xpetstore.domain.Customer;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public abstract class BaseSaveCustomerAction
    extends BaseAction
    implements ServletResponseAware
{
    //~ Instance fields --------------------------------------------------------

    protected Customer          _customer = new Customer(  );
    private HttpServletResponse _response;

    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        Transaction tx = null;
        Session     s = getHibernateSession(  );
        String      customerId = _customer.getUserId(  );

        try
        {
            tx = s.beginTransaction(  );

            /* Make sure that the user-id is unique */
            try
            {
                Customer c = ( Customer ) s.load( Customer.class, customerId );

                if ( !c.getUserId(  ).equalsIgnoreCase( _customer.getUserId(  ) ) )
                {
                    addError( "customer", getText( "duplicate_account" ) );

                    return ERROR;
                }
            }
            catch ( ObjectNotFoundException o ) {}

            /* Make sure that the email is unique */
            String     oql = "FROM cst IN CLASS " + Customer.class + " WHERE cst.email=?";
            Collection col = s.find( oql, _customer.getEmail(  ), Hibernate.STRING );

            if ( col.size(  ) > 0 )
            {
                Customer c = ( Customer ) col.iterator(  ).next(  );

                if ( !c.getUserId(  ).equalsIgnoreCase( customerId ) )
                {
                    addError( "customer", getText( "duplicate_email" ) );

                    return ERROR;
                }
            }

            save( _customer, s );
            tx.commit(  );

            return SUCCESS;
        }
        catch ( Exception e )
        {
            _log.error( "Unexpected error", e );

            if ( tx != null )
            {
                tx.rollback(  );
            }

            throw e;
        }
        finally
        {
            s.close(  );
        }
    }

    /**
     *
     * @see webwork.action.ActionSupport#doValidation()
     */
    protected void doValidation(  )
    {
        System.out.println( getClass(  ).getName(  ) + ".doValidation()" );

        /* Account */
        Account account = _customer.getAccount(  );
        checkLength( "customerId", "userId_length", account.getUserId(  ), 4 );
        checkLength( "password", "password_length", account.getPassword(  ), 4 );

        /* Email */
        checkNotEmpty( "email", "email_required", _customer.getEmail(  ) );

        /* Credit card */
        CreditCard cc = _customer.getCreditCard(  );
        checkNotEmpty( "ccEmail", "ccType_required", cc.getType(  ) );
        checkNotEmpty( "ccNumber", "ccNumber_required", cc.getNumber(  ) );
        checkNotEmpty( "ccExpiryDate", "ccExpiryDate_required", cc.getExpiryDate(  ) );

        super.doValidation(  );
    }

    /**
     * @return Customer
     */
    public Customer getCustomer(  )
    {
        return _customer;
    }

    public abstract void save( Customer customer,
                               Session  session )
        throws Exception;

    /**
     * Sets the customer.
     * @param customer The customer to set
     */
    public void setCustomer( Customer customer )
    {
        _customer = customer;
    }

    /**
     * @see webwork.action.ServletResponseAware#setServletResponse(javax.servlet.http.HttpServletResponse)
     */
    public void setServletResponse( HttpServletResponse response )
    {
        _response = response;
    }
}
