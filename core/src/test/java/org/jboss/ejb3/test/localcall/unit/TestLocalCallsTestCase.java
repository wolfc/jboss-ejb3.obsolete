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

package org.jboss.ejb3.test.localcall.unit;

import javax.naming.InitialContext;
import org.jboss.ejb3.test.localcall.ServiceRemote;
import org.jboss.ejb3.test.localcall.StatefulClustered;
import org.jboss.ejb3.test.localcall.StatefulRemote;
import org.jboss.ejb3.test.localcall.StatelessClustered;
import org.jboss.ejb3.test.localcall.StatelessRemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public class TestLocalCallsTestCase extends JBossTestCase
{
   public TestLocalCallsTestCase(String name)
   {
      super(name);
      
   }
   
   public void testRemoteIfLocalCalls() throws Exception
   {
      System.out.println("Testing local");
      InitialContext ctx = getInitialContext();
      StatefulRemote stateful1 = (StatefulRemote)ctx.lookup("StatefulBean/remote");
      StatefulRemote stateful2 = (StatefulRemote)ctx.lookup("StatefulBean/remote");
      
      StatefulClustered statefulClustered1 = (StatefulClustered)ctx.lookup("StatefulClusteredBean/remote");
      StatefulClustered statefulClustered2 = (StatefulClustered)ctx.lookup("StatefulClusteredBean/remote");
      
      StatelessRemote stateless1 = (StatelessRemote)ctx.lookup("StatelessBean/remote");
      StatelessRemote stateless2 = (StatelessRemote)ctx.lookup("StatelessBean/remote");
      
      StatelessClustered statelessClustered1 = (StatelessClustered)ctx.lookup("StatelessClusteredBean/remote");
      StatelessClustered statelessClustered2 = (StatelessClustered)ctx.lookup("StatelessClusteredBean/remote");
      
      ServiceRemote service1 = (ServiceRemote)ctx.lookup("ServiceBean/remote");
      ServiceRemote service2 = (ServiceRemote)ctx.lookup("ServiceBean/remote");
      
      stateful1.test();
      stateful2.test();
      statefulClustered1.test();
      statefulClustered2.test();
      stateless1.test();
      stateless2.test();
      statelessClustered1.test();
      statelessClustered2.test();
      service1.test();
      service2.test();
            
      assertFalse(stateful1.hashCode() ==  stateful2.hashCode());
      assertFalse(statefulClustered1.hashCode() == statefulClustered2.hashCode());
      assertTrue(stateless1.hashCode() == stateless2.hashCode());
      assertTrue(statelessClustered1.hashCode() == statelessClustered2.hashCode());
      assertTrue(service1.hashCode() == service2.hashCode());
      
      assertFalse(stateful1.equals(stateful2));
      assertFalse(statefulClustered1.equals(statefulClustered2));
      assertTrue(stateless1.equals(stateless2));
      assertTrue(statelessClustered1.equals(statelessClustered2));
      assertTrue(service1.equals(service2));
      
   }
   
   public void testLocalIfLocalCalls() throws Exception
   {
      System.out.println("TESTING LOCAL CALLS");
      InitialContext ctx = getInitialContext();
      ServiceRemote service = (ServiceRemote)ctx.lookup("ServiceBean/remote");
      service.testLocal();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(TestLocalCallsTestCase.class, "localcall-test.jar");
   }
}
