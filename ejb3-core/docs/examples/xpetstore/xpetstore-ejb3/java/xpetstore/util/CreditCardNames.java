package xpetstore.util;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class CreditCardNames
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String     VISA = "Visa";
    public static final String     MASTERCARD = "MasterCard";
    public static final String     AMEX = "American Express";
    private static final ArrayList __all;

    static
    {
        __all = new ArrayList(  );
        __all.add( VISA );
        __all.add( MASTERCARD );
        __all.add( AMEX );
    }

    //~ Methods ----------------------------------------------------------------

    public static Collection all(  )
    {
        return __all;
    }
}
