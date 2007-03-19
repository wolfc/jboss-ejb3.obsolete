package xpetstore.services.petstore.ejb;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.LocalBinding;

import xpetstore.domain.catalog.ejb.Category;
import xpetstore.domain.catalog.ejb.Item;
import xpetstore.domain.catalog.ejb.Product;
import xpetstore.domain.customer.ejb.Customer;
import xpetstore.domain.order.ejb.Order;
import xpetstore.domain.order.ejb.OrderItem;
import xpetstore.domain.order.model.OrderStatus;

import xpetstore.domain.signon.ejb.Account;

import xpetstore.services.petstore.exceptions.CartEmptyOrderException;
import xpetstore.services.petstore.exceptions.DuplicateEmailException;

import xpetstore.util.JMSUtil;
import xpetstore.util.JNDINames;
import xpetstore.util.Page;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @ ejb.bean
 *      name="Petstore"
 *      type="Stateless"
 *      view-type="local"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.ejb-ref
 *      ejb-name="Category"
 *      view-type="local"
 * 		ref-name="ejb/CategoryLocal"
 * @ ejb.ejb-ref
 *      ejb-name="Item"
 *      view-type="local"
 * 		ref-name="ejb/ItemLocal"
 * @ ejb.ejb-ref
 *      ejb-name="Product"
 *      view-type="local"
 * 		ref-name="ejb/ProductLocal"
 * @ ejb.ejb-ref
 *      ejb-name="Customer"
 *      view-type="local"
 * 		ref-name="ejb/CustomerLocal"
 * @ ejb.ejb-ref
 *      ejb-name="Account"
 *      view-type="local"
 * 		ref-name="ejb/AccountLocal"
 * @ ejb.ejb-ref
 *      ejb-name="Order"
 *      view-type="local"
 * 		ref-name="ejb/OrderLocal"
 * @ ejb.resource-ref
 *      res-ref-name="${jndi.queue.ConnectionFactory}"
 *      res-type="javax.jms.QueueConnectionFactory"
 *      res-auth="Container"
 * 		jndi-name="${orion.queue.ConnectionFactory}"
 * @ ejb.resource-ref
 *      res-ref-name="${jndi.queue.order}"
 *      res-type="javax.jms.Queue"
 *      res-auth="Container"
 * 		jndi-name="${orion.queue.order}"
 * @ ejb.resource-ref
 *      res-ref-name="${jndi.datasource}"
 *      res-type="javax.sql.DataSource"
 *      res-auth="Container"
 * 		jndi-name="${orion.datasource}"
 *
 * @ jboss.resource-ref
 *      res-ref-name="${jndi.queue.ConnectionFactory}"
 *      jndi-name="${jboss.queue.ConnectionFactory}"
 * @ jboss.resource-ref
 *      res-ref-name="${jndi.queue.order}"
 *      jndi-name="${jboss.queue.order}"
 * @ jboss.resource-ref
 *      res-ref-name="${jndi.datasource}"
 *      jndi-name="${jboss.datasource}"
 *
 * @ weblogic.resource-description
 *      res-ref-name="${jndi.queue.ConnectionFactory}"
 *      jndi-name="${weblogic.queue.ConnectionFactory}"
 * @ weblogic.resource-description
 *      res-ref-name="${jndi.queue.order}"
 *      jndi-name="${weblogic.queue.order}"
 * @ weblogic.resource-description
 *      res-ref-name="${jndi.datasource}"
 *      jndi-name="${weblogic.datasource}"
 */
