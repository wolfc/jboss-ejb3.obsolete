/*
 * Created on 25-Feb-2003
 */
package xpetstore.web.webwork.action.customer;

import cirrus.hibernate.Session;

import xpetstore.domain.Customer;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *      name="updateCustomer"
 *      success="index.action"
 *      error="myaccount.vm"
 */
public class UpdateCustomerAction
    extends BaseSaveCustomerAction
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        String result = super.doExecute(  );

        if ( SUCCESS.equals( result ) )
        {
            initSession( _customer );
        }

        return result;
    }

    /**
     * @see xpetstore.web.webwork.action.customer.BaseSaveCustomerAction#save(xpetstore.domain.Customer, cirrus.hibernate.Session)
     */
    public void save( Customer customer,
                      Session  session )
        throws Exception
    {
        Customer cst = ( Customer ) session.load( Customer.class, customer.getUserId(  ) );
        cst.set( customer );
        session.update( cst );
    }
}
