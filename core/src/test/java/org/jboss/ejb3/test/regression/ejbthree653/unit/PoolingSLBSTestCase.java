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
package org.jboss.ejb3.test.regression.ejbthree653.unit;

import java.util.Date;

import junit.framework.Test;

import org.jboss.ejb3.test.regression.ejbthree653.MyStateful21;
import org.jboss.ejb3.test.regression.ejbthree653.MyStateful21Home;
import org.jboss.test.JBossTestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PoolingSLBSTestCase extends JBossTestCase
{

   public PoolingSLBSTestCase(String name)
   {
      super(name);
   }

   public void test1() throws Exception
   {
      MyStateful21Home home = (MyStateful21Home) getInitialContext().lookup("MyStateful21Bean/home");
      MyStateful21 session = home.create();
      for(int i = 0; i < 1000; i++)
      {
         String me = new Date().toString();
         String expected = "Hi " + me;
         String actual = session.sayHelloTo(me);
         assertEquals(expected, actual);
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(PoolingSLBSTestCase.class, "ejbthree653.jar");
   }
}
