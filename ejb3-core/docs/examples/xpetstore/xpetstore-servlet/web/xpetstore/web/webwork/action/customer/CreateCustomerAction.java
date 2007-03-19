/*
 * Created on Feb 25, 2003
 */
package xpetstore.web.webwork.action.customer;

import cirrus.hibernate.Session;

import xpetstore.domain.Customer;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *      name="createCustomer"
 *      success="signon.action"
 *      error="register.vm"
 */
public class CreateCustomerAction
    extends BaseSaveCustomerAction
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see xpetstore.web.webwork.action.customer.BaseSaveCustomerAction#save(xpetstore.domain.Customer, cirrus.hibernate.Session)
     */
    public void save( Customer customer,
                      Session  session )
        throws Exception
    {
        session.save( customer );
    }
}
