/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.dependent.bean;

import javax.ejb.DependentObject;

@DependentObject
        public class Name implements java.io.Serializable
{
   private String first;
   private String last;

   public Name()
   {
   }

   public Name(String first, String last)
   {
      this.first = first;
      this.last = last;
   }

   public String getFirst()
   {
      return first;
   }

   public void setFirst(String first)
   {
      this.first = first;
   }

   public String getLast()
   {
      return last;
   }

   public void setLast(String last)
   {
      this.last = last;
   }
}
