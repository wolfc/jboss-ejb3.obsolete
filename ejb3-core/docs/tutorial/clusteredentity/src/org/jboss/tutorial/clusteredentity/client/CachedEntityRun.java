/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tutorial.clusteredentity.client;

import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.tutorial.clusteredentity.bean.Contact;
import org.jboss.tutorial.clusteredentity.bean.Customer;
import org.jboss.tutorial.clusteredentity.bean.EntityTest;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class CachedEntityRun
{
   public static void main(String[] args)throws NamingException
   {
      if (args.length != 2)
      {
         throw new RuntimeException("You need to pass in two parameters of the type ipaddress:port");
      }

      Properties prop1 = new Properties();
      prop1.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      prop1.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      prop1.put("java.naming.provider.url", "jnp://" + args[0]);

      Properties prop2 = new Properties();
      prop2.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      prop2.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      prop2.put("java.naming.provider.url", "jnp://" + args[1]);

      System.out.println("Saving customer to node 1");
      InitialContext ctx1 = new InitialContext(prop1);

      EntityTest tester1 = (EntityTest)ctx1.lookup("EntityTestBean/remote");
      Customer customer = tester1.createCustomer();
      customer = tester1.findByCustomerId(customer.getId());

      System.out.println("Looking for customer in node 2's cache");
      InitialContext ctx2 = new InitialContext(prop2);

      EntityTest tester2 = (EntityTest)ctx1.lookup("EntityTestBean/remote");

      if(!tester2.isCustomerInCache(customer.getId()))
      {
         throw new RuntimeException("Could not find Customer in node2's cache");
      }
      System.out.println("Found customer " + customer.getId() + " in node2 cache");

      if (!tester2.isCustomerContactsInCache(customer.getId()))
      {
         throw new RuntimeException("Could not find Customer contacts in node2's cache");
      }
      System.out.println("Found contacts collection for " + customer.getId() + " in node2 cache");

      Set<Contact> contacts = customer.getContacts();

      for (Contact contact : contacts)
      {
         if (!tester2.isContactInCache(contact.getId()))
         {
            throw new RuntimeException("Could not find Contact in node2's cache");
         }
         System.out.println("Found contact " + contact.getId() + " collection in node2 cache");
      }

      customer = tester2.findByCustomerId(customer.getId());
      if (customer == null)
      {
         throw new RuntimeException("Customer was not found in node 2");
      }
      System.out.println("Lookup of customer from node2 worked (via cache)");
      System.out.println("Customer: id=" + customer.getId() + "; name=" + customer.getName());

      for (Contact contact : contacts)
      {
	      System.out.println("\tContact: id=" + contact.getId() + "; name=" + contact.getName());
	  }

   }
}
