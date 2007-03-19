package xpetstore.domain.catalog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import xpetstore.util.Page;

import xpetstore.domain.catalog.ejb.Product;


/**
 * This is a DAO class for complex db queries that can't be
 * resolved by finders
 *
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class ProductDAO
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String SQL_FIND_BY_KEY = "SELECT productId,name,description FROM T_PRODUCT WHERE (productId LIKE ?) OR (name LIKE ?) OR (description LIKE ?)";

    //~ Instance fields --------------------------------------------------------

    private Connection _cnn;

    //~ Constructors -----------------------------------------------------------

    public ProductDAO( Connection cnn )
    {
        _cnn = cnn;
    }

    //~ Methods ----------------------------------------------------------------

    public Page findByKey( String key,
                           int    start,
                           int    count )
        throws SQLException
    {
		PreparedStatement stmt = null;
		ResultSet rs = null;
    	try
    	{
        stmt = _cnn.prepareStatement( SQL_FIND_BY_KEY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );

        stmt.setString( 1, key );
        stmt.setString( 2, key );
        stmt.setString( 3, key );

        rs = stmt.executeQuery(  );

        return toPage( rs, start, count );
    	}
    	finally
    	{
    		if ( stmt != null )
    		{
    			stmt.close();
    		}
    		if ( rs != null )
    		{
    			rs.close();
    		}
    	}
    }

    private Page toPage( ResultSet rs,
                         int       start,
                         int       count )
        throws SQLException
    {
        int       i;
        int       size;
        int       imax = start + count;
        ArrayList lst = new ArrayList(  );

        for ( i = size = 0; rs.next(  ); i++, size++ )
        {
            if ( ( i >= start ) && ( i < imax ) )
            {
                lst.add( new Product( rs.getString( 1 ), rs.getString( 2 ), rs.getString( 3 ) ) );
            }
        }

        return new Page( lst, start, ( start + count ) < size );
    }
}
