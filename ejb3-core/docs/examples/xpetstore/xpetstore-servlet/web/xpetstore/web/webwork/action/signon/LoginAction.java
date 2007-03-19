/*
 * Created on 24-Feb-2003
 */
package xpetstore.web.webwork.action.signon;

import javax.servlet.http.HttpServletResponse;

import cirrus.hibernate.ObjectNotFoundException;
import cirrus.hibernate.Session;

import webwork.action.ServletResponseAware;

import xpetstore.domain.Customer;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="login"
 *   success="index.action"
 *   error="signon.vm"
 */
public class LoginAction
    extends BaseAction
    implements ServletResponseAware
{
    //~ Instance fields --------------------------------------------------------

    private String              _password;
    private HttpServletResponse _response;
    private String              _userId;

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
            Customer c = ( Customer ) s.load( Customer.class, _userId );

            if ( c.getAccount(  ).matchPassword( _password ) )
            {
                initSession( c );

                /* Redirect */
                if ( ( _redirectUri != null ) && ( _redirectUri.length(  ) > 0 ) )
                {
                    _response.sendRedirect( _redirectUri );
                    return NONE;
                }

                /* Normal flow */
                return SUCCESS;
            }
            else
            {
                addError( "login", getText( "authentication_failed" ) );
                return ERROR;
            }
        }
        catch ( ObjectNotFoundException o )
        {
            addError( "login", getText( "authentication_failed" ) );

            return ERROR;
        }
        finally
        {
            s.close(  );
        }
    }

    /**
     * @return String
     */
    public String getPassword(  )
    {
        return _password;
    }

    /**
     * @return String
     */
    public String getUserId(  )
    {
        return _userId;
    }

    /**
     * Sets the password.
     * @param password The password to set
     */
    public void setPassword( String password )
    {
        _password = password;
    }

    /**
     * @see webwork.action.ServletRequestAware#setServletRequest(javax.servlet.http.HttpServletRequest)
     */
    public void setServletResponse( HttpServletResponse response )
    {
        _response = response;
    }

    /**
     * Sets the login.
     * @param login The login to set
     */
    public void setUserId( String login )
    {
        _userId = login;
    }
}
