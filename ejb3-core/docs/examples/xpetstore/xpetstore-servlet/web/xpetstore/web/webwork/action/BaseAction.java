/*
 * Created on Feb 23, 2003
 */
package xpetstore.web.webwork.action;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cirrus.hibernate.Session;
import cirrus.hibernate.SessionFactory;

import webwork.action.ActionSupport;
import webwork.action.ApplicationAware;
import webwork.action.LocaleAware;
import webwork.action.SessionAware;

import xpetstore.domain.Customer;

import xpetstore.web.servlet.ActionServlet;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public abstract class BaseAction
    extends ActionSupport
    implements SessionAware,
                   ApplicationAware,
                   LocaleAware
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String BUNDLE_NAME = "Resources";
    public static final String CART_KEY = "cart";
    public static final String USERID_KEY = "userId";
    public static final String USERNAME_KEY = "username";

    //~ Instance fields --------------------------------------------------------

    protected Map    _application;
    private Session  _hibernateSession;
    protected Locale _locale;
    protected Log    _log = LogFactory.getLog( getClass(  ) );
    protected String _redirectUri = "";
    protected Map    _session;

    //~ Methods ----------------------------------------------------------------

    protected void checkLength( String key,
                                String msg,
                                String value,
                                int    length )
    {
        if ( ( value == null ) || ( value.length(  ) < length ) )
        {
            addError( key, getText( msg ) );
        }
    }

    protected void checkNotEmpty( String key,
                                  String msg,
                                  String value )
    {
        if ( ( value == null ) || ( value.length(  ) == 0 ) )
        {
            addError( key, getText( msg ) );
        }
    }

    protected void clearSession(  )
    {
        _session.remove( USERID_KEY );
        _session.remove( USERNAME_KEY );
        _session.remove( CART_KEY );
    }

    /**
     * @see webwork.action.Action#execute()
     */
    public String execute(  )
        throws Exception
    {
        _log.info( "execute()" );
        return super.execute(  );
    }

    /**
     * Returns the shoppping cart.
     * The cart is a <code>java.util.Map</code> containing the quantities of
     * the items of the shopping cart indexed by their <code>itemId</code>
     *
     * @return Map
     */
    public Map getCart(  )
    {
        Map cart = ( Map ) _session.get( CART_KEY );

        if ( cart == null )
        {
            cart = new HashMap(  );
            _session.put( CART_KEY, cart );
        }

        return cart;
    }

    public Session getHibernateSession(  )
        throws Exception
    {
        if ( _hibernateSession == null )
        {
            SessionFactory factory = ( SessionFactory ) _application.get( ActionServlet.SESSION_FACTORY_KEY );
            _hibernateSession = factory.openSession(  );
        }

        if ( !_hibernateSession.isOpen(  ) )
        {
            _hibernateSession.reconnect(  );
        }

        return _hibernateSession;
    }

    /**
     * @see webwork.action.ActionSupport#getLocale()
     */
    public Locale getLocale(  )
    {
        return _locale;
    }

    /**
     * @return String
     */
    public String getRedirectUri(  )
    {
        return _redirectUri;
    }

    /**
     * @see webwork.action.ActionSupport#getText(java.lang.String)
     */
    public String getText( String key )
    {
        try
        {
            return getTexts(  ).getString( key );
        }
        catch ( MissingResourceException m )
        {
            return "???" + key + "???";
        }
    }

    /**
     * @see webwork.action.ActionSupport#getTexts()
     */
    public ResourceBundle getTexts(  )
    {
        return ResourceBundle.getBundle( BUNDLE_NAME, getLocale(  ) );
    }

    public String getUserId(  )
    {
        return ( String ) _session.get( USERID_KEY );
    }

    protected void initSession( Customer customer )
    {
        _session.put( USERID_KEY, customer.getUserId(  ) );
        _session.put( USERNAME_KEY, customer.getFirstname(  ) + " " + customer.getLastname(  ) );

        /* TODO set the locale */
    }

    /**
     * @see webwork.action.ApplicationAware#setApplication(java.util.Map)
     */
    public void setApplication( Map application )
    {
        _application = application;
    }

    /**
      * @see webwork.action.LocaleAware#setLocale(java.util.Locale)
     */
    public void setLocale( Locale locale )
    {
        _locale = locale;
    }

    /**
     * Sets the nextUri.
     * @param nextUri The nextUri to set
     */
    public void setRedirectUri( String nextUri )
    {
        _redirectUri = nextUri;
    }

    /**
     * @see webwork.action.SessionAware#setSession(java.util.Map)
     */
    public void setSession( Map session )
    {
        _session = session;
    }
}
