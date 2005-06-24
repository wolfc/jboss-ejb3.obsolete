//$Id$
package org.jboss.tutorial.extended.bean;

import javax.persistence.Entity;
import javax.persistence.GeneratorType;
import javax.persistence.Id;

/**
 * Company customer
 *
 * @author Emmanuel Bernard
 */
@Entity
public class Customer implements java.io.Serializable
{
   long id;
   String name;

   public
   Customer()
   {
   }

   @Id(generate = GeneratorType.IDENTITY)
   public
   long getId()
   {
      return id;
   }

   public
   String getName()
   {
      return name;
   }

   public
   void setId(long long1)
   {
      id = long1;
   }

   public
   void setName(String string)
   {
      name = string;
   }

}

