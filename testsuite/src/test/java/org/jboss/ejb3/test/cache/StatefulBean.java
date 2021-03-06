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
package org.jboss.ejb3.test.cache;

import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.naming.InitialContext;

import org.jboss.ejb3.annotation.Cache;
import org.jboss.ejb3.annotation.CacheConfig;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateful
@Cache("StatefulTreeCache")
@CacheConfig(name = "jboss.cache:service=EJB3SFSBClusteredCache", maxSize = 1000, idleTimeoutSeconds = 1)
@Local(StatefulLocal.class)
@Remote(StatefulRemote.class)
public class StatefulBean implements StatefulRemote, java.io.Serializable, StatefulLocal
{
   public static boolean postActivateCalled = false;
   public static boolean prePassivateCalled = false;

   private String state;

   public long bench(int iterations) throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatefulLocal local = (StatefulLocal) ctx.lookup("StatefulBean/local");
      long start = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++)
      {
         local.getState();
      }
      long end = System.currentTimeMillis() - start;
      System.out.println(iterations + " stateful iterations took: " + end);
      return end;
   }

   public void longRunning() throws Exception
   {
      Thread.sleep(11000);
   }

   @Remove
   public void done() {
   }

   public boolean getPostActivate()
   {
      return postActivateCalled;
   }

   public boolean getPrePassivate()
   {
      return prePassivateCalled;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   public String getState()
   {
      return this.state;
   }

   public void reset()
   {
      state = null;
      postActivateCalled = false;
      prePassivateCalled = false;
   }

   @PostActivate
   public void postActivate()
   {
      postActivateCalled = true;
   }

   @PrePassivate
   public void prePassivate()
   {
      prePassivateCalled = true;
   }


}
