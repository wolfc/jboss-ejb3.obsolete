/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.ejbthree1703;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Stateless
public class NoopBean implements NoopLocal
{
   private static final Logger log = Logger.getLogger(NoopBean.class);
   
   @Resource
   private SessionContext ctx;
   
   public void noop()
   {
      // really do nothing :-)
   }

   public void reentrant(int count, CyclicBarrier entree, CyclicBarrier exit)
   {
      if(count == 0)
      {
         shoo(entree, exit);
         return;
      }
      
      ctx.getBusinessObject(NoopLocal.class).reentrant(count - 1, entree, exit);
   }

   public void shoo(CyclicBarrier entree, CyclicBarrier exit)
   {
      log.debug("entering barriers");
      try
      {
         entree.await(5, SECONDS);
         exit.await(5, SECONDS);
      }
      catch(BrokenBarrierException e)
      {
         throw new RuntimeException(e);
      }
      catch(InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      catch(TimeoutException e)
      {
         throw new RuntimeException(e);
      }
   }
}
