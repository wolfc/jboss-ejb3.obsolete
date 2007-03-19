/*
 * Created on Feb 23, 2003
 */
package xpetstore.web.servlet;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import cirrus.hibernate.Datastore;
import cirrus.hibernate.Hibernate;
import cirrus.hibernate.HibernateException;
import cirrus.hibernate.SessionFactory;

import webwork.dispatcher.ServletDispatcher;

import xpetstore.domain.Account;
import xpetstore.domain.Category;
import xpetstore.domain.Customer;
import xpetstore.domain.Item;
import xpetstore.domain.Order;
import xpetstore.domain.OrderItem;
import xpetstore.domain.Product;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @web.servlet
 *      name="action"
 *      display-name="xPetstore WebWork Action Servlet"
 *      load-on-startup="1"
 *
 * @web.servlet-mapping
 *      url-pattern="*.action"
 *
 * @web.resource-ref
 *      name="${jndi.mail.session}"
 *      type="javax.mail.Session"
 *      auth="Container"
 *
 * @jboss.resource-ref
 *      res-ref-name="${jndi.mail.session}"
 *      jndi-name="${jboss.mail.session}"
 *
 * @weblogic.resource-description
 *      res-ref-name="${jndi.mail.session}"
 *      jndi-name="${weblogic.mail.session}"
 */
public class ActionServlet
    extends ServletDispatcher
{
    //~ Static fields/initializers ---------------------------------------------

    private static final Logger __logger = Logger.getLogger( ActionServlet.class );
    public static final String  SESSION_FACTORY_KEY = "cirrus.hibernate.SessionFactory";

    //~ Methods ----------------------------------------------------------------

    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init( ServletConfig config )
        throws ServletException
    {
        super.init( config );

        __logger.info( "Initializing" );

        try
        {
            Datastore ds = Hibernate.createDatastore(  );

            /* Classes */
            load( Account.class, ds );
            load( Category.class, ds );
            load( Customer.class, ds );
            load( Item.class, ds );
            load( Order.class, ds );
            load( OrderItem.class, ds );
            load( Product.class, ds );

            /* Hibernate properties */
            __logger.info( "...loading: hibernate.properties" );

            InputStream in = getClass(  ).getClassLoader(  ).getResourceAsStream( "hibernate.properties" );
            Properties  props = new Properties(  );
            props.load( in );

            /* Session Factory */
            __logger.info( "...initializing the Hibernate SessionFactory" );

            SessionFactory sessionFactory = ds.buildSessionFactory( props );
            config.getServletContext(  ).setAttribute( SESSION_FACTORY_KEY, sessionFactory );

            __logger.info( "Initialized" );
        }
        catch ( HibernateException h )
        {
            throw new ServletException( h );
        }
        catch ( IOException io )
        {
            throw new ServletException( "Unable to load: hibernate.properties", io );
        }
    }

    private void load( Class     type,
                       Datastore ds )
        throws ServletException
    {
        try
        {
            __logger.info( "... loading mapping for: " + type );
            ds.storeClass( type );
        }
        catch ( Exception e )
        {
            throw new ServletException( "Unable to load Hibernate mapping:" + type, e );
        }
    }

    /**
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy(  )
    {
        getServletContext(  ).removeAttribute( SESSION_FACTORY_KEY );

        super.destroy(  );
    }
}
