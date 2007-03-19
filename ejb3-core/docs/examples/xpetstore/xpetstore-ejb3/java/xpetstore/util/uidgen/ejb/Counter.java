package xpetstore.util.uidgen.ejb;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 *
 * @ ejb.bean
 *      name="Counter"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="name"
 *      schema="Counter"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_COUNTER"
 *
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name = "Counter")
@Table(name = "T_COUNTER")
public class Counter
{
   private String name;

   private int value;

   public Counter(String name)
   {
      setName(name);
      setValue(0);
   }

   /**
    * @ ejb.interface-method
    */
   public int nextValue()
   {
      int value = getValue() + 1;
      setValue(value);

      return value;
   }

   @Id
   protected java.lang.String getName()
   {
      return name;
   }

   protected void setName(java.lang.String name)
   {
      this.name = name;
   }

   protected int getValue()
   {
      return value;
   }

   protected void setValue(int value)
   {
      this.value = value;
   }

   @PostLoad
   public void ejbLoad()
   {
   }

   @PrePersist
   @PreUpdate
   public void ejbStore()
   {
   }

   @PostRemove
   public void ejbRemove() throws javax.ejb.RemoveException
   {

   }
}
