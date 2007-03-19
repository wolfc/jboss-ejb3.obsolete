/*
 * Created on 26-Feb-2003
 */
package xpetstore.web.filter;

import java.io.IOException;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xpetstore.web.struts.action.BaseAction;


/**
 * This filter protects some URI and make sure that only signed-on users
 * can access them
 *
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @web.filter
 *      name="signon"
 *      display-name="xPetstore Struts Signon Filter"
 *
 * @web.filter-mapping
 *      servlet-name="action"
 *
 * @web.filter-init-param
 *      name="signon.action"
 *         value="/signon.jspa"
 *
 * @web.filter-init-param
 *      name="protected.uri"
 *         value="checkout.jspa,order.jspa"
 */
public class SignOnFilter
    implements Filter
{
    //~ Static fields/initializers ---------------------------------------------

    private static final Log __log = LogFactory.getLog( SignOnFilter.class );

    //~ Instance fields --------------------------------------------------------

    private String       _signon;
    private HashMap      _protectedUris = new HashMap(  );
    private FilterConfig _config;

    //~ Methods ----------------------------------------------------------------

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init( FilterConfig config )
        throws ServletException
    {
        __log.info( "init()" );

        _config = config;

        /* SignOn action */
        _signon = config.getInitParameter( "signon.action" );
        __log.info( "...signon.action=" + _signon );

        /* Protected Uri */
        String          uri = config.getInitParameter( "protected.uri" );
        StringTokenizer tok = new StringTokenizer( uri, "," );

        while ( tok.hasMoreTokens(  ) )
        {
            String url = tok.nextToken(  );
            _protectedUris.put( url, url );

            __log.info( "...Adding URI to protect: " + url );
        }
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest  request,
                          ServletResponse response,
                          FilterChain     chain )
        throws IOException, 
                   ServletException
    {
        __log.info( "doFilter()" );

        HttpServletRequest req = ( HttpServletRequest ) request;

        /* Uri */
        String uri = req.getRequestURI(  );

        if ( uri.startsWith( "/" ) )
        {
            uri = uri.substring( 1 );
        }

        int i = uri.indexOf( "/" );

        if ( i >= 0 )
        {
            uri = uri.substring( i + 1 );
        }

        __log.info( "...uri=" + uri );

        /* check if signon is required */
        if ( isProtected( uri ) && !isSignedIn( req ) )
        {
            __log.info( "...User not logged in. Redirecting to: " + _signon );

            String forward = _signon + "?redirectUri=" + uri;
            _config.getServletContext(  ).getRequestDispatcher( forward ).forward( request, response );
        }
        else
        {
            chain.doFilter( request, response );
        }
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy(  )
    {
        _protectedUris.clear(  );
    }

    public boolean isProtected( String uri )
    {
        return ( _protectedUris.get( uri ) != null );
    }

    public boolean isSignedIn( HttpServletRequest request )
    {
        HttpSession session = request.getSession( false );

        return ( session == null )
               ? false
               : ( session.getAttribute( BaseAction.USERID_KEY ) != null );
    }
}
