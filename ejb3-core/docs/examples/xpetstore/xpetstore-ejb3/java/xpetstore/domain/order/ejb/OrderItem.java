package xpetstore.domain.order.ejb;

import javax.annotation.EJB;
import javax.ejb.EJBException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import xpetstore.domain.catalog.ejb.Item;

import xpetstore.util.uidgen.ejb.UIDGenerator;


/**
 *
 * @ ejb.bean
 *      name="OrderItem"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="orderItemUId"
 *      schema="OrderItem"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.value-object
 *      name="OrderItem"
 *      match="*"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_ORDER_ITEM"
 * @ ejb.ejb-ref
 *      ejb-name="UIDGenerator"
 *      view-type="local"
 * 		ref-name="ejb/UIDGeneratorLocal"
 *
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name = "OrderItem")
@Table(name = "T_ORDER_ITEM")
public class OrderItem
{

    public static final String COUNTER_NAME = "OrderItem";
    
    private Integer orderItemUId;
    private int quantity;
    private double unitPrice;
    private Item item;
    
    public OrderItem()
    {
       
    }
    
    public OrderItem( int quantity, double unitPrice )
   {
       setQuantity( quantity );
       setUnitPrice( unitPrice );
   }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="Supports"
     */
    public double calculateSubTotal(  )
    {
        return Math.max( getQuantity(  ) * getUnitPrice(  ), 0 );
    }

    /**
     * @ ejb.pk-field
     * @ ejb.persistence
     *      column-name="orderItemUId"
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="Supports"
     */
   @Id
   @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="orderItemUId")
    public Integer getOrderItemUId(  )
    {
       return orderItemUId;
    }

    public void setOrderItemUId( Integer orderItemUId )
    {
       this.orderItemUId = orderItemUId;
    }

    /**
     * @ ejb.persistence
     *      column-name="quantity"
     */
    @Column(name="quantity")
    public int getQuantity(  )
    {
       return quantity;
    }

    public void setQuantity( int quantity )
    {
       this.quantity = quantity;
    }

    /**
     * @ ejb.persistence
     *      column-name="unitPrice"
     */
    @Column(name="unitPrice")
    public double getUnitPrice(  )
    {
       return unitPrice;
    }

    public void setUnitPrice( double unitPrice )
    {
       this.unitPrice = unitPrice;
    }

    /**
     * @ ejb.interface-method
     * @ ejb.relation
     *      name="orderItem-item"
     *      role-name="orderItem-refers_to-item"
     *      cascade-delete="yes"
     *      target-ejb="Item"
     *      target-role-name="item-is_refered_by-orderItems"
     *      target-multiple="yes"
     * @ ejb.value-object
     *      aggregate="xpetstore.domain.catalog.model.ItemValue"
     *      aggregate-name="Item"
     *      members="xpetstore.domain.catalog.interfaces.Item"
     *      members-name="Item"
     *      relation="external"
     *
     * @ jboss.relation
     *      fk-column="itemId_fk"
     *      related-pk-field="itemId"
     *
     * @ weblogic.column-map
     *      foreign-key-column="itemId_fk"
     */
    @ManyToOne(targetEntity=Item.class, cascade={CascadeType.ALL})
    @JoinColumn(name="itemId_fk")
    public Item getItem(  )
    {
       return item;
    }

    /**
     * @ ejb.interface-method
     */
    public void setItem( Item item )
    {
       this.item = item;
    }
}
