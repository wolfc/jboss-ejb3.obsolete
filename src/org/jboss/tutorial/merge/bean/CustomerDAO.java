/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.merge.bean;

import javax.ejb.Remote;

import java.util.List;

@Remote
public interface CustomerDAO
{
   int create(String first, String last, String street, String city, String state, String zip);

   Customer find(int id);

   List findByLastName(String name);

   void merge(Customer c);
}
