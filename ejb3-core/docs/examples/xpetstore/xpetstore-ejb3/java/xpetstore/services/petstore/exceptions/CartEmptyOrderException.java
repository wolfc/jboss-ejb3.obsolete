package xpetstore.services.petstore.exceptions;

import xpetstore.util.ChainedException;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class CartEmptyOrderException
    extends ChainedException
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for ShoppingCartEmptyOrderException.
     */
    public CartEmptyOrderException(  )
    {
        super(  );
    }

    /**
     * Constructor for ShoppingCartEmptyOrderException.
     * @param message
     */
    public CartEmptyOrderException( String message )
    {
        super( message );
    }

    /**
     * Constructor for ShoppingCartEmptyOrderException.
     * @param message
     * @param cause
     */
    public CartEmptyOrderException( String    message,
                                    Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Constructor for ShoppingCartEmptyOrderException.
     * @param cause
     */
    public CartEmptyOrderException( Throwable cause )
    {
        super( cause );
    }
}
