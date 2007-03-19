/*
 * Created on 24-Feb-2003
 */
package xpetstore.web.webwork.action.customer;

import xpetstore.domain.Customer;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="register"
 *   success="register.vm"
 */
public class RegisterAction
    extends BaseAction
{
    //~ Instance fields --------------------------------------------------------

    private Customer _customer = new Customer(  );

    //~ Methods ----------------------------------------------------------------

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
