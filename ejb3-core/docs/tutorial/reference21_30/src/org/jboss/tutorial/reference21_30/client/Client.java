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
package org.jboss.tutorial.reference21_30.client;

import org.jboss.tutorial.reference21_30.bean.Stateless2;
import org.jboss.tutorial.reference21_30.bean.Stateless2Home;
import org.jboss.tutorial.reference21_30.bean.Stateless3;


import javax.naming.InitialContext;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      accessReferences();
   }
   
   public static void accessReferences() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      System.out.println("Testing EJB3.0 references to EJB2.x");
      Stateless3 test3 = (Stateless3)jndiContext.lookup("Stateless3");
      test3.testAccess();
      
      System.out.println("Testing EJB2.x references to EJB3.0");
      Stateless2Home home = (Stateless2Home)jndiContext.lookup("Stateless2");
      Stateless2 test2 = home.create();
      test2.testAccess();
      
      System.out.println("Succeeded");
   }
}
