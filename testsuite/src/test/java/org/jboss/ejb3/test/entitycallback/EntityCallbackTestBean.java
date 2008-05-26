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
package org.jboss.ejb3.test.entitycallback;

import java.util.Iterator;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContext;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Remote(EntityCallbackTest.class)
public class EntityCallbackTestBean implements EntityCallbackTest
{
   private @PersistenceContext EntityManager entityManager;

   public void createCustomer()
   {
      System.out.println("----createCustomer()");
      CallbackCounterBean.clear();
      Customer cust = new Customer("Kabir");
      entityManager.persist(cust);
      System.out.println("----createCustomer() - END");
   }

   public void addJourneysToCustomer()
   {
      System.out.println("----addJourneysToCustomer()");
      CallbackCounterBean.clear();
      Customer cust = entityManager.find(Customer.class, new Long(1));

      cust.addJourney(new TrainJourney("Oslo", "London", "T1"));
      cust.addJourney(new TrainJourney("Oslo", "London", "T2"));
      cust.addJourney(new BusJourney("Oslo", "London", "B1"));
      cust.addJourney(new BusJourney("Oslo", "London", "B2"));
      cust.addJourney(new BusJourney("Oslo", "London", "B3"));
      System.out.println("----addJourneysToCustomer() - END");
   }

   public void updateCustomer()
   {
      System.out.println("----updateCustomer()");
      CallbackCounterBean.clear();
      Customer cust = entityManager.find(Customer.class, new Long(1));

      cust.setName("Kabir Khan");
      System.out.println("----updateCustomer() -END");
   }

   public void updateOneTrainJourney()
   {
      System.out.println("----updateOneTrainJourney()");
      CallbackCounterBean.clear();
      Customer cust = entityManager.find(Customer.class, new Long(1));

      for (Iterator it = cust.getJourneys().iterator(); it.hasNext();)
      {
         Object obj = it.next();
         if (obj instanceof TrainJourney)
         {
            TrainJourney trainJourney = (TrainJourney) obj;
            String train = trainJourney.getTrain();
            trainJourney.setTrain(train + "-1");
            entityManager.flush();
            break;
         }
      }
      System.out.println("----updateOneTrainJourney() - END");
   }

   public void updateAllBusJourneys()
   {
      System.out.println("----updateAllBusJourneys()");
      CallbackCounterBean.clear();
      Customer cust = entityManager.find(Customer.class, new Long(1));

      for (Iterator it = cust.getJourneys().iterator(); it.hasNext();)
      {
         Object obj = it.next();
         if (obj instanceof BusJourney)
         {
            BusJourney busJourney = (BusJourney) obj;
            String bus = busJourney.getBus();
            busJourney.setBus(bus + "-1");
         }
      }
      System.out.println("----updateAllBusJourneys() - EN");
   }

   public void updateEverything()
   {
      System.out.println("----updateEverything()");
      CallbackCounterBean.clear();
      Customer cust = entityManager.find(Customer.class, new Long(1));
      cust.setName("Kabir");

      for (Iterator<Journey> it = cust.getJourneys().iterator(); it.hasNext();)
      {
         Journey journey = it.next();
         journey.setDest("Somewhere fun and sunny");
      }
      System.out.println("----updateEverything() - END");
   }

   public void createAndDeleteCustomer()
   {
      System.out.println("----createAndDeleteCustomer()");

      CallbackCounterBean.clear();
      Customer cust = new Customer("Djengis");
      entityManager.persist(cust);

      entityManager.remove(cust);
      System.out.println("----createAndDeleteCustomer()");
   }


   public void deleteSomeJourneys()
   {
      System.out.println("----deleteSomeJourneys()");
      CallbackCounterBean.clear();
      Customer cust = entityManager.find(Customer.class, new Long(1));

      boolean deletedBus = false;
      boolean deletedTrain = false;

      for (Iterator<Journey> it = cust.getJourneys().iterator(); it.hasNext();)
      {
         Journey journey = (Journey) it.next();

         if (journey instanceof BusJourney && !deletedBus)
         {
            it.remove();
            entityManager.remove(journey);
            deletedBus = true;
         }
         else if (journey instanceof TrainJourney && !deletedTrain)
         {
            it.remove();
            entityManager.remove(journey);
            deletedTrain = true;
         }
      }
      System.out.println("----deleteSomeJourneys() - END");
   }

   public void deleteCustomerAndJourneys()
   {
      System.out.println("----deleteCustomerAndJourneys()");
      CallbackCounterBean.clear();
      Customer cust = entityManager.find(Customer.class, new Long(1));
      entityManager.remove(cust);
      System.out.println("----deleteCustomerAndJourneys() - END");
   }

}
