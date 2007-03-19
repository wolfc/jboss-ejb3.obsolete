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
package org.jboss.tutorial.packaging.client;

import javax.naming.InitialContext;
import org.jboss.tutorial.packaging.bean.Session2;
import org.jboss.tutorial.packaging.bean.Session1;
import org.jboss.tutorial.packaging.bean.Entity1;
import org.jboss.tutorial.packaging.bean.Entity2;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class Client
{

   public static void main(String[] args) throws Exception
   {
      Session1 session1 = (Session1) getInitialContext().lookup("tutorial/Session1Bean/remote");
      Session2 session2 = (Session2) getInitialContext().lookup("tutorial/Session2Bean/remote");

      int oneF = session1.create1FromFactory();
      int oneM = session1.create1FromManager();
      int twoF = session1.create2FromFactory();
      int twoM = session1.create2FromManager();

      Entity1 one = session2.find1FromFactory(oneF);
      System.out.println("factory: " + one.getString());

      one = session2.find1FromManager(oneM);
      System.out.println("manager: " + one.getString());


      Entity2 two = session2.find2FromFactory(twoF);
      System.out.println("factory: " + two.getString());

      two = session2.find2FromManager(twoM);
      System.out.println("manager: " + two.getString());
   }

   public static InitialContext getInitialContext()  throws Exception
   {
      return new InitialContext();
   }



}
