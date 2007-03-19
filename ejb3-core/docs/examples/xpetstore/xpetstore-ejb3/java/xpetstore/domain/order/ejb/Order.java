package xpetstore.domain.order.ejb;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import javax.annotation.EJB;

import javax.naming.NamingException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import xpetstore.domain.customer.ejb.Customer;
import xpetstore.domain.catalog.ejb.Item;

import xpetstore.util.uidgen.ejb.*;

/**
 *
 * @ ejb.bean
 *      name="Order"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="orderUId"
 *      schema="Order"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.value-object
 *      name="Order"
 *      match="*"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_ORDER"
 * @ ejb.finder
 *      signature="Collection findByCustomer(java.lang.String userId)"
 *      query="SELECT OBJECT(o) FROM Order AS o WHERE o.customer.userId = ?1"
 * @ ejb.ejb-ref
 *      ejb-name="OrderItem"
 *      view-type="local"
 *      ref-name="ejb/OrderItemLocal"
 * @ ejb.ejb-ref
 *      ejb-name="UIDGenerator"
 *      view-type="local"
 *      ref-name="ejb/UIDGeneratorLocal"
 *
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name = "Order")
@Table(name = "T_ORDER")
public class Order
{
    public static final String COUNTER_NAME = "Order";
    
    private Integer orderUid;
    private Date orderDate;
    private String status;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zipcode;
    private String country;
    private String creditCardNumber;
    private String creditCardType;
    private String creditCardExpiryDate;
    private Collection orderItems;
    private Customer customer;
    
    public Order()
    {
    }

    /**
       * @ ejb.interface-method
       */
    public void addOrderItem( OrderItem orderItem )
    {
       if (orderItems == null)
          orderItems = new java.util.ArrayList();
       
         orderItems.add(orderItem);
    }

    /**
       * @ ejb.interface-method
       */
    public void changeStatus( String status )
    {
        setStatus( status );
    }

    /**
       * @ ejb.interface-method
       */
    public double calculateTotal(  )
    {
        double   total = 0;
        Iterator it = getOrderItems(  ).iterator(  );
        while ( it.hasNext(  ) )
        {
            total += ( ( OrderItem ) it.next(  ) ).calculateSubTotal(  );
        }

        return total;
    }

    /**
       * @ ejb.pk-field
       * @ ejb.persistence column-name="orderUId"
       * @ ejb.interface-method
       * @ ejb.transaction
       *   type="Supports"
       */
   @Id
   @GeneratedValue(strategy= GenerationType.SEQUENCE)
    @Column(name="orderUId")
    public Integer getOrderUId(  )
    {
       return orderUid;
    }

    public void setOrderUId( Integer orderUId )
    {
       this.orderUid = orderUId;
    }

    /**
       * @ ejb.persistence column-name="orderDate"
       */
    @Column(name="orderDate")
    public Date getOrderDate(  )
    {
       return orderDate;
    }

    public void setOrderDate( Date orderDate )
    {
       this.orderDate = orderDate;
    }

    /**
       * @ ejb.persistence column-name="status" jdbc-type="VARCHAR"
       *   sql-type="varchar(25)"
       */
    @Column(name="status", length=25)
    public String getStatus(  )
    {
       return status;
    }

    public void setStatus( String status )
    {
       this.status = status;
    }

    /**
       * @ ejb.persistence column-name="street1" jdbc-type="VARCHAR"
       *                  sql-type="varchar(50)"
       */
    @Column(name="street1", length=50)
    public String getStreet1(  )
    {
       return street1;
    }

    public void setStreet1( java.lang.String street1 )
    {
       this.street1 = street1;
    }

    /**
       * @ ejb.persistence column-name="street2" jdbc-type="VARCHAR"
       *                  sql-type="varchar(50)"
       */
    @Column(name="street2", length=50)
    public String getStreet2(  )
    {
       return street2;
    }

    public void setStreet2( String street2 )
    {
       this.street2 = street2;
    }

    /**
       * @ ejb.persistence column-name="city" jdbc-type="VARCHAR"
       *                  sql-type="varchar(25)"
       */
    @Column(name="city", length=25)
    public java.lang.String getCity(  )
    {
       return city;
    }

    public void setCity( String city )
    {
       this.city = city;
    }

    /**
       * @ ejb.persistence column-name="state" jdbc-type="VARCHAR"
       *                  sql-type="varchar(3)"
       */
    @Column(name="state", length=3)
    public String getState(  )
    {
       return state;
    }

    public void setState( String state )
    {
       this.state = state;
    }

    /**
       * @ ejb.persistence column-name="zipcode" jdbc-type="VARCHAR"
       *                  sql-type="varchar(10)"
       */
    @Column(name="zipcode", length=10)
    public String getZipcode(  )
    {
       return zipcode;
    }

    public void setZipcode( String zipcode )
    {
       this.zipcode = zipcode;
    }

    /**
       * @ ejb.persistence column-name="country" jdbc-type="VARCHAR"
       *                  sql-type="varchar(3)"
       */
    @Column(name="country", length=10)
    public java.lang.String getCountry(  )
    {
       return country;
    }

    public void setCountry( String country )
    {
       this.country = country;
    }

    /**
       * @ ejb.persistence column-name="creditCardNumber" jdbc-type="VARCHAR"
       *                  sql-type="varchar(25)"
       */
    @Column(name="creditCardNumber", length=25)
    public String getCreditCardNumber(  )
    {
       return creditCardNumber;
    }

    public void setCreditCardNumber( String creditCardNumber )
    {
       this.creditCardNumber = creditCardNumber;
    }

    /**
       * @ ejb.persistence column-name="creditCardType" jdbc-type="VARCHAR"
       *                  sql-type="varchar(25)"
       */
    @Column(name="creditCardType", length=25)
    public String getCreditCardType(  )
    {
       return creditCardType;
    }

    public void setCreditCardType( String creditCardType )
    {
       this.creditCardType = creditCardType;
    }

    /**
       * @ ejb.persistence column-name="creditCardExpiryDate" jdbc-type="VARCHAR"
       *                  sql-type="varchar(10)"
       */
    @Column(name="creditCardExpiryDate", length=10)
    public String getCreditCardExpiryDate(  )
    {
       return creditCardExpiryDate;
    }

    public void setCreditCardExpiryDate( String creditCardExpiryDate )
    {
       this.creditCardExpiryDate = creditCardExpiryDate;
    }

    /**
       * @ ejb.interface-method
       * @ ejb.relation name="order-customer"
       *               role-name="order-belongs_to-customer"
       *               target-ejb="Customer"
       *               target-role-name="customer-has-orders"
       *               target=multiple="yes"
       * @ jboss.relation fk-column="customer_fk" related-pk-field="userId"
       * @ weblogic.column-map foreign-key-column="customer_fk"
       */
    @ManyToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="CUSTOMER_ID")
    public Customer getCustomer(  )
    {
       return customer;
    }

    /**
       * @ ejb.interface-method
       */
    public void setCustomer( Customer customer )
    {
       this.customer = customer;
    }

    /**
       * @ ejb.interface-method
       * @ ejb.relation name="order-orderItem" role-name="order-has-orderItems"
       *               target-ejb="OrderItem"
       *               target-role-name="orderItem-belongs_to-order"
       *               target-cascade-delete="yes"
       * @ jboss.target-relation fk-column="order_fk" related-pk-field="orderUId"
       *                        fk-contraint="${db.foreign.key}
       * @ weblogic.target-column-map foreign-key-column="order_fk"
       */
    @OneToMany(targetEntity=OrderItem.class, cascade={CascadeType.ALL})
    @JoinColumn(name="ORDER_ID")
    public Collection getOrderItems(  )
    {
       return orderItems;
    }

    public void setOrderItems( Collection orderItems )
    {
       this.orderItems = orderItems;
    }
   
}
