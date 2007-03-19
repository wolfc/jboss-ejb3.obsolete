package xpetstore.util;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class Debug
{
    //~ Methods ----------------------------------------------------------------

    public static void print( String msg )
    {
        System.out.println( msg );
    }

    public static void print( String    msg,
                              Exception cause )
    {
        System.out.println( msg );
        cause.printStackTrace(  );
    }
}
