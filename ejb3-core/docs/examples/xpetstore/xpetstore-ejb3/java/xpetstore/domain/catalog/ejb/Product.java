package xpetstore.domain.catalog.ejb;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import javax.persistence.Table;


/**
 *
 * @ ejb.bean
 *      name="Product"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="productId"
 *      schema="Product"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.value-object
 *      name="Product"
 *      match="*"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_PRODUCT"
 *
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name="Product")
@Table(name="T_PRODUCT")
public class Product
{
   private String productId;
   private String name;
   private String description;
   private Collection items;
   private Category category;
   
   public Product()
   {
      
   }
   
   public Product(String productId, String name, String description)
   {
      this.productId = productId;
      this.name = name;
      this.description = description;
   }
   
    /**
     * @ ejb.pk-field
     * @ ejb.persistence
     *      column-name="productId"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="Supports"
     */
   @Id
   @Column(name="productId", length=10)
    public String getProductId(  )
    {
       return productId;
    }

    public void setProductId( String productId )
    {
       this.productId = productId;
    }

    /**
     * @ ejb.persistence
     *      column-name="name"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(50)"
     */
    @Column(name="name", length=50)
    public String getName(  )
    {
       return name;
    }

    public void setName( String name )
    {
       this.name = name;
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
       return description;
    }

    public void setDescription( String description )
    {
       this.description = description;
    }

    /**
     * @ ejb.interface-method
     * @ ejb.relation
     *      name="product-items"
     *      role-name="product-has-items"
     */
    @OneToMany(targetEntity=Item.class, cascade={CascadeType.ALL})
    @JoinColumn(name="PRODUCT_ID")
    public Collection getItems(  )
    {
       return items;
    }

    public void setItems( Collection items )
    {
       this.items = items;
    }

    /**
     * @ ejb.interface-method
     * @ ejb.relation
     *      name="category-products"
     *      role-name="product-belongs_to-category"
     *      cascade-delete="yes"
     *
     * @ jboss.relation
     *      fk-column="category_fk"
     *      related-pk-field="categoryId"
     *      fk-contraint="${db.foreign.key}"
     *
     * @ weblogic.column-map
     *      foreign-key-column="category_fk"
     */
    @ManyToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="CATEGORY_ID")
    public Category getCategory(  )
    {
       return category;
    }

    public void setCategory( Category category )
    {
       this.category = category;
    }
    
    public String toString()
    {
       StringBuffer buffer = new StringBuffer(200);
       buffer.append("[Product: productId " + productId);
       buffer.append(", name " + name);
       buffer.append("]");
       
       return buffer.toString();
    }

}
