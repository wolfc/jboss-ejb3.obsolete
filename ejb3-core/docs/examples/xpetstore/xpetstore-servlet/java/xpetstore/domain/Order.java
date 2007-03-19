/*
 * Created on Feb 22, 2003
 */
package xpetstore.domain;

import java.io.Serializable;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @hibernate.class
 *         table="T_ORDER"
 */
public class Order
    implements Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String PENDING = "pending";
    public static final String DELIVERED = "delivered";
    public static final String CANCELLED = "cancelled";

    //~ Instance fields --------------------------------------------------------

    private Address    _address;
    private CreditCard _creditCard;
    private Customer   _customer;
    private Date       _orderDate;
    private long       _orderId;
    private Set        _orderItems = new HashSet(  );
    private String     _status = PENDING;

    //~ Constructors -----------------------------------------------------------

    public Order(  )
    {
        _customer   = new Customer(  );
        _address    = new Address(  );
        _creditCard = new CreditCard(  );
    }

    public Order( Customer customer )
    {
        _customer   = customer;
        _address    = new Address( customer.getAddress(  ) );
        _creditCard = new CreditCard( customer.getCreditCard(  ) );
    }

    //~ Methods ----------------------------------------------------------------

    public void add( Item item,
                     int  quantity )
    {
        _orderItems.add( new OrderItem( item, quantity ) );
    }

    public double getTotal(  )
    {
        double   total = 0;
        Iterator it = _orderItems.iterator(  );

        while ( it.hasNext(  ) )
        {
            total += ( ( OrderItem ) it.next(  ) ).getSubTotal(  );
        }

        return total;
    }

    /**
     * @return Address
     *
     * @hibernate.component
     */
    public Address getAddress(  )
    {
        return _address;
    }

    /**
     * @return CreditCard
     *
     * @hibernate.component
     */
    public CreditCard getCreditCard(  )
    {
        return _creditCard;
    }

    /**
     * @return Customer
     *
     * @hibernate.many-to-one
     *      column="customer_fk"
     *      cascade="none"
     */
    public Customer getCustomer(  )
    {
        return _customer;
    }

    /**
     * @return Date
     *
     * @hibernate.property
     */
    public Date getOrderDate(  )
    {
        return _orderDate;
    }

    /**
     * @return long
     *
     * @hibernate.id
     *      generator-class="vm.long"
     */
    public long getOrderId(  )
    {
        return _orderId;
    }

    /**
     * @return Set
     *
     * @hibernate.set
     *    role="orderItems"
     *    lazy="true"
     *    cascade="all"
     * @hibernate.collection-key
     *    column="order_fk"
     * @hibernate.collection-one-to-many
     *    class="xpetstore.domain.OrderItem"
     */
    public Set getOrderItems(  )
    {
        return _orderItems;
    }

    /**
     * @return String
     *
     * @hibernate.property
     *      length="10"
     */
    public String getStatus(  )
    {
        return _status;
    }

    /**
     * Sets the address.
     * @param address The address to set
     */
    public void setAddress( Address address )
    {
        _address = address;
    }

    /**
     * Sets the creditCard.
     * @param creditCard The creditCard to set
     */
    public void setCreditCard( CreditCard creditCard )
    {
        _creditCard = creditCard;
    }

    /**
     * Sets the customer.
     * @param customer The customer to set
     */
    public void setCustomer( Customer customer )
    {
        _customer = customer;
    }

    /**
     * Sets the orderDate.
     * @param orderDate The orderDate to set
     */
    public void setOrderDate( Date orderDate )
    {
        _orderDate = orderDate;
    }

    /**
     * Sets the orderId.
     * @param orderId The orderId to set
     */
    public void setOrderId( long orderId )
    {
        _orderId = orderId;
    }

    /**
     * Sets the orderItems.
     * @param orderItems The orderItems to set
     */
    public void setOrderItems( Set orderItems )
    {
        _orderItems = orderItems;
    }

    /**
     * Sets the status.
     * @param status The status to set
     */
    public void setStatus( String status )
    {
        _status = status;
    }
}
