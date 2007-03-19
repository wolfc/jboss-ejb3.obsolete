/*
 * Created on 25-Feb-2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 */
package xpetstore.web.webwork.action.customer;

import cirrus.hibernate.Session;

import xpetstore.domain.Customer;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *      name="myaccount"
 *      success="myaccount.vm"
 */
public class MyAccountAction
    extends BaseAction
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
