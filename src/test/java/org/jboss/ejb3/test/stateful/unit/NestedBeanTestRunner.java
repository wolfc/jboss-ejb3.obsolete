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
package org.jboss.ejb3.test.stateful.unit;

import java.rmi.dgc.VMID;

import javax.naming.InitialContext;

import junit.framework.Assert;

import org.jboss.ejb3.test.stateful.nested.base.std.NestedBeanMonitor;
import org.jboss.ejb3.test.stateful.nested.base.std.NestedStateful;
import org.jboss.ejb3.test.stateful.nested.base.std.ParentStatefulRemote;
import org.jboss.logging.Logger;

/**
 * Encapsulates the functions needed for tests of nested SFSBs in
 * a class that can be used either by a JBossTestCase subclass or
 * a JBossClusteredTestCase subclass.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1.1 $
 */
public class NestedBeanTestRunner extends BaseTestRunner
{
   public NestedBeanTestRunner(InitialContext context, Logger log)
   {
      super(context, log);
   }
   
   
   public void testBasic()
   throws Exception
   {
      getLog().debug("Running testBasic()");
      getLog().debug("==================================");
      
      NestedBeanSet beanSet = getNestedBeanSet();
      ParentStatefulRemote stateful = beanSet.parent;
      
      Assert.assertEquals("Remote counter: ", 1, stateful.increment());
      Assert.assertEquals("Remote counter: ", 2, stateful.increment());
      Assert.assertEquals("Local counter: ", 1, stateful.incrementLocal());
      Assert.assertEquals("Local counter: ", 2, stateful.incrementLocal());
      
      removeBean(stateful);
      
      getLog().debug("ok");
   }
   
   public void testDependentLifecycle()
   throws Exception
   {
      getLog().debug("Running testDependentLifecycle()");
      getLog().debug("==================================");
      
      NestedBeanSet beanSet = getNestedBeanSet();
      ParentStatefulRemote parent = beanSet.parent;
      NestedStateful nested = beanSet.nested;
      NestedBeanMonitor monitor = beanSet.monitor;
      
      Assert.assertEquals("Remote counter (via parent): ", 1, parent.increment());  
      
      String deepId = monitor.getDeepNestedId();
      Assert.assertNotNull("Got a deepId", deepId);
      Assert.assertFalse("Got a non-ERROR deepId", "ERROR".equals(deepId));
      String localDeepId = monitor.getLocalDeepNestedId();
      Assert.assertNotNull("Got a localDeepId", localDeepId);
      Assert.assertFalse("Got a non-ERROR localDeepId", "ERROR".equals(localDeepId));
       
      removeBean(parent);
       
      // Confirm parent is dead
      Assert.assertEquals("Remote counter (via dead parent): ", -1, monitor.incrementParent());
      Assert.assertFalse("parent.remove() fails", monitor.removeParent());
      
      // Confirm nested beans still work following parent remove
      Assert.assertEquals("Remote counter (direct):", 2 ,nested.increment());      
      Assert.assertEquals("Local counter (monitor):", 1 , monitor.incrementLocalNested());
      Assert.assertEquals("Deep nested id", deepId, monitor.getDeepNestedId());
      Assert.assertEquals("Local deep nested id", localDeepId, monitor.getLocalDeepNestedId());
      
      // Remove the bottom tier
      Assert.assertTrue("Local deep nested removed", monitor.removeLocalDeepNested());
      
      // Confirm it is dead
      Assert.assertEquals("Local deep nested removed", "ERROR", monitor.getLocalDeepNestedId());
      Assert.assertFalse("localDeepNested.remove() fails", monitor.removeLocalDeepNested());
      
      // Confirm other beans still work following parent remove
      Assert.assertEquals("Remote counter (direct):", 3 ,nested.increment());      
      Assert.assertEquals("Local counter (monitor):", 2 , monitor.incrementLocalNested());
      Assert.assertEquals("Deep nested id", deepId, monitor.getDeepNestedId());
      
      // Remove the nested bean
      removeBean(nested);
      
      Assert.assertEquals("Remote counter (via monitor): ", -1, monitor.incrementNested());
      Assert.assertFalse("nested.remove() fails", monitor.removeNested());
      
      // Confirm other beans still work
      Assert.assertEquals("Local counter (monitor):", 3 , monitor.incrementLocalNested());
      Assert.assertEquals("Deep nested id", deepId, monitor.getDeepNestedId());
      
      // Remove the local nested bean
      Assert.assertTrue("Local nested removed", monitor.removeLocalNested());
      
      // Confirm it is dead
      Assert.assertEquals("Local nested removed", -1, monitor.incrementLocalNested());
      Assert.assertFalse("localNested.remove() fails", monitor.removeLocalNested());
      
      // Confirm other beans still work following parent remove
      Assert.assertEquals("Deep nested id", deepId, monitor.getDeepNestedId());
      
      // Remove the deep nested bean
      Assert.assertTrue("Deep removed", monitor.removeDeepNested());
      
      // Confirm it is dead
      Assert.assertEquals("Local nested removed", "ERROR", monitor.getDeepNestedId());
      Assert.assertFalse("deepNested.remove() fails", monitor.removeDeepNested());
      
      removeBean(monitor);
      
      getLog().debug("ok");
      
   }

