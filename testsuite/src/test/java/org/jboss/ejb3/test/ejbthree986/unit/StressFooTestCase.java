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
package org.jboss.ejb3.test.ejbthree986.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree986.Foo;
import org.jboss.test.JBossTestCase;

/**
 * Stress test a stateless session bean.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:yvan.borgne@lapeyre.fr">Yvan Borgne</a>
 * @version $Revision$
 */
public class StressFooTestCase extends JBossTestCase
{

   public StressFooTestCase(String name)
   {
      super(name);
   }

   private Foo lookupBean() throws Exception
   {
      return (Foo) getInitialContext().lookup("FooBean/remote");
   }
   
   public void test1() throws Exception
   {
      Foo bean = lookupBean();
      for(int i = 0; i < 10000; i++)
      {
         if((i % 1000) == 0)
            System.err.println("I'm alive @" + i);
         bean.bar();
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(StressFooTestCase.class, "ejbthree986.jar");
   }
}
