package xpetstore.domain.customer.ejb;

import javax.ejb.CreateException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import xpetstore.domain.signon.ejb.Account;

/**
 *
 * @ ejb.bean
 *      name="Customer"
 *      type="CMP"
 *      view-type="local"
 *      primkey-field="userId"
 *      schema="Customer"
 *      cmp-version="${ejb.cmp.version}"
 * @ ejb.value-object
 *      name="Customer"
 *      match="*"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.persistence
 *      table-name="T_CUSTOMER"
 * @ ejb.finder
 *      signature="Customer findByEmail(java.lang.String email)"
 *      query="SELECT OBJECT(c) FROM Customer AS c WHERE c.email = ?1"
 *
 * @ jboss.persistence
 *      create-table="${jboss.create.table}"
 *      remove-table="${jboss.remove.table}"
 */
@Entity(name="Customer")
@Table(name="T_CUSTOMER")
public class Customer
{
   private String userId;
   private String firstname;
   private String lastname;
   private String email;
   private String telephone;
   private String localeId;
   private String street1;
   private String street2;
   private String city;
   private String state;
   private String zipcode;
   private String country;
   private String creditCardNumber;
   private String creditCardExpiryDate;
   private String creditCardType;
   private Account account;
   
   public Customer()
   {
      
   }
   
   public Customer( Account  account )
   {
      setUserId( account.getUserId(  ) );
   }
   
   public Customer(String userId, String firstname, String lastname, String email, String telephone, String localeId, String street1, String street2, String city, String state, String zipcode, String country, String creditCardNumber, String creditCardType, String creditCardExpiryDate)
   {
      this.userId = userId;
      this.firstname = firstname;
      this.lastname = lastname;
      this.email = email;
      this.telephone = telephone;
      this.localeId = localeId;
      this.street1 = street1;
      this.street2 = street2;
      this.city = city;
      this.state = state;
      this.zipcode = zipcode;
      this.country = country;
      this.creditCardNumber = creditCardNumber;
      this.creditCardExpiryDate = creditCardExpiryDate;
      this.creditCardType = creditCardType;
   }
    /**
     * @ ejb.pk-field
     * @ ejb.persistence
     *      column-name="userId"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
     * @ ejb.interface-method
     * @ ejb.transaction
     *      type="Supports"
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
     *      column-name="firstname"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(50)"
     */
    @Column(name="firstname", length=50)
    public String getFirstname(  )
    {
       return firstname;
    }

    public void setFirstname( String firstname )
    {
       this.firstname = firstname;
    }

    /**
     * @ ejb.persistence
     *      column-name="lastname"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(50)"
     */
    @Column(name="lastname", length=50)
    public String getLastname(  )
    {
       return lastname;
    }

    public void setLastname( String lastname )
    {
       this.lastname = lastname;
    }

    /**
     * @ ejb.persistence
     *      column-name="email"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(255)"
     */
    @Column(name="email", length=255)
    public String getEmail(  )
    {
       return email;
    }

    public void setEmail( String email )
    {
       this.email = email;
    }

    /**
     * @ ejb.persistence
     *      column-name="telephone"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
     */
    @Column(name="telephone", length=10)
    public String getTelephone(  )
    {
       return telephone;
    }

    public void setTelephone( String telephone )
    {
       this.telephone = telephone;
    }

    /**
     * @ ejb.persistence
     *      column-name="language"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(3)"
     */
    @Column(name="language", length=3)
    public String getLanguage(  )
    {
       return localeId;
    }

    public void setLanguage( String localeId )
    {
       this.localeId = localeId;
    }

    /**
     * @ ejb.persistence
     *      column-name="street1"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(50)"
     */
    @Column(name="street1", length=3)
    public String getStreet1(  )
    {
       return street1;
    }

    public void setStreet1( java.lang.String street1 )
    {
       this.street1 = street1;
    }

    /**
     * @ ejb.persistence
     *      column-name="street2"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(50)"
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
     * @ ejb.persistence
     *      column-name="city"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(25)"
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
     * @ ejb.persistence
     *      column-name="state"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(3)"
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
     * @ ejb.persistence
     *      column-name="zipcode"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
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
     * @ ejb.persistence
     *      column-name="country"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(3)"
     */
    @Column(name="country", length=3)
    public java.lang.String getCountry(  )
    {
       return country;
    }

    public void setCountry( String country )
    {
       this.country = country;
    }

    /**
     * @ ejb.persistence
     *      column-name="creditCardNumber"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(25)"
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
     * @ ejb.persistence
     *      column-name="creditCardType"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(25)"
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
     * @ ejb.persistence
     *      column-name="creditCardExpiryDate"
     *      jdbc-type="VARCHAR"
     *      sql-type="varchar(10)"
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
     * @ ejb.relation
     *      name="customer-account"
     *      role-name="customer-has-account"
     *      target-ejb="Account"
     *      target-role-name="account-belongs_to-customer"
     *      target-cascade-delete="yes"
     * @ ejb.value-object
     *      compose="xpetstore.domain.signon.model.AccountValue"
     *      compose-name="AccountValue"
     *      members="xpetstore.domain.signon.interfaces.Account"
     *      members-name="AccountValue"
     *      relation="external"
     *
     * @ jboss.relation
     *      fk-column="account_fk"
     *      related-pk-field="userId"
     *      fk-contraint="${db.foreign.key}
     *
     * @ weblogic.column-map
     *      foreign-key-column="account_fk"
     */
    @ManyToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="ACCOUNT_ID")
    public Account getAccount(  )
    {
       return account;
    }

    public void setAccount( Account account )
    {
       this.account = account;
    }
    
    public String toString()
    {
       StringBuffer buffer = new StringBuffer(200);
       buffer.append("[Customer: userId " + userId);
       buffer.append(", email " + email);
       buffer.append(", account " + account);
       buffer.append("]");
       
       return buffer.toString();
    }
}
