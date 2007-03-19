package xpetstore.services.petstore.exceptions;

import xpetstore.util.ChainedException;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class DuplicateEmailException
    extends ChainedException
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for DuplicateEmailException.
     */
    public DuplicateEmailException(  )
    {
        super(  );
    }

    /**
     * Constructor for DuplicateEmailException.
     * @param message
     */
    public DuplicateEmailException( String message )
    {
        super( message );
    }

    /**
     * Constructor for DuplicateEmailException.
     * @param message
     * @param cause
     */
    public DuplicateEmailException( String    message,
                                    Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Constructor for DuplicateEmailException.
     * @param cause
     */
    public DuplicateEmailException( Throwable cause )
    {
        super( cause );
    }
}
