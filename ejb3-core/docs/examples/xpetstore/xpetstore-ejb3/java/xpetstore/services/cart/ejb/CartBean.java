package xpetstore.services.cart.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.ejb.Remove;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.LocalBinding;

import xpetstore.domain.catalog.ejb.Item;
import xpetstore.domain.catalog.ejb.Product;

import xpetstore.services.cart.model.CartItem;
import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.util.Debug;


/**
 *
 * @ ejb.bean
 *      name="Cart"
 *      type="Stateful"
 *      view-type="local"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.ejb-ref
 *      ejb-name="Item"
 *      view-type="local"
 * 		ref-name="ejb/ItemLocal"
 */
@Stateful(name="Cart")
@LocalBinding(jndiBinding="ejb/Cart")
@Local(Cart.class)
public class CartBean
   implements Cart
{

   @PersistenceContext
   private EntityManager manager;
   
    /** Map of item quantities indexed by itemId */
    private Map _details = new HashMap(  );

   public CartBean()
   {
      
   }

    /**
     * @ ejb.interface-method
     */
    public void addItem( String itemId )
    {
        addItem( itemId, 1 );
    }

    /**
     * @ ejb.interface-method
     */
    public void addItem( String itemId,
                         int    qty )
    {
        Integer curQty = ( Integer ) _details.get( itemId );
        if ( curQty == null )
        {
            _details.put( itemId, new Integer( qty ) );
        }
        else
        {
            _details.put( itemId, new Integer( qty + curQty.intValue(  ) ) );
        }
    }

    /**
     * @ ejb.interface-method
     */
    public void removeItem( String itemId )
    {
        _details.remove( itemId );
    }

    /**
     * @ ejb.interface-method
     */
    public void updateItems( String itemId[],
                             int    newQty[] )
    {
        for ( int i = 0; i < itemId.length; i++ )
        {
            String id = itemId[ i ];
            int    qty = newQty[ i ];

            if ( _details.containsKey( id ) )
            {
                if ( qty > 0 )
                {
                    _details.put( id, new Integer( qty ) );
                }
            }
            else
            {
                Debug.print( " can't update item[" + id + "]. This item not in the cart" );
            }
        }
    }

    /**
     * @ ejb.interface-method
     */
    public int getCount(  )
    {
        return _details.size(  );
    }

    /**
     * @ ejb.interface-method
     */
    public double getTotal(  )
    {
        double   ret = 0.0d;
        Iterator it = getCartItems(  ).iterator(  );
        for ( ; it.hasNext(  ); )
        {
            CartItem i = ( CartItem ) it.next(  );
            ret += ( i.getUnitCost(  ) * i.getQuantity(  ) );
        }

        return ret;
    }

    /**
     * @ ejb.interface-method
     */
    public void empty(  )
    {
        _details.clear(  );
    }

    /**
     * @ return Return a Map of quantities indexed by itemId
     *
     * @ ejb.interface-method
     * @ ejb.transaction-type
     *      type="NotSupported"
     */
    public Map getDetails(  )
    {
        return _details;
    }

    /**
     * @ return Return a list of {@link CartItem} objects
     *
     * @ ejb.interface-method
     * @ ejb.transaction-type
     *      type="NotSupported"
     */
    public Collection getCartItems(  )
    {
        try
        {
            ArrayList     items = new ArrayList(  );
            Iterator      it = _details.keySet(  ).iterator(  );
            while ( it.hasNext(  ) )
            {
                String  key = ( String ) it.next(  );
                Integer value = ( Integer ) _details.get( key );
                try
                {
                    Item    item = manager.find( Item.class, key );
             
                    Product prod = item.getProduct(  );

                    CartItem     ci = new CartItem( item.getItemId(  ), prod.getProductId(  ), prod.getName(  ), item.getDescription(  ), value.intValue(  ), item.getListPrice(  ) );

                    items.add( ci );
                }
                catch ( Exception cce )
                {
                    cce.printStackTrace(  );
                }
            }

            // Sort the items
            Collections.sort( items, new CartItem.ItemIdComparator(  ) );
            return items;
        }
        catch ( Exception e )
        {
            return Collections.EMPTY_LIST;
        }
    }
    
    @Remove
    public void remove()
    {
       
    }

}
