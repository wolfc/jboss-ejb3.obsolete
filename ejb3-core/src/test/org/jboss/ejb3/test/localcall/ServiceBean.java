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

package org.jboss.ejb3.test.localcall;

import javax.ejb.Remote;
import javax.ejb.Local;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.Service;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Service
@Remote(ServiceRemote.class)
@Local(ServiceLocal.class)
public class ServiceBean implements ServiceLocal, ServiceRemote
{
   public void test()
   {
      
   }
   
   public void testLocal()throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatefulLocal stateful1 = (StatefulLocal)ctx.lookup("StatefulBean/local");
      StatefulLocal stateful2 = (StatefulLocal)ctx.lookup("StatefulBean/local");
      
      StatefulClustered statefulClustered1 = (StatefulClustered)ctx.lookup("StatefulClusteredBean/remote");
      StatefulClustered statefulClustered2 = (StatefulClustered)ctx.lookup("StatefulClusteredBean/remote");
      
      StatelessLocal stateless1 = (StatelessLocal)ctx.lookup("StatelessBean/local");
      StatelessLocal stateless2 = (StatelessLocal)ctx.lookup("StatelessBean/local");
      
      StatelessClustered statelessClustered1 = (StatelessClustered)ctx.lookup("StatelessClusteredBean/remote");
      StatelessClustered statelessClustered2 = (StatelessClustered)ctx.lookup("StatelessClusteredBean/remote");
      
      ServiceLocal service1 = (ServiceLocal)ctx.lookup("ServiceBean/local");
      ServiceLocal service2 = (ServiceLocal)ctx.lookup("ServiceBean/local");
      
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
   
   private void assertFalse(boolean b)
   {
      if (b)
      {
         throw new RuntimeException("Assertion failed!");
      }
   }

   private void assertTrue(boolean b)
   {
      if (!b)
      {
         throw new RuntimeException("Assertion failed!");
      }
   }
   
}
