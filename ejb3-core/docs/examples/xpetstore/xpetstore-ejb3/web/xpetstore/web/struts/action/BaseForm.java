package xpetstore.web.struts.action;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class BaseForm
    extends ActionForm
{
    //~ Instance fields --------------------------------------------------------

    private String _redirectUri = "";

    //~ Methods ----------------------------------------------------------------

    public void checkCreditCardDateFormat( String       value,
                                           String       msg,
                                           ActionErrors errors )
    {
        boolean err = false;

        try
        {
            int i = value.indexOf( "-" );

            if ( ( err = ( i >= 0 ) ) )
            {
                int lhs = Integer.parseInt( value.substring( 0, i ) );
                int rhs = Integer.parseInt( value.substring( i + 1 ) );
                err = !( ( lhs >= 1 ) && ( lhs <= 12 ) && ( rhs >= 0 ) );
            }
        }
        catch ( Exception e )
        {
            err = true;
        }

        if ( err )
        {
            errors.add( "ActionForm", new ActionError( msg ) );
        }
    }

    public void checkLength( String       value,
                             int          len,
                             String       msg,
                             ActionErrors errors )
    {
        if ( ( value == null ) || ( value.length(  ) < len ) )
        {
            errors.add( "ActionForm", new ActionError( msg ) );
        }
    }

    public void checkNotEmpty( String       value,
                               String       msg,
                               ActionErrors errors )
    {
        if ( ( value == null ) || ( value.trim(  ).length(  ) == 0 ) )
        {
            errors.add( "ActionForm", new ActionError( msg ) );
        }
    }

    /**
     * @return String
     */
    public String getRedirectUri(  )
    {
        return _redirectUri;
    }

    /**
     * Sets the redirectUri.
     * @param redirectUri The redirectUri to set
     */
    public void setRedirectUri( String redirectUri )
    {
        _redirectUri = redirectUri;
    }
}
