package xpetstore.services.cart.test;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import xpetstore.services.cart.ejb.Cart;
import xpetstore.services.cart.model.CartItem;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class CartTest
    extends TestCase
{
    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for CartTest.
     * @param arg0
     */
    public CartTest( String arg0 )
    {
        super( arg0 );
    }

    //~ Methods ----------------------------------------------------------------

    public void testCart(  )
    {
        Cart cart = null;

        try
        {
            cart = (Cart)new InitialContext().lookup("Cart");
            cart.addItem( "EST-1" );
            cart.addItem( "EST-2" );
            cart.addItem( "EST-10" );
            cart.addItem( "EST-1", 9 );
            cart.removeItem( "EST-2" );

            // Cart count 
            assertEquals( "cart.count", 2, cart.getCount(  ) );

            // Cart items 
            Collection items = cart.getCartItems(  );
            assertNotNull( "items=null", items );
            assertEquals( "items.size", 2, items.size(  ) );

            for ( Iterator it = items.iterator(  ); it.hasNext(  ); )
            {
                CartItem item = ( CartItem ) it.next(  );

                if ( "EST-1".equals( item.getItemId(  ) ) )
                {
                    assertEquals( "item[EST-1].name", "Angelfish", item.getName(  ) );
                    assertEquals( "item[EST-1].description", "Large", item.getDescription(  ) );
                    assertEquals( "item[EST-1].productId", "FI-SW-01", item.getProductId(  ) );
                    assertEquals( "item[EST-1].unitCost", 16.50, item.getUnitCost(  ), 0 );
                    assertEquals( "item[EST-1].quantity", 10, item.getQuantity(  ) );
                    assertEquals( "item[EST-1].totalCost", 165.0, item.getTotalCost(  ), 0 );
                }
                else if ( "EST-10".equals( item.getItemId(  ) ) )
                {
                    assertEquals( "item[EST-10].name", "Bulldog", item.getName(  ) );
                    assertEquals( "item[EST-10].description", "Spotless Female Puppy", item.getDescription(  ) );
                    assertEquals( "item[EST-10].productId", "K9-BD-01", item.getProductId(  ) );
                    assertEquals( "item[EST-10].unitCost", 28.50, item.getUnitCost(  ), 0 );
                    assertEquals( "item[EST-10].quantity", 1, item.getQuantity(  ) );
                    assertEquals( "item[EST-10].totalCost", 28.50, item.getTotalCost(  ), 0 );
                }
                else
                {
                    assertEquals( item.getItemId(  ) + " is invalid", false, true );
                }
            }

            // Sub total          
            assertEquals( "subTotal", 193.5, cart.getTotal(  ), 0 );
        }
        catch ( Exception e )
        {
            e.printStackTrace(  );
            fail( "Error=" + e.toString(  ) );
        }
        finally
        {
            try
            {
                if ( cart != null )
                {
                    cart.remove(  );
                }
            }
            catch ( Exception e )
            {
                fail( "Unable to destroy the card. Error=" + e.toString(  ) );
            }
        }
    }

    public void testUpdateCart(  )
    {
        Cart cart = null;

        try
        {
            cart = (Cart)new InitialContext().lookup("Cart");
            cart.addItem( "EST-1" );
            cart.addItem( "EST-10" );
            cart.updateItems( new String[] { "EST-1", "EST-10" }, new int[] { 10, 11 } );

            // Cart count 
            assertEquals( "cart.count", 2, cart.getCount(  ) );

            // Cart items 
            Collection items = cart.getCartItems(  );
            assertNotNull( "items=null", items );
            assertEquals( "items.size", 2, items.size(  ) );

            for ( Iterator it = items.iterator(  ); it.hasNext(  ); )
            {
                CartItem item = ( CartItem ) it.next(  );

                if ( "EST-1".equals( item.getItemId(  ) ) )
                {
                    assertEquals( "item[EST-1].name", "Angelfish", item.getName(  ) );
                    assertEquals( "item[EST-1].description", "Large", item.getDescription(  ) );
                    assertEquals( "item[EST-1].productId", "FI-SW-01", item.getProductId(  ) );
                    assertEquals( "item[EST-1].unitCost", 16.50, item.getUnitCost(  ), 0 );
                    assertEquals( "item[EST-1].quantity", 10, item.getQuantity(  ) );
                    assertEquals( "item[EST-1].totalCost", 165.0, item.getTotalCost(  ), 0 );
                }
                else if ( "EST-10".equals( item.getItemId(  ) ) )
                {
                    assertEquals( "item[EST-10].name", "Bulldog", item.getName(  ) );
                    assertEquals( "item[EST-10].description", "Spotless Female Puppy", item.getDescription(  ) );
                    assertEquals( "item[EST-10].productId", "K9-BD-01", item.getProductId(  ) );
                    assertEquals( "item[EST-10].unitCost", 28.50, item.getUnitCost(  ), 0 );
                    assertEquals( "item[EST-10].quantity", 11, item.getQuantity(  ) );
                    assertEquals( "item[EST-10].totalCost", 313.50, item.getTotalCost(  ), 0 );
                }
                else
                {
                    assertEquals( item.getItemId(  ) + " is invalid", false, true );
                }
            }

            // Sub total          
            assertEquals( "subTotal", 478.5, cart.getTotal(  ), 0 );
        }
        catch ( Exception e )
        {
            e.printStackTrace(  );
            fail( "Error=" + e.toString(  ) );
        }
        finally
        {
            try
            {
                if ( cart != null )
                {
                    cart.remove(  );
                }
            }
            catch ( Exception e )
            {
                fail( "Unable to destroy the card. Error=" + e.toString(  ) );
            }
        }
    }
}
