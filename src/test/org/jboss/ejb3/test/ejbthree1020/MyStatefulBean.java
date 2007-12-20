/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1020;

import javax.ejb.Init;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 64465 $
 */
@Stateful
public class MyStatefulBean implements MyStateful
{
   private static Object lock = new Object();
   
   private int id;
   
   @Init
   public void create(int id)
   {
      this.id = id;
   }
   
   public int getId()
   {
      return id;
   }
   
   public MyStateful[] method1(int expectedId)
   {
      if(expectedId != id)
         throw new RuntimeException("ids don't match");
      
      try
      {
         MyStateful bos[] = new MyStateful[2];
         
         SessionContext ctx1 = (SessionContext) new InitialContext().lookup("java:comp/EJBContext");
         bos[0] = ctx1.getBusinessObject(MyStateful.class);
         
         synchronized(lock)
         {
            lock.wait(5000);
         }
         
         SessionContext ctx2 = (SessionContext) new InitialContext().lookup("java:comp/EJBContext");
         bos[1] = ctx2.getBusinessObject(MyStateful.class);
         
         System.out.println("ctx1 = " + ctx1);
         System.out.println("ctx2 = " + ctx2);
         
         return bos;
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public MyStateful method2(int expectedId)
   {
      if(expectedId != id)
         throw new RuntimeException("ids don't match");
      
      try
      {
         SessionContext ctx1 = (SessionContext) new InitialContext().lookup("java:comp/EJBContext");
         MyStateful bo = ctx1.getBusinessObject(MyStateful.class);
         
         synchronized (lock)
         {
            lock.notify();
         }
         
         return bo;
      }
      catch(NamingException e)
      {
         throw new RuntimeException(e);
      }
   }
}
