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
package org.jboss.ejb3.core.test.ejbthree1846;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Stateful
@CacheConfig(idleTimeoutSeconds=1)
public class MyStatefulBean implements MyStateful
{
   private static final Logger log = Logger.getLogger(MyStatefulBean.class);
   
   /*
    *  for unit test only
    */
   public static volatile int activations = 0;
   public static volatile int passivations = 0;
   
   public static CyclicBarrier barrier = new CyclicBarrier(2);
   
   @Resource
   private SessionContext ctx;
   
   public MyStateful getBusinessObject()
   {
      return ctx.getBusinessObject(MyStateful.class);
   }
   
   @PostActivate
   public void postActivate()
   {
      log.info("postActivate");
      activations++;
   }
   
   @PrePassivate
   public void prePassivate()
   {
      log.info("prePassivate");
      passivations++;
      try
      {
         barrier.await(5000, TimeUnit.MILLISECONDS);
      }
      catch (BrokenBarrierException e)
      {
         throw new RuntimeException(e);
      }
      catch(InterruptedException e)
      {
         throw new RuntimeException(e);
      }
      catch (TimeoutException e)
      {
         throw new RuntimeException(e);
      }
   }
}
