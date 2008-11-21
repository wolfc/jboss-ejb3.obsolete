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
package org.jboss.ejb3.test.entitycallback.unit;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import junit.framework.Test;

import org.jboss.ejb3.test.entitycallback.CallbackCounter;
import org.jboss.ejb3.test.entitycallback.EntityCallbackTest;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class EntityCallbackUnitTestCase
      extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;

   class CallbackCounts
   {
      String entity;
      String test;

      int preCreate;
      int postCreate;
      int preRemove;
      int postRemove;
      int preUpdate;
      int postUpdate;
      int postLoad;

      void checkCallbackCounts(int preCreate, int postCreate, int preRemove, int postRemove, int preUpdate, int postUpdate, int postLoad)
      {
         assertEquals("Wrong number of preCreate for " + entity + " for test '" + test + "'", preCreate, this.preCreate);
         assertEquals("Wrong number of postCreate for " + entity + " for test '" + test + "'", postCreate, this.postCreate);
         assertEquals("Wrong number of preRemove for " + entity + " for test '" + test + "'", preRemove, this.preRemove);
         assertEquals("Wrong number of postRemove for " + entity + " for test '" + test + "'", postRemove, this.postRemove);
         assertEquals("Wrong number of preUpdate for " + entity + " for test '" + test + "'", preUpdate, this.preUpdate);
         assertEquals("Wrong number of postUpdate for " + entity + " for test '" + test + "'", postUpdate, this.postUpdate);
         assertEquals("Wrong number of postLoad for " + entity + " for test '" + test + "'", postLoad, this.postLoad);
      }
   }



   public EntityCallbackUnitTestCase(String name)
   {
      super(name);
   }

   public void test() throws Exception
   {
      System.out.println("testing insertion");
      EntityCallbackTest test = (EntityCallbackTest) this.getInitialContext().lookup("EntityCallbackTestBean/remote");


      System.out.println("***** testing createCustomer() *****");
      String current = "createCustomer";
      test.createCustomer();
      CallbackCounts customerCounts = getCallbackCounts(current, "Customer");
      CallbackCounts journeyCounts = getCallbackCounts(current, "Journey");
      CallbackCounts trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      CallbackCounts busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(1, 1, 0, 0, 0, 0, 0);
      journeyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);
      trainJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);


      System.out.println("***** testing addJourneysToCustomer() *****");
      current = "addJourneysToCustomer";
      test.addJourneysToCustomer();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 1);
      journeyCounts.checkCallbackCounts(5, 5, 0, 0, 0, 0, 0);
      trainJourneyCounts.checkCallbackCounts(2, 2, 0, 0, 0, 0, 0);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);


      System.out.println("***** testing updateCustomer() *****");
      current = "updateCustomer";
      test.updateCustomer();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(0, 0, 0, 0, 1, 1, 1);
      journeyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 5);
      trainJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 2);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);


      System.out.println("***** testing updateOneTrainJourney() *****");
      current = "updateOneTrainJourney";
      test.updateOneTrainJourney();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 1);
      journeyCounts.checkCallbackCounts(0, 0, 0, 0, 1, 1, 5);
      trainJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 1, 1, 2);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);


      System.out.println("***** testing updateAllBusJourneys() *****");
      current = "updateAllBusJourneys";
      test.updateAllBusJourneys();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 1);
      journeyCounts.checkCallbackCounts(0, 0, 0, 0, 3, 3, 5);
      trainJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 2);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);


      System.out.println("***** testing updateEverything() *****");
      current = "updateEverything";
      test.updateEverything();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(0, 0, 0, 0, 1, 1, 1);
      journeyCounts.checkCallbackCounts(0, 0, 0, 0, 5, 5, 5);
      trainJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 2, 2, 2);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);


      System.out.println("***** testing createAndDeleteCustomer() *****");
      current = "createAndDeleteCustomer";
      test.createAndDeleteCustomer();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(1, 1, 1, 1, 0, 0, 0);
      journeyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);
      trainJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);


      System.out.println("***** testing deleteSomeJourneys() *****");
      current = "deleteSomeJourneys";
      test.deleteSomeJourneys();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 1);
      journeyCounts.checkCallbackCounts(0, 0, 2, 2, 0, 0, 5);
      trainJourneyCounts.checkCallbackCounts(0, 0, 1, 1, 0, 0, 2);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);



      System.out.println("***** testing deleteCustomerAndJourneys() *****");
      current = "deleteCustomerAndJourneys";
      test.deleteCustomerAndJourneys();
      customerCounts = getCallbackCounts(current, "Customer");
      journeyCounts = getCallbackCounts(current, "Journey");
      trainJourneyCounts = getCallbackCounts(current, "TrainJourney");
      busJourneyCounts = getCallbackCounts(current, "BusJourney");

      //Order of parameters for check method is:
      //preCreate, postCreate, preRemove, postRemove, preUpdate, postUpdate, postLoad
      customerCounts.checkCallbackCounts(0, 0, 1, 1, 0, 0, 1);
      journeyCounts.checkCallbackCounts(0, 0, 3, 3, 0, 0, 3);
      trainJourneyCounts.checkCallbackCounts(0, 0, 1, 1, 0, 0, 1);
      busJourneyCounts.checkCallbackCounts(0, 0, 0, 0, 0, 0, 0);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityCallbackUnitTestCase.class, "entitycallback-test.jar");
   }

   private CallbackCounts getCallbackCounts(String test, String entity)throws Exception
   {
      CallbackCounter callbackCounter = (CallbackCounter)this.getInitialContext().lookup("CallbackCounterBean/remote");
      CallbackCounts counts = new CallbackCounts();
      counts.entity = entity;
      counts.test = test;
      counts.preCreate = callbackCounter.getCallbacks(entity, PrePersist.class);
      counts.postCreate = callbackCounter.getCallbacks(entity, PostPersist.class);
      counts.preRemove = callbackCounter.getCallbacks(entity, PreRemove.class);
      counts.postRemove = callbackCounter.getCallbacks(entity, PostRemove.class);
      counts.preUpdate = callbackCounter.getCallbacks(entity, PreUpdate.class);
      counts.postUpdate = callbackCounter.getCallbacks(entity, PostUpdate.class);
      counts.postLoad = callbackCounter.getCallbacks(entity, PostLoad.class);

      return counts;
   }
}
