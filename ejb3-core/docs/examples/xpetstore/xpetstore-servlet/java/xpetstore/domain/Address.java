/*
 * Created on Feb 22, 2003
 */
package xpetstore.domain;

import java.io.Serializable;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class Address
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private String _city = "";
    private String _country = "";
    private String _state = "";
    private String _street1 = "";
    private String _street2 = "";
    private String _zipcode = "";

    //~ Constructors -----------------------------------------------------------

    public Address(  )
    {
        super(  );
    }

    public Address( Address addr )
    {
        set( addr );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @return String
     *
     * @hibernate.property
     *         length="25"
     */
    public String getCity(  )
    {
        return _city;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         length="3"
     */
    public String getCountry(  )
    {
        return _country;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         length="3"
     */
    public String getState(  )
    {
        return _state;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         length="50"
     */
    public String getStreet1(  )
    {
        return _street1;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         length="50"
     */
    public String getStreet2(  )
    {
        return _street2;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         length="10"
     */
    public String getZipcode(  )
    {
        return _zipcode;
    }

    public void set( Address address )
    {
        _street1 = address.getStreet1(  );
        _street2 = address.getStreet2(  );
        _city    = address.getCity(  );
        _state   = address.getState(  );
        _country = address.getCountry(  );
        _zipcode = address.getZipcode(  );
    }

    /**
     * Sets the city.
     * @param city The city to set
     */
    public void setCity( String city )
    {
        _city = city;
    }

    /**
     * Sets the country.
     * @param country The country to set
     */
    public void setCountry( String country )
    {
        _country = country;
    }

    /**
     * Sets the state.
     * @param state The state to set
     */
    public void setState( String state )
    {
        _state = state;
    }

    /**
     * Sets the street1.
     * @param street1 The street1 to set
     */
    public void setStreet1( String street1 )
    {
        _street1 = street1;
    }

    /**
     * Sets the street2.
     * @param street2 The street2 to set
     */
    public void setStreet2( String street2 )
    {
        _street2 = street2;
    }

    /**
     * Sets the zipcode.
     * @param zipcode The zipcode to set
     */
    public void setZipcode( String zipcode )
    {
        _zipcode = zipcode;
    }
}
