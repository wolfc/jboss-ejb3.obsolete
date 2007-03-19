/*
 * Created on Feb 22, 2003
 */
package xpetstore.domain;

import java.io.Serializable;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @hibernate.class
 *         table="T_CUSTOMER"
 */
public class Customer
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private Account    _account = new Account(  );
    private Address    _address = new Address(  );
    private CreditCard _creditCard = new CreditCard(  );
    private String     _email = "";
    private String     _firstname = "";
    private String     _language = "";
    private String     _lastname = "";
    private String     _telephone = "";

    //~ Constructors -----------------------------------------------------------

    public Customer(  ) {}

    public Customer( Account account )
    {
        _account = account;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @return Account
     *
     * @hibernate.one-to-one
     *         cascade="all"
     */
    public Account getAccount(  )
    {
        return _account;
    }

    /**
     * @return Address
     *
     * @hibernate.component
     */
    public Address getAddress(  )
    {
        return _address;
    }

    /**
     * @return CreditCard
     *
     * @hibernate.component
     */
    public CreditCard getCreditCard(  )
    {
        return _creditCard;
    }

    /**
     * @return String
     *
     * @hibernate.id
     *        generator-class="assigned"
     *         length="10"
     */
    public String getUserId(  )
    {
        return ( _account != null )
               ? _account.getUserId(  )
               : "";
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         length="255"
     *         not-null="true"
     *         unique="true"
     */
    public String getEmail(  )
    {
        return _email;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *      length="50"
     */
    public String getFirstname(  )
    {
        return _firstname;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *      length="3"
     */
    public String getLanguage(  )
    {
        return _language;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *      length="50"
     */
    public String getLastname(  )
    {
        return _lastname;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *      length="10"
     */
    public String getTelephone(  )
    {
        return _telephone;
    }

    public void set( Customer cst )
    {
        _account.set( cst.getAccount(  ) );
        _address.set( cst.getAddress(  ) );
        _creditCard.set( cst.getCreditCard(  ) );
        _email     = cst.getEmail(  );
        _firstname = cst.getFirstname(  );
        _lastname  = cst.getLastname(  );
        _language  = cst.getLanguage(  );
        _telephone = cst.getTelephone(  );
    }

    /**
     * Sets the account.
     * @param account The account to set
     */
    public void setAccount( Account account )
    {
        _account = account;
    }

    /**
     * Sets the address.
     * @param address The address to set
     */
    public void setAddress( Address address )
    {
        _address = address;
    }

    /**
     * Sets the creditCard.
     * @param creditCard The creditCard to set
     */
    public void setCreditCard( CreditCard creditCard )
    {
        _creditCard = creditCard;
    }

    /**
     * Sets the userId.
     * @param userId The userId to set
     */
    public void setUserId( String userId )
    {
        if ( _account == null )
        {
            _account = new Account(  );
        }

        _account.setUserId( userId );
    }

    /**
     * Sets the email.
     * @param email The email to set
     */
    public void setEmail( String email )
    {
        _email = email;
    }

    /**
     * Sets the firstname.
     * @param firstname The firstname to set
     */
    public void setFirstname( String firstname )
    {
        _firstname = firstname;
    }

    /**
     * Sets the language.
     * @param language The language to set
     */
    public void setLanguage( String language )
    {
        _language = language;
    }

    /**
     * Sets the lastname.
     * @param lastname The lastname to set
     */
    public void setLastname( String lastname )
    {
        _lastname = lastname;
    }

    /**
     * Sets the telephone.
     * @param telephone The telephone to set
     */
    public void setTelephone( String telephone )
    {
        _telephone = telephone;
    }
}
