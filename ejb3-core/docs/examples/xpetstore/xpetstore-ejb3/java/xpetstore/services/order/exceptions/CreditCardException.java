package xpetstore.services.order.exceptions;

import xpetstore.util.ChainedException;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class CreditCardException
    extends ChainedException
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for CreditCardException.
     */
    public CreditCardException(  )
    {
        super(  );
    }

    /**
     * Constructor for CreditCardException.
     * @param message
     */
    public CreditCardException( String message )
    {
        super( message );
    }

    /**
     * Constructor for CreditCardException.
     * @param message
     * @param cause
     */
    public CreditCardException( String    message,
                                Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Constructor for CreditCardException.
     * @param cause
     */
    public CreditCardException( Throwable cause )
    {
        super( cause );
    }
}