@Stateless(name="Petstore")
@LocalBinding(jndiBinding="ejb/Petstore")
@Local(Petstore.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PetstoreBean implements Petstore
{
 //  private static final Logger log = Logger.getLogger(PetstoreBean.class);
   
   @PersistenceContext
   private EntityManager manager;
   
    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public boolean authenticate( String userId,
                                 String password )
    {
       try
       {
         Account act = manager.find(Account.class, userId );
         System.out.println("!! PetstoreBean.authenticate " + act + " " + userId + " " + password);
         if (act == null)
            return false;
         return act.matchPassword( password );
       }
       catch (Exception e)
       {
          e.printStackTrace();
          return false;
       }
    }

 

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public Category getCategory( String categoryId )
    {
        return manager.find(Category.class, categoryId );
    }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public Page getCategories( int start,
                               int count )
    {
       return toPage( manager.createQuery("SELECT Category c FROM Category").getResultList(), start, count, Category.class );
    }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public Product getProduct( String productId )
    {
        return manager.find( Product.class, productId );
    }

    /**
     * @ ejb.interface-method
     */
    public Product getProductByItem( String itemId )
    {
        Item item = manager.find( Item.class, itemId );
        return item.getProduct(  );
    }

    /**
     * @ ejb.interface-method
     */
    public Page getProducts( String categoryId,
                             int    start,
                             int    count )
    {
       try
       {
          System.out.println("!! PetstoreBean.getProducts manager " + manager);
         Category cat = manager.find( Category.class, categoryId );
         System.out.println("!! PetstoreBean.getProducts cat " + cat);
         if (cat != null)
            return toPage( cat.getProducts(  ), start, count, Product.class );
         else
            return Page.EMPTY_PAGE;
       }
       catch (Exception e)
       {
          e.printStackTrace();
          return null;
       }
    }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public Page searchProducts( String key,
                                int    start,
                                int    count )
    {    
       return toPage(manager.createNativeQuery("SELECT productId,name,description FROM T_PRODUCT WHERE (productId LIKE " + key + ") OR (name LIKE " + key + ") OR (description LIKE " + key + ")").getResultList(), start, count, Product.class);
    }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public Item getItem( String itemId )
    {
        return manager.find( Item.class, itemId );
    }

    /**
     * @ ejb.interface-method
     */
    public Page getItems( String productId,
                          int    start,
                          int    count )
    {
         Product prod = manager.find( Product.class, productId );
         return toPage( prod.getItems(  ), start, count, Item.class );
    }

    /**
     * @ ejb.interface-method
     */
    public String createCustomer( Customer customer )
        throws DuplicateEmailException
    {
       System.out.println("!!PetstoreBean.createCustomer " + customer);
       
        /* Make sure that the customer email is unique */
         String email = customer.getEmail(  );
         List customers = null;
         try
         {
            customers = manager.createQuery("SELECT c FROM Customer c WHERE c.email = '" + email + "'").getResultList();
            
         } 
         catch (Exception e)
         {
            
         }
         
         if (customers != null && customers.size() > 0)
            throw new DuplicateEmailException( email );
      
         /* create the account */
         Account account = customer.getAccount(  );
         
         customer.setUserId(account.getUserId());

         manager.persist(account);
         
         manager.persist(customer);
         
         System.out.println("!!PetstoreBean.createCustomer userId " + customer);
         
         return customer.getUserId(  );
    }

    /**
     * @ ejb.interface-method
     */
    public void updateCustomer( Customer customer )
    {
       manager.merge(customer);
    }

    /**
     * @ ejb.interface-method
     */
    public Customer getCustomer( String userId )
    {
        return manager.find( Customer.class, userId );
    }

    /**
     * @param userId        Id of the customer
     * @param orderDate    Creation date of the order
     * @param items        <code>java.lang.Map</code> containing the items
     *                      ordered. The key is the itemId, and the value
     *                      a </code>java.lang.Integer</code> representing the
     *                      quantity ordered
     *
     * @ ejb.interface-method
     */
    public Integer createOrder( String userId,
                                Date   orderDate,
                                Map    items )
        throws CartEmptyOrderException
    {
        /* Make sure that the cart is not empty */
        if ( items.size(  ) == 0 )
        {
            throw new CartEmptyOrderException(  );
        }

        /* Get the customer */
        Customer cst = manager.find( Customer.class, userId );

        /* Create the order */
        Order order = new Order(  );
        order.setOrderDate( orderDate );
        order.setStatus( OrderStatus.PENDING );
        order.setStreet1( cst.getStreet1(  ) );
        order.setStreet2( cst.getStreet2(  ) );
        order.setCity( cst.getCity(  ) );
        order.setState( cst.getState(  ) );
        order.setZipcode( cst.getZipcode(  ) );
        order.setCountry( cst.getCountry(  ) );
        order.setCreditCardNumber( cst.getCreditCardNumber(  ) );
        order.setCreditCardType( cst.getCreditCardType(  ) );
        order.setCreditCardExpiryDate( cst.getCreditCardExpiryDate(  ) );

        order.setCustomer( cst );
        
        manager.persist(order);

        /* Add the items */
        Iterator keys = items.keySet(  ).iterator(  );
        while ( keys.hasNext(  ) )
        {
            String  itemId = ( String ) keys.next(  );
            Integer qty = ( Integer ) items.get( itemId );
            
            Item item = manager.find(Item.class, itemId);
            
            System.out.println("!!PetstoreBean.createOrder item " + item + " " + itemId);
            
            OrderItem orderItem = new OrderItem( qty, item.getListPrice(  ) );
            orderItem.setItem( item );
            
            manager.persist(orderItem);
            
            order.addOrderItem(orderItem);
            
            order.addOrderItem( orderItem );
        }

        /* Process the item ansynchronously */
        try
        {
            JMSUtil.sendToJMSQueue( JNDINames.QUEUE_ORDER, order.getOrderUId(  ), false );
        }
        catch ( Exception e )
        {
            throw new EJBException( e );
        }

        return order.getOrderUId(  );
    }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public Page getCustomerOrders( String userId,
                                   int    start,
                                   int    count )
    {
       List col = manager.createQuery("SELECT Order o FROM Order WHERE o.customer.userId = " + userId).getResultList();
       return toPage( col, start, count, Order.class );
    }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    public Order getOrder( Integer orderUId )
    {
        return manager.find( Order.class, orderUId );
    }

    /**
     * @ ejb.interface-method
     */
    public Page getOrderItems( Integer orderUId,
                               int     start,
                               int     count )
    {
        Collection col;
      
         Order order = manager.find( Order.class, orderUId );
         if (order != null)
            col = order.getOrderItems(  );
         else
            col = new ArrayList(  );
     
        return toPage( col, start, count, OrderItem.class );
    }

    private Page toPage( Collection col,
                         int        start,
                         int        count,
                         Class      type )
    {
        int size = col.size(  );
        if ( size == 0 )
        {
            return Page.EMPTY_PAGE;
        }

        ArrayList lst = new ArrayList(  );
        Iterator  it = col.iterator(  );
        for ( int i = 0, imax = start + count; ( i < imax ) && it.hasNext(  );
              i++ )
        {
            Object obj = it.next(  );
            if ( i >= start )
            {
                lst.add( obj );
            }
        }

        return new Page( lst, start, ( start + count ) < size );
    }
}