   public void testStatefulPassivation()
   throws Exception
   {
      getLog().debug("Running testStatefulPassivation()");
      getLog().debug("==================================");
      
      NestedBeanSet beanSet = getNestedBeanSet();
      NestedBeanMonitor monitor = beanSet.monitor;
      ParentStatefulRemote parent = beanSet.parent;
      NestedStateful nested = beanSet.nested;
      
      int parentInv = beanSet.parentInvocations;
      int nestedInv = beanSet.nestedInvocations;

      Assert.assertEquals("Remote counter: ", 1, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Remote counter: ", 2, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Local counter: ", 1, parent.incrementLocal());
      parentInv++;
      Assert.assertEquals("Local counter: ", 2, parent.incrementLocal());
      parentInv++;
      
      sleep(getSleepTime());  // should passivate

      Assert.assertEquals("Parent passivate count: ",
                          getExpectedPassivations(1, parentInv), 
                          parent.getPrePassivate());
      parentInv++;
      Assert.assertEquals("Parent activate count: ",
                          getExpectedPassivations(1, parentInv), 
                          parent.getPostActivate());
      parentInv++;
      Assert.assertEquals("Remote nested passivate count: ",
                          getExpectedPassivations(1, nestedInv), 
                          nested.getPrePassivate());
      Assert.assertEquals("Remote nested activate count: ",
                          getExpectedPassivations(1, nestedInv), 
                          nested.getPostActivate());
      Assert.assertEquals("Local nested passivate count: ",
                          getExpectedPassivations(1, parentInv), 
                          parent.getLocalNestedPrePassivate());
      parentInv++;
      Assert.assertEquals("Local nested activate count: ",
                          getExpectedPassivations(1, parentInv), 
                          parent.getLocalNestedPostActivate());
      parentInv++;
      
      // Use the monitor to check the deep nested beans.  In a cluster these
      // are marked not to treat replication as passivation, so we ignore 
      // the number of invocations
      Assert.assertEquals("Deep nested passivate count: ", 1, monitor.getDeepNestedPassivations());
      nestedInv++;
      Assert.assertEquals("Deep nested activate count: ", 1, monitor.getDeepNestedActivations());
      nestedInv++;
      Assert.assertEquals("Local deep nested passivate count: ", 1, monitor.getLocalDeepNestedPassivations());
      parentInv++;
      Assert.assertEquals("Local deep nested activate count: ", 1, monitor.getLocalDeepNestedActivations());
      parentInv++;
      
      Assert.assertEquals("Remote counter: ", 3, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Remote counter: ", 4, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Local counter: ", 3, parent.incrementLocal());
      parentInv++;
      Assert.assertEquals("Local counter: ", 4, parent.incrementLocal());  
      parentInv++;    
      
      sleep(getSleepTime());  // should passivate

      Assert.assertEquals("Parent passivate count: ",
                          getExpectedPassivations(2, parentInv), 
                          parent.getPrePassivate());
      parentInv++;
      Assert.assertEquals("Parent activate count: ",
                          getExpectedPassivations(2, parentInv), 
                          parent.getPostActivate());
      parentInv++;
      Assert.assertEquals("Remote nested passivate count: ",
                          getExpectedPassivations(2, nestedInv), 
                          nested.getPrePassivate());
      Assert.assertEquals("Remote nested activate count: ",
                          getExpectedPassivations(2, nestedInv), 
                          nested.getPostActivate());
      Assert.assertEquals("Local nested passivate count: ",
                          getExpectedPassivations(2, parentInv), 
                          parent.getLocalNestedPrePassivate());
      parentInv++;
      Assert.assertEquals("Local nested activate count: ",
                          getExpectedPassivations(2, parentInv), 
                          parent.getLocalNestedPostActivate());
      parentInv++;
      
      // Use the monitor to check the deep nested beans.
      Assert.assertEquals("Deep nested passivate count: ", 2, monitor.getDeepNestedPassivations());
      nestedInv++;
      Assert.assertEquals("Deep nested activate count: ", 2, monitor.getDeepNestedActivations());
      nestedInv++;
      Assert.assertEquals("Local deep nested passivate count: ", 2, monitor.getLocalDeepNestedPassivations());
      parentInv++;
      Assert.assertEquals("Local deep nested activate count: ", 2, monitor.getLocalDeepNestedActivations());
      parentInv++;
      
      Assert.assertEquals("Remote counter: ", 5, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Remote counter: ", 6, parent.increment());
      parentInv++;
      nestedInv++;
      Assert.assertEquals("Local counter: ", 5, parent.incrementLocal());
      parentInv++;
      Assert.assertEquals("Local counter: ", 6, parent.incrementLocal());
      parentInv++;
      
      removeBean(parent);
      
      // Force the nested bean to go through another passivation
      // to check the activation process can survive the removal 
      // of its parent
      sleep(getSleepTime());
      
      // Confirm nested still works following parent remove
      Assert.assertEquals("Remote counter (direct): ", 7, nested.increment());
      nestedInv++;
      Assert.assertEquals("Remote nested passivate count: ",
                          getExpectedPassivations(3, nestedInv), 
                          nested.getPrePassivate());
      Assert.assertEquals("Remote nested activate count: ",
                          getExpectedPassivations(3, nestedInv), 
                          nested.getPostActivate());
      
      removeBean(nested);
      
      getLog().debug("ok");
   }
   
   public NestedBeanSet getNestedBeanSet()
   throws Exception
   {
      // We want a colocated monitor and SFSB to test passivation/destruction
      // StatelessRemoteBean is not clustered, so once we have it's VMID
      // we can keep creating SFSBs until we get one we want
      getLog().debug("Looking up NestedBeanMonitorBean...");
      NestedBeanMonitor monitor = (NestedBeanMonitor) getInitialContext().lookup("NestedBeanMonitorBean/remote");
      VMID monitorVM = monitor.getVMID();
      addRemovable(monitor);
      
      int parentInv = 0;
      int nestedInv = 0;
      
      ParentStatefulRemote parent = null;      
      boolean vmMatch = false;
      for (int i = 0; i < 20 && !vmMatch; i++)
      {
         getLog().debug("Looking up testParentStateful... Attempt " + (i + 1));
         parent = (ParentStatefulRemote) initialContext.lookup("testParentStateful/remote");
         
         vmMatch = monitorVM.equals(parent.getVMID());
         addRemovable(parent);
         if (!vmMatch)
         {
            removeBean(parent);
         }
      }
      
      Assert.assertTrue("Monitor and SFSB in same VM", vmMatch);
      
      // Reset the parent to initialize all the beans
      parent.reset();
      
      // Monitor the parent
      monitor.monitor(parent);
      
      // Reset the parent to clean things up
      parent.reset();
      parentInv++;
      nestedInv++;
      
      NestedStateful nested = (NestedStateful) parent.getNested();
      parentInv++;
      addRemovable(nested);
      
      NestedBeanSet result = new NestedBeanSet();
      result.monitor = monitor;
      result.parent = parent;
      result.nested = nested;
      result.parentInvocations = parentInv;
      result.nestedInvocations = nestedInv;
      
      return result;
   }
   
   

   public static class NestedBeanSet
   {
      public NestedBeanMonitor monitor;
      public ParentStatefulRemote parent;
      public NestedStateful nested;
      public int parentInvocations = 0;
      public int nestedInvocations = 0;
   }
   
}
