package xpetstore.domain.signon.ejb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 *
 * @ ejb.bean
 *      name="Account"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="userId"
 *      schema="Account"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.value-object
 *      name="Account"
 *      match="*"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_ACCOUNT"
 *
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name="Account")
@Table(name="T_ACCOUNT")
public class Account
{
   private String userId;
   private String password;
   
   public Account()
   {
      
   }
   
   public Account(String userId, String password)
   {
      this.userId = userId;
      this.password = password;
   }

    /**
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="Supports"
     */
    public boolean matchPassword( String password )
    {
        return ( password == null )
               ? ( getPassword(  ) == null )
               : password.equals( getPassword(  ) );
    }

    /**
     * @ ejb.pk-field
     * @ ejb.persistence
     *      column-name="userId"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="NotSupported"
     */
    @Id
    @Column(name="userId", length=10)
    public String getUserId(  )
    {
       return userId;
    }

    public void setUserId( String userId )
    {
       this.userId = userId;
    }

    /**
     * @ ejb.persistence
     *      column-name="pwd"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
     */
    @Column(name="pwd", length=10)
    public String getPassword(  )
    {
       return password;
    }

    public void setPassword( String password )
    {
       this.password = password;
    }
    
    public String toString()
    {
       StringBuffer buffer = new StringBuffer(200);
       buffer.append("[Account: userId " + userId);
       buffer.append(", password " + password);
       buffer.append("]");
       
       return buffer.toString();
    }

}
