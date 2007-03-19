package xpetstore.services.cart.ejb;

import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public interface Cart
{
    void addItem( String itemId );
   
    void addItem( String itemId, int qty );
   
    void removeItem( String itemId );
 
    void updateItems( String itemId[], int newQty[] );

    int getCount(  );
   
    double getTotal(  );
   
    void empty(  );

    Map getDetails(  );
 
    Collection getCartItems(  );
    
    void remove();
}
