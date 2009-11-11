/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.sandbox.performance.unit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.ejb3.test.sandbox.performance.Calculator;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class StressCreator
{
   private static final Logger log = Logger.getLogger(StressCreator.class);
   
   private static class Task implements Callable<BigDecimal>
   {
      private Calculator calculator;
      long enteredQueue;
      long start, end;

      Task(Calculator calculator)
      {
         assert calculator != null : "calculator is null";
         this.calculator = calculator;
         enteredQueue = System.currentTimeMillis();
      }

      public BigDecimal call() throws Exception
      {
         start = System.currentTimeMillis();
         try
         {
            return calculator.calculatePi(10000);
         }
         finally
         {
            end = System.currentTimeMillis();
         }
      }
   }
  
   public static double createStress(final Calculator calculator)
   {
      ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, Long.MAX_VALUE, NANOSECONDS,
            new SynchronousQueue<Runnable>());
      Task tasks[] = new Task[75];
      Future<BigDecimal> results[] = new Future[tasks.length];
      long start = System.currentTimeMillis();
      for (int i = 0; i < results.length; i++)
      {
         results[i] = executor.submit(tasks[i] = new Task(calculator));
      }
      executor.shutdown();
      try
      {
         boolean terminated = executor.awaitTermination(120, SECONDS);
         if (!terminated)
            System.err.println("Warning: not terminated");
         int failures = 0;
         for (Future<BigDecimal> result : results)
         {
            try
            {
               result.get();
            }
            catch(ExecutionException e)
            {
               log.warn(e.getCause().getMessage());
               failures++;
            }
         }
         long end = System.currentTimeMillis();
         double d = (end - start) / 1000.0;
         System.out.println("Took " + d + " seconds");

         long totalWaitQueue = 0;
         long totalExecution = 0;
         long maxWaitQueue = 0;
         long maxExecution = 0;
         for (Task task : tasks)
         {
            long waitQueue = (task.start - task.enteredQueue);
            long execution = (task.end - task.start);
            totalWaitQueue += waitQueue;
            totalExecution += execution;
            maxWaitQueue = Math.max(maxWaitQueue, waitQueue);
            maxExecution = Math.max(maxExecution, execution);
         }
         
         double t = tasks.length;
         System.out.println("Average wait queue " + ((totalWaitQueue / t) / 1000.0));
         System.out.println("Average execution " + ((totalExecution / t) / 1000.0));

         System.out.println("Max wait queue " + (maxWaitQueue / 1000.0));
         System.out.println("Max execution " + (maxExecution / 1000.0));

         if(failures > 0)
            System.out.println("WITH " + failures + " FAILURES!");
         
         return d;
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
   }
}
