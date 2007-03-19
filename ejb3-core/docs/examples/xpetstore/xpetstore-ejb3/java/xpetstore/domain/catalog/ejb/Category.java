package xpetstore.domain.catalog.ejb;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 *
 * @ ejb.bean
 *      name="Category"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="categoryId"
 *      schema="Category"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.value-object
 *      name="Category"
 *      match="*"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_CATEGORY"
 * @ ejb.finder
 *      signature="Collection findAll()"
 *      query="SELECT OBJECT(c) FROM Category c"
 *
 * @ jboss.query
 *      signature="Collection findAll()"
 *      strategy="on-load"
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name="Category")
@Table(name="T_CATEGORY")
public class Category
{
   private String categoryId;
   private String name;
   private String description;
   private Collection products;
   
   public Category()
   {
      
   }
   
    /**
     * @ ejb.pk-field
     * @ ejb.persistence
     *      column-name="categoryId"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="Supports"
     */
   @Id
   @Column(name="categoryId")
    public String getCategoryId(  )
    {
       return categoryId;
    }

    public void setCategoryId( String categoryId )
    {
       this.categoryId = categoryId;
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
     *      name="category-products"
     *      role-name="category-has-products"
     */
    @OneToMany(targetEntity=Product.class, cascade={CascadeType.ALL})
    @JoinColumn(name="CATEGORY_ID")
    public Collection getProducts(  )
    {
       return products;
    }

    public void setProducts( Collection products )
    {
       this.products = products;
    }
}
