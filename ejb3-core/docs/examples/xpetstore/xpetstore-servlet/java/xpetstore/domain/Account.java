/*
 * Generated file - Do not edit!
 */
package xpetstore.domain;

import java.io.Serializable;


/**
 *
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @hibernate.class
 *         table="T_ACCOUNT"
 */
public class Account
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private String _userId = "";
    private String _password = "";

    //~ Constructors -----------------------------------------------------------

    public Account(  )
    {
        super(  );
    }

    //~ Methods ----------------------------------------------------------------

    public boolean matchPassword( String password )
    {
        return ( password == null )
               ? ( _password == null )
               : password.equals( _password );
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         column="pwd"
     *      length="10"
     *      not-null="true"
     */
    public String getPassword(  )
    {
        return _password;
    }

    /**
     * @return String
     *
     * @hibernate.id
     *      generator-class="assigned"
     *      length="10"
     */
    public String getUserId(  )
    {
        return _userId;
    }

    public void set( Account account )
    {
        _password = account.getPassword(  );
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
     * Sets the userId.
     * @param userId The userId to set
     */
    public void setUserId( String userId )
    {
        _userId = userId;
    }
}
