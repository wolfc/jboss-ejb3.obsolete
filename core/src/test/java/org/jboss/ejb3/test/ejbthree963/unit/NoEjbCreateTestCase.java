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
package org.jboss.ejb3.test.ejbthree963.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree963.MyStateful;
import org.jboss.ejb3.test.ejbthree963.MyStatefulHome;
import org.jboss.test.JBossTestCase;


/**
 * Test if an EJB 2.1 bean without ejbCreate is properly deployed.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class NoEjbCreateTestCase extends JBossTestCase
{

   public NoEjbCreateTestCase(String name)
   {
      super(name);
   }

   public void testCreateNoArgs() throws Exception
   {
      MyStatefulHome home = (MyStatefulHome) getInitialContext().lookup(MyStatefulHome.JNDI_NAME);
      MyStateful bean = home.create();
      bean.setName("testCreateNoArgs");
      String expected = "Hi testCreateNoArgs";
      String actual = bean.sayHi();
      assertEquals(expected, actual);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(NoEjbCreateTestCase.class, "ejbthree963.jar");
   }

}
