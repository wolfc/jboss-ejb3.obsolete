/*
 * Created on Feb 22, 2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 */
package xpetstore.domain;

import java.io.Serializable;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class CreditCard
    implements Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String VISA = "Visa";
    public static final String MASTERCARD = "MasterCard";
    public static final String AMEX = "American Express";

    //~ Instance fields --------------------------------------------------------

    private String _number = "";
    private String _type = "";
    private String _expiryDate = "";

    //~ Constructors -----------------------------------------------------------

    public CreditCard(  )
    {
        super(  );
    }

    public CreditCard( CreditCard cc )
    {
        set( cc );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @return String
     *
     * @hibernate.property
     *         column="creditCardExpiryDate"
     *      length="10"
     */
    public String getExpiryDate(  )
    {
        return _expiryDate;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         column="creditCardNumber"
     *      length="25"
     */
    public String getNumber(  )
    {
        return _number;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *         column="creditCardType"
     *      length="25"
     */
    public String getType(  )
    {
        return _type;
    }

    public void set( CreditCard cc )
    {
        _type       = cc.getType(  );
        _number     = cc.getNumber(  );
        _expiryDate = cc.getExpiryDate(  );
    }

    /**
     * Sets the expiryDate.
     * @param expiryDate The expiryDate to set
     */
    public void setExpiryDate( String expiryDate )
    {
        _expiryDate = expiryDate;
    }

    /**
     * Sets the number.
     * @param number The number to set
     */
    public void setNumber( String number )
    {
        _number = number;
    }

    /**
     * Sets the type.
     * @param type The type to set
     */
    public void setType( String type )
    {
        _type = type;
    }
}
