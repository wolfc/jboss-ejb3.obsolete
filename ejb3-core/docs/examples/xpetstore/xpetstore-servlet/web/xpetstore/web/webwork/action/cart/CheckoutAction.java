/*
 * Created on Feb 25, 2003
 */
package xpetstore.web.webwork.action.cart;

import cirrus.hibernate.Session;

import xpetstore.domain.Customer;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="checkout"
 *   success="checkout.vm"
 */
public class CheckoutAction
    extends BaseCartAction
{
    //~ Instance fields --------------------------------------------------------

    private Customer _customer = new Customer(  );

    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        Session s = getHibernateSession(  );

        try
        {
            _customer = ( Customer ) s.load( Customer.class, getUserId(  ) );

            return SUCCESS;
        }
        finally
        {
            s.close(  );
        }
    }

    /**
     * @return Customer
     */
    public Customer getCustomer(  )
    {
        return _customer;
    }

    /**
     * Sets the customer.
     * @param customer The customer to set
     */
    public void setCustomer( Customer customer )
    {
        _customer = customer;
    }
}
