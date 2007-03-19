package xpetstore.services.petstore.ejb;

import java.util.Date;
import java.util.Map;

import xpetstore.domain.catalog.ejb.Category;
import xpetstore.domain.catalog.ejb.Item;
import xpetstore.domain.catalog.ejb.Product;
import xpetstore.domain.customer.ejb.Customer;
import xpetstore.domain.order.ejb.Order;

import xpetstore.services.petstore.exceptions.CartEmptyOrderException;
import xpetstore.services.petstore.exceptions.DuplicateEmailException;
import xpetstore.util.Page;


public interface Petstore
{
    boolean authenticate( String userId, String password );
   
    Category getCategory( String categoryId );

    Page getCategories( int start, int count );

    Product getProduct( String productId );
    
    Product getProductByItem( String itemId );

    Page getProducts( String categoryId, int start, int count );

    Page searchProducts( String key, int start, int count );

    Item getItem( String itemId );

    Page getItems( String productId, int start, int count );
    
    String createCustomer( Customer customer ) throws DuplicateEmailException;
       
    void updateCustomer( Customer customer );

    Customer getCustomer( String userId );

    Integer createOrder( String userId, Date orderDate, Map items ) throws CartEmptyOrderException;

    Page getCustomerOrders( String userId, int start, int count );

    Order getOrder( Integer orderUId );
 
    Page getOrderItems( Integer orderUId, int start, int count );
}
