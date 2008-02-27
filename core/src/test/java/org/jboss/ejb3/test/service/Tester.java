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
package org.jboss.ejb3.test.service;


import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.system.ServiceMBeanSupport;

import javax.naming.InitialContext;

import java.io.File;
import java.util.ArrayList;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Tester extends ServiceMBeanSupport implements TesterMBean
{
   public static ArrayList creates = new ArrayList();
   public static ArrayList starts = new ArrayList();
   
   public void testLocalServiceWithInterfaceAnnotation() throws Exception
   {
      final int count = 15;
      final InitialContext ctx = new InitialContext();
      ServiceSevenLocal test = (ServiceSevenLocal) ctx.lookup("ServiceSeven/local");
      test.setLocalMethodCalls(0);

      Thread[] threads = new Thread[count];
      for (int i = 0 ; i < count ; i++)
      {
         final int outer = i;
         threads[i] = new Thread(
               new Runnable()
               {
                  public void run()
                  {
                     try
                     {
                        ServiceSevenLocal test = (ServiceSevenLocal) ctx.lookup("ServiceSeven/local");
                        for (int j = 0 ; j < count ; j++)
                        {
                           String s = outer + "_" + j;
                           //System.out.println(s);
                           test.localMethod(s);
                        }
                     }
                     catch(Exception e)
                     {
                        throw new RuntimeException(e);
                     }
                  }
               }
            );
         threads[i].start();
      }

      Thread.sleep(5000);
      for (int i = 0 ; i < count ; i++)
      {
         threads[i].join();
      }

      if (test.getInstances() != 1)
      {
         throw new RuntimeException("There should only ever be one instance of the service. We have " + test.getInstances());
      }

      int localCalls = test.getLocalMethodCalls();
      if (localCalls != (count * count))
      {
         throw new RuntimeException("There should be " + count * count + " local method calls, not " + localCalls);
      }
   }

   public void testServiceWithDefaultLocalJNDIName() throws Exception
   {
      final int count = 15;
      
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      final InitialContext ctx = new InitialContext();
      ServiceOneLocal test = (ServiceOneLocal) ctx.lookup("ServiceOne/local");
      test.setLocalMethodCalls(0);

      Thread[] threads = new Thread[count];
      for (int i = 0 ; i < count ; i++)
      {
         final int outer = i;
         threads[i] = new Thread(
               new Runnable()
               {
                  public void run()
                  {
                     try
                     {
                        ServiceOneLocal test = (ServiceOneLocal) ctx.lookup("ServiceOne/local");
                        for (int j = 0 ; j < count ; j++)
                        {
                           String s = outer + "_" + j;
                           //System.out.println(s);
                           test.localMethod(s);
                        }
                     }
                     catch(Exception e)
                     {
                        throw new RuntimeException(e);
                     }
                  }
               }
            );
         threads[i].start();
      }

      Thread.sleep(5000);
      for (int i = 0 ; i < count ; i++)
      {
         threads[i].join();
      }

      if (test.getInstances() != 1)
      {
         throw new RuntimeException("There should only ever be one instance of the service. We have " + test.getInstances());
      }

      int localCalls = test.getLocalMethodCalls();
      if (localCalls != (count * count))
      {
         throw new RuntimeException("There should be " + count * count + " local method calls, not " + localCalls);
      }
   }

   public void testServiceWithLocalBinding() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ServiceTwoLocal test = (ServiceTwoLocal) ctx.lookup("serviceTwo/local");
      test.setCalled(false);
      if (test.getCalled()) throw new RuntimeException("Called should be false, not " + test.getCalled());
      test.localMethod();
      if (!test.getCalled()) throw new RuntimeException("Called should be true, not " + test.getCalled());
   }
   
   public void testDeploymentDescriptorServiceWithLocalBinding() throws Exception
   {
      InitialContext ctx = new InitialContext();
      ServiceSixLocal test = (ServiceSixLocal) ctx.lookup("serviceSix/local");
      test.setCalled(false);
      if (test.getCalled()) throw new RuntimeException("Called should be false, not " + test.getCalled());
      test.localMethod();
      if (!test.getCalled()) throw new RuntimeException("Called should be true, not " + test.getCalled());
   }

   public ArrayList getCreates()
   {
      return creates;
   }

   public ArrayList getStarts()
   {
      return starts;
   }

}
