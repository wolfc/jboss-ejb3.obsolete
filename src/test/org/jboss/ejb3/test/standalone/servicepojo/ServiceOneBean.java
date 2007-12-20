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
package org.jboss.ejb3.test.standalone.servicepojo;

import org.jboss.ejb3.annotation.Service;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 67628 $
 */
@Service
public class ServiceOneBean implements ServiceOneLocal, ServiceOneManagement
{
   int state = 0;
   int attribute;

   public void setAttribute(int attribute)
   {
      this.attribute = attribute;
   }

   public int getAttribute()
   {
      return this.attribute;
   }

   public int getState()
   {
      return state;
   }
   
   public String sayHello(String name)
   {
      return "Hello " + name;
   }

   // Lifecycle methods
   public void create() throws Exception
   {
      System.out.println("ServiceOne - Creating");
      state++;
   }

   public void start() throws Exception
   {
      System.out.println("ServiceOne - Starting");
      state++;
   }

   public void stop()
   {
      System.out.println("ServiceOne - Stopping");
      state--;
   }

   public void destroy()
   {
      System.out.println("ServiceOne - Destroying");
      state--;
   }
}
