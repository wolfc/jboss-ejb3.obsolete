package xpetstore.services.petstore.exceptions;

import xpetstore.util.ChainedException;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class DuplicateAccountException
    extends ChainedException
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for DuplicateAccountException.
     */
    public DuplicateAccountException(  )
    {
        super(  );
    }

    /**
     * Constructor for DuplicateAccountException.
     * @param message
     */
    public DuplicateAccountException( String message )
    {
        super( message );
    }

    /**
     * Constructor for DuplicateAccountException.
     * @param message
     * @param cause
     */
    public DuplicateAccountException( String    message,
                                      Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Constructor for DuplicateAccountException.
     * @param cause
     */
    public DuplicateAccountException( Throwable cause )
    {
        super( cause );
    }
}
