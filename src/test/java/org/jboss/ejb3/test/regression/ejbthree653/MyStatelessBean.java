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
package org.jboss.ejb3.test.regression.ejbthree653;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Stateless
@Remote(MyStateless.class)
public class MyStatelessBean
{
   private static long currentThreadId;
   
   /**
    * EJBCreate method.
    */
   @PostConstruct
   public void ejb3Create() 
   {
      System.out.println("New SessionBean " + this);
      System.out.println("Thread " + Thread.currentThread().getId());
      //setupBean();
      if(currentThreadId == 0)
      {
         currentThreadId = Thread.currentThread().getId();
      }
      else
      {
         if(Thread.currentThread().getId() == currentThreadId)
         {
            throw new IllegalStateException("no new instance should be created");
         }
         else
         {
            // could happen with remoting 2.0
            currentThreadId = Thread.currentThread().getId();
         }
      }
   } 
   
   public String sayHelloTo(String name)
   {
      return "Hi " + name;
   }
}
