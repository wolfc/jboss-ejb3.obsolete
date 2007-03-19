package xpetstore.services.petstore.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.InitialContext;

import javax.ejb.FinderException;

import junit.framework.TestCase;

import xpetstore.domain.catalog.ejb.Category;
import xpetstore.domain.catalog.ejb.Item;
import xpetstore.domain.catalog.ejb.Product;
import xpetstore.domain.customer.ejb.Customer;
import xpetstore.domain.order.ejb.OrderItem;
import xpetstore.domain.order.ejb.Order;
import xpetstore.domain.signon.ejb.Account;

import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.util.Page;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class PetstoreTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static Petstore _petstore;

    //~ Constructors -----------------------------------------------------------

    public PetstoreTest( String arg0 )
    {
        super( arg0 );
    }

    //~ Methods ----------------------------------------------------------------

    //================================================
    // signon Test cases
    //================================================
    public void testAuthenticate(  )
    {
        boolean result;

        try
        {
            result = _petstore.authenticate( "user1", "password1" );
            assertEquals( "Unable to authenticate 'user1'", true, result );
        }
        catch ( Exception e )
        {
            fail( "Unable to authenticate. Error=" + e.toString(  ) );
        }
    }

    //================================================
    // Customer Test cases
    //================================================    
    public void testCreateCustomer(  )
    {
        try
        {
            String        userId = "CST-" + ( System.currentTimeMillis(  ) % 100000 );
            Account  act0 = new Account( userId, "password" );
            Customer cst0 = new Customer( null, "firstname", "lastname", userId + "@foo.com", "123-456", "en", "street1.1", "street1.2", "city", "CA", "A1A-1A1", "US", "111-111-111", "Visa", "01-11" );

            //System.out.println( "creating customer=" + cst0 );
            String        id = _petstore.createCustomer( cst0 );
            Customer cst = _petstore.getCustomer( id );
            Account  act = cst.getAccount(  );

            assertEquals( "firstname", "firstname", cst.getFirstname(  ) );
            assertEquals( "lastname", "lastname", cst.getLastname(  ) );
            assertEquals( "telephone", "123-456", cst.getTelephone(  ) );
            assertEquals( "email", userId + "@foo.com", cst.getEmail(  ) );
            assertEquals( "language", "en", cst.getLanguage(  ) );
            assertEquals( "street1", "street1.1", cst.getStreet1(  ) );
            assertEquals( "street2", "street1.2", cst.getStreet2(  ) );
            assertEquals( "city", "city", cst.getCity(  ) );
            assertEquals( "zipcode", "A1A-1A1", cst.getZipcode(  ) );
            assertEquals( "state", "CA", cst.getState(  ) );
            assertEquals( "country", "US", cst.getCountry(  ) );
            assertEquals( "creditCardType", "Visa", cst.getCreditCardType(  ) );
            assertEquals( "creditCardNumber", "111-111-111", cst.getCreditCardNumber(  ) );
            assertEquals( "creditCardExpiryDate", "01-11", cst.getCreditCardExpiryDate(  ) );
            assertEquals( "account.userId", userId, act.getUserId(  ) );
            assertEquals( "account.password", "password", act.getPassword(  ) );
        }
        catch ( Exception e )
        {
            fail( "Unable to create the user. Error=" + e.toString(  ) );
        }
    }

    public void testUpdateCustomer(  )
    {
        try
        {
            String        userId = "CST-" + ( System.currentTimeMillis(  ) % 1000000 );
            Account  act0 = new Account( userId, "password" );
            Customer cst0 = new Customer( null, "firstnam", "lastnam", userId + "@foo.com", "222-22", "f", "street2.", "street2.", "city", "O", "A1A-1A1", "C", "111-111-111", "Visa", "01-21" );

            //System.out.println( "creating customer=" + cst0 );
            String        id = _petstore.createCustomer( cst0 );

            Account  act1 = new Account( userId, "password1" );
            Customer cst1 = new Customer( id, "firstname2", "lastname2", userId + "-2@foo.com", "222-222", "fr", "street2.1", "street2.2", "city2", "ON", "A2A-2A2", "CA", "222-222-222", "Amex", "02-22" );
            cst1.setAccount( act1 );

            //System.out.println( "updating customer=" + cst1 );
            _petstore.updateCustomer( cst1 );

            Customer cst = _petstore.getCustomer( id );
            Account  act = cst.getAccount(  );

            //System.out.println( "customer=" + cst );
            assertEquals( "firstname", "firstname2", cst.getFirstname(  ) );
            assertEquals( "lastname", "lastname2", cst.getLastname(  ) );
            assertEquals( "telephone", "222-222", cst.getTelephone(  ) );
            assertEquals( "email", userId + "-2@foo.com", cst.getEmail(  ) );
            assertEquals( "language", "fr", cst.getLanguage(  ) );
            assertEquals( "street1", "street2.1", cst.getStreet1(  ) );
            assertEquals( "street2", "street2.2", cst.getStreet2(  ) );
            assertEquals( "city", "city2", cst.getCity(  ) );
            assertEquals( "zipcode", "A2A-2A2", cst.getZipcode(  ) );
            assertEquals( "state", "ON", cst.getState(  ) );
            assertEquals( "country", "CA", cst.getCountry(  ) );
            assertEquals( "creditCardType", "Amex", cst.getCreditCardType(  ) );
            assertEquals( "creditCardNumber", "222-222-222", cst.getCreditCardNumber(  ) );
            assertEquals( "creditCardExpiryDate", "02-22", cst.getCreditCardExpiryDate(  ) );
            assertEquals( "account.userId", userId, act.getUserId(  ) );
            assertEquals( "account.password", "password1", act.getPassword(  ) );
        }
        catch ( Exception e )
        {
            fail( "Unable to create the customer. Error=" + e.toString(  ) );
        }
    }

    //================================================
    // Categories Test cases
    //================================================    
    public void testGetCategory(  )
    {
        try
        {
            Category cat = _petstore.getCategory( "FISH" );
            assertNotNull( "category[FISH] not found", cat );
            assertEquals( "categoryId", "FISH", cat.getCategoryId(  ) );
            assertEquals( "name", "Fish", cat.getName(  ) );
            assertEquals( "description", "description of FISH", cat.getDescription(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    public void testGetCategories(  )
    {
        try
        {
            Page pg = _petstore.getCategories( 0, 10 );
            assertNotNull( "page", pg );
            assertEquals( "page.size", 5, pg.getSize(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    //================================================
    // Products Test cases
    //================================================    
    public void testGetProduct(  )
    {
        try
        {
            Product prod = _petstore.getProduct( "K9-DL-01" );
            assertNotNull( "product[K9-DL-01] not found", prod );
            assertEquals( "productId", "K9-DL-01", prod.getProductId(  ) );
            assertEquals( "name", "Dalmation", prod.getName(  ) );
            assertEquals( "description", "Great dog for a fire station", prod.getDescription(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    public void testGetProducts(  )
    {
        try
        {
            Page pg = _petstore.getProducts( "FISH", 0, 10 );
            assertNotNull( "page", pg );
            assertEquals( "page.size", 4, pg.getSize(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    public void testSearchProducts(  )
    {
        try
        {
            Page pg = _petstore.searchProducts( "fish", 0, 10 );
            assertNotNull( "page", pg );
            assertEquals( "page.size", 4, pg.getSize(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    //================================================
    // Products Test cases
    //================================================    
    public void testGetItem(  )
    {
        try
        {
            Item item = _petstore.getItem( "EST-1" );
            assertNotNull( "Item[EST-1] not found", item );
            assertEquals( "productId", "EST-1", item.getItemId(  ) );
            assertEquals( "name", "Large", item.getDescription(  ) );
            assertEquals( "listPrice", 16.50, item.getListPrice(  ), 0 );
            assertEquals( "unitCost", 10.00, item.getUnitCost(  ), 0 );
            assertEquals( "itemPath", "fish1.jpg", item.getImagePath(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    public void testGetItems(  )
    {
        try
        {
            Page pg = _petstore.getItems( "FI-SW-02", 0, 10 );
            assertNotNull( "page", pg );
            assertEquals( "page.size", 2, pg.getSize(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    //================================================
    // Order Test cases
    //================================================    
    public void testOrder(  )
    {
        try
        {
            // Fill the cart 
            HashMap map = new HashMap(  );
            map.put( "EST-1", new Integer( 10 ) );
            map.put( "EST-10", new Integer( 1 ) );

            // Customer 
            String userId = "user1";

            // Order
            Date       now = new Date(  );
            Integer    orderUId = _petstore.createOrder( userId, now, map );
            Order order = _petstore.getOrder( orderUId );

            //System.out.println( "order=" + order );
            assertNotNull( "order=null", order );
            assertEquals( "order.orderUID", orderUId, order.getOrderUId(  ) );
            assertNotNull( "order.date=null", order.getOrderDate(  ) );
            assertEquals( "order.date", now.getTime(  ) / 100000, order.getOrderDate(  ).getTime(  ) / 100000, 1000 );
            assertEquals( "street1", "street1.1", order.getStreet1(  ) );
            assertEquals( "street2", "street1.2", order.getStreet2(  ) );
            assertEquals( "city", "city1", order.getCity(  ) );
            assertEquals( "state", "ST1", order.getState(  ) );
            assertEquals( "zipcode", "A1B-1C1", order.getZipcode(  ) );
            assertEquals( "country", "US", order.getCountry(  ) );
            assertEquals( "creditCardType", "Visa", order.getCreditCardType(  ) );
            assertEquals( "creditCardNumber", "111-111-111", order.getCreditCardNumber(  ) );
            assertEquals( "creditCardExpiryDate", "01-11", order.getCreditCardExpiryDate(  ) );
            assertNotNull( "order.status=null", order.getStatus(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    public void testOrderItems(  )
    {
        try
        {
            // Fill the cart 
            HashMap map = new HashMap(  );
            map.put( "EST-1", new Integer( 10 ) );
            map.put( "EST-10", new Integer( 1 ) );

            // Customer 
            String userId = "user1";

            // Order
            Date    now = new Date(  );
            Integer orderUId = _petstore.createOrder( userId, now, map );

            // Order items 
            Page page = _petstore.getOrderItems( orderUId, 0, Integer.MAX_VALUE );
            assertEquals( "page.size", 2, page.getSize(  ) );

            Iterator it = page.getList(  ).iterator(  );

            for ( int i = 0; it.hasNext(  ); )
            {
                String         prefix = "order#" + i;
                OrderItem value = ( OrderItem ) it.next(  );
                Item      item = value.getItem(  );

                assertNotNull( prefix + ".item", value.getItem(  ) );

                if ( "EST-1".equals( item.getItemId(  ) ) )
                {
                    assertEquals( prefix + ".quantity", 10, value.getQuantity(  ) );
                    assertEquals( prefix + ".unitPrice", 16.5, value.getUnitPrice(  ), 0 );
                }
                else if ( "EST-10".equals( item.getItemId(  ) ) )
                {
                    assertEquals( prefix + ".quantity", 1, value.getQuantity(  ) );
                    assertEquals( prefix + ".unitPrice", 28.5, value.getUnitPrice(  ), 0 );
                }
                else
                {
                    fail( prefix + ".id is invalid: " + item.getItemId(  ) );
                }
            }
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    public void testMyOrders(  )
    {
        try
        {
            // Fill the cart 
            HashMap map = new HashMap(  );
            map.put( "EST-1", new Integer( 10 ) );
            map.put( "EST-10", new Integer( 1 ) );

            // Customer 
            String        userId = "CST-" + ( System.currentTimeMillis(  ) % 1000000 );
            Account  act0 = new Account( userId, "password" );
            Customer cst0 = new Customer( null, "firstname", "lastname", userId + "@localdomain", "123-456", "en", "street1.1", "street1.2", "city", "CA", "A1A-1A1", "US", "111-111-111", "Visa", "01-01-11" );
            cst0.setAccount( act0 );
            _petstore.createCustomer( cst0 );

            // Order
            Date now = new Date(  );
            _petstore.createOrder( userId, now, map );

            _petstore.createOrder( userId, now, map );

            Page orders = _petstore.getCustomerOrders( userId, 0, 10 );
            assertNotNull( "customer[" + userId + "] - orders=null", orders );
            assertEquals( "customer[" + userId + "] - orders.size", 1, orders.getSize(  ) );
        }
        catch ( Exception e )
        {
            fail( "Error=" + e.toString(  ) );
        }
    }

    //================================================
    // junit.framework.TestCase functions
    //================================================
    protected void setUp(  )
    {
        if ( _petstore == null )
        {
            try
            {
                System.out.println( "Creating the Petstore object" );
                _petstore = (Petstore)new InitialContext().lookup("Petstore");
            }
            catch ( Exception e )
            {
                e.printStackTrace(  );
                fail( "Unable to create the Petstore object. Error=" + e.toString(  ) );
            }
        }
    }
}
