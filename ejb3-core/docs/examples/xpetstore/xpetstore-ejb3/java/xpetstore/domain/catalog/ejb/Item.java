package xpetstore.domain.catalog.ejb;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.persistence.Table;


/**
 *
 * @ ejb.bean
 *      name="Item"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="itemId"
 *      schema="Item"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.value-object
 *      name="Item"
 *      match="*"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_ITEM"
 *
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name="Item")
@Table(name="T_ITEM")
public class Item
{
   private String itemId;
   private String status;
   private double listPrice;
   private double unitCost;
   private String imagePath;
   private Product product;
   
   public Item()
   {
      
   }
   
    /**
     * @ ejb.pk-field
     * @ ejb.persistence
     *      column-name="itemId"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="Supports"
     */
   @Id
   @Column(name="itemId")
    public String getItemId(  )
   {
      return itemId;
   }

    public void setItemId( String itemId )
    {
       this.itemId = itemId;
    }

    /**
     * @ ejb.persistence
     *      column-name="description"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(255)"
     */
    @Column(name="description", length=255)
    public String getDescription(  )
    {
       return status;
    }

    public void setDescription( String status )
    {
       this.status = status;
    }

    /**
     * @ ejb.persistence
     *      column-name="listPrice"
     */
    @Column(name="listPrice")
    public double getListPrice(  )
    {
       return listPrice;
    }

    public void setListPrice( double listPrice )
    {
       this.listPrice = listPrice;
    }

    /**
     * @ ejb.persistence
     *      column-name="unitCost"
     */
    @Column(name="unitCost")
    public double getUnitCost(  )
    {
       return unitCost;
    }

    public void setUnitCost( double unitCost )
    {
       this.unitCost = unitCost;
    }

    /**
     * @ ejb.persistence
     *      column-name="imagePath"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(255)"
     */
    @Column(name="imagePath", length=255)
    public String getImagePath(  )
    {
       return imagePath;
    }

    public void setImagePath( String imagePath )
    {
       this.imagePath = imagePath;
    }

    /**
     * @ ejb.interface-method
     * @ ejb.relation
     *      name="product-items"
     *      role-name="item-belongs_to-product"
     *      cascade-delete="yes"
     *
     * @ jboss.relation
     *      fk-column="product_fk"
     *      related-pk-field="productId"
     *      fk-contraint="${db.foreign.key}
     *
     * @ weblogic.column-map
     *      foreign-key-column="product_fk"
     */
    @ManyToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="PRODUCT_ID")
    public Product getProduct(  )
    {
       return product;
    }

    public void setProduct( Product product )
    {
       this.product = product;
    }
    
    public String toString()
    {
       StringBuffer buffer = new StringBuffer(200);
       buffer.append("[Item: itemId " + itemId);
       buffer.append(", product " + product);
       buffer.append("]");
       
       return buffer.toString();
    }
}
