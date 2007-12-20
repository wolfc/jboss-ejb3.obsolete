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

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.Service;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 67628 $
 */
@Service
@Remote(ServiceOneRemote.class)
@Local(ServiceOneLocal.class)
@Management(ServiceOneManagement.class)
@SecurityDomain("other")
public class ServiceOne implements ServiceOneLocal, ServiceOneRemote, ServiceOneManagement
{
   @PersistenceContext(unitName = "test")
   private EntityManager manager;
   
   static int instances = 0;
   int localMethodCalls;
   int remoteMethodCalls;
   int jmxAttribute;
   int someJmxAttribute;
   int otherJmxAttribute;
   int readWriteOnlyAttribute;
   
   @EJB StatelessRemote stateless;
   
   public void testEjbInjection()
   {
      stateless.test();
   }

   public ServiceOne()
   {
      instances++;
   }

   public int getInstances()
   {
      return instances;
   }

   public int getRemoteMethodCalls()
   {
      return remoteMethodCalls;
   }

   public void setRemoteMethodCalls(int i)
   {
      remoteMethodCalls = i;
   }

   public int getLocalMethodCalls()
   {
      return localMethodCalls;
   }

   public void setLocalMethodCalls(int i)
   {
      localMethodCalls = i;
   }

   public synchronized void localMethod(String s)
   {
      localMethodCalls++;
   }

   public synchronized void remoteMethod(String s)
   {
      remoteMethodCalls++;
   }

   public String jmxOperation(String s)
   {
      return "x" + s + "x";
   }

   public String[] jmxOperation(String[] s)
   {
      for (int i = 0 ; i < s.length ; i++)
      {
         s[i] = jmxOperation(s[i]);
      }
      return s;
   }

   public int getAttribute()
   {
      return jmxAttribute;
   }

   public void setAttribute(int i)
   {
      jmxAttribute = i;
   }

   public int getSomeAttr()
   {
      return someJmxAttribute;
   }

   public void setSomeAttr(int i)
   {
      someJmxAttribute = i;
   }

   public int getOtherAttr()
   {
      return otherJmxAttribute;
   }

   public void setOtherAttr(int i)
   {
      otherJmxAttribute = i;
   }

   public void setWriteOnly(int i)
   {
      readWriteOnlyAttribute = i;
   }

   public int getReadOnly()
   {
      return readWriteOnlyAttribute;
   }


   public void create() throws Exception
   {
      System.out.println("ServiceOne - CREATE");
      Tester.creates.add("1");
   }

   public void start() throws Exception
   {
      System.out.println("ServiceOne - START");
      Tester.starts.add("1");
      
      //test EntityManager injection
      manager.toString();
   }

   public void stop()
   {
      System.out.println("ServiceOne - STOP");
   }

   public void destroy()
   {
      System.out.println("ServiceOne - DESTROY");
   }

}
