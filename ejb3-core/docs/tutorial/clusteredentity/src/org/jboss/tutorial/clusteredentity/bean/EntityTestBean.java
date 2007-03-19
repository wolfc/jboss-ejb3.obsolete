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
package org.jboss.tutorial.clusteredentity.bean;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.cache.entry.CacheEntry;
import org.hibernate.cache.entry.CollectionCacheEntry;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.jmx.CacheJmxWrapperMBean;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@Remote(EntityTest.class)
public class EntityTestBean implements EntityTest
{
   @PersistenceContext
   private EntityManager manager;

   public Customer createCustomer()
   {
      Customer customer = new Customer();
      customer.setName("JBoss");

      Set<Contact> contacts = new HashSet<Contact>();
      Contact kabir = new Contact();
      kabir.setCustomer(customer);
      kabir.setName("Kabir");
      kabir.setTlf("1111");
      contacts.add(kabir);

      Contact bill = new Contact();
      bill.setCustomer(customer);
      bill.setName("Bill");
      bill.setTlf("2222");
      contacts.add(bill);

      customer.setContacts(contacts);
      manager.persist(customer);
      return customer;
   }

   public Customer findByCustomerId(Long id)
   {
      return manager.find(Customer.class, id);
   }


   public boolean isCustomerInCache(Long id)
   {
      try
      {
         Cache cache = getCache();
         String key = "/org/jboss/tutorial/clusteredentity/bean/Customer/org.jboss.tutorial.clusteredentity.bean.Customer#" + id;
         return isInCache(cache, key);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean isContactInCache(Long id)
   {
      try
      {
         Cache cache = getCache();
         String key = "/org/jboss/tutorial/clusteredentity/bean/Contact/org.jboss.tutorial.clusteredentity.bean.Contact#" + id;

         return isInCache(cache, key);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean isCustomerContactsInCache(Long id)
   {
      try
      {
         Cache cache = getCache();
         String key = "/org/jboss/tutorial/clusteredentity/bean/Customer/contacts/org.jboss.tutorial.clusteredentity.bean.Customer.contacts#" + id;

         return isInCache(cache, key);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private Cache getCache() throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      CacheJmxWrapperMBean proxy = (CacheJmxWrapperMBean)MBeanProxyExt.create(CacheJmxWrapperMBean.class, new ObjectName("jboss.cache:service=EJB3EntityTreeCache"), server);
      return proxy.getCache();
   }

   private boolean isInCache(Cache cache, String key)throws CacheException
   {
      return isInCache(cache, null, key);
   }

   private boolean isInCache(Cache cache, Node node, String key) throws CacheException
   {
      //Not the best way to look up the cache entry, but how hibernate creates the cache entry
      //and fqn seems to be buried deep deep down inside hibernate...

      if (node == null)
      {
         node = cache.getRoot();
      }

      Set children = node.getChildren();
      for(Object child : children)
      {
         Node childNode = (Node)child;

         Fqn fqn = childNode.getFqn();
         if (fqn.toString().equals(key))
         {
            Object entry = childNode.getData().get("item");
            return (entry != null) && (entry instanceof CacheEntry || entry instanceof CollectionCacheEntry);
         }

         Set grandchildren = childNode.getChildren();
         if (grandchildren != null && grandchildren.size() > 0)
         {
            if (isInCache(cache, childNode, key))
            {
               return true;
            }
         }
      }

      return false;
   }
}
