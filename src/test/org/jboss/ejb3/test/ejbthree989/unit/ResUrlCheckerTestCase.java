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
package org.jboss.ejb3.test.ejbthree989.unit;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree989.ResUrlChecker;
import org.jboss.test.JBossTestCase;

/**
 * Test to see if resources of type URL work.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 63412 $
 */
public class ResUrlCheckerTestCase extends JBossTestCase
{
   public ResUrlCheckerTestCase(String name)
   {
      super(name);
   }

   private ResUrlChecker lookupBean() throws Exception
   {
      return (ResUrlChecker) getInitialContext().lookup("ResUrlCheckerBean/remote");
   }
   
   public void test1() throws Exception
   {
      ResUrlChecker bean = lookupBean();
      // defined in ResUrlCheckerBean
      URL expected = new URL("http://localhost");
      URL actual = bean.getURL1();
      assertEquals(expected, actual);
   }
   
   public void test2() throws Exception
   {
      ResUrlChecker bean = lookupBean();
      // defined in jboss.xml
      URL expected = new URL("http://localhost/url2");
      URL actual = bean.getURL2();
      assertEquals(expected, actual);
   }
   
   public void test3() throws Exception
   {
      ResUrlChecker bean = lookupBean();
      // defined in ResUrlCheckerBean
      URL expected = new URL("http://localhost/url3");
      URL actual = bean.getURL3();
      assertEquals(expected, actual);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(ResUrlCheckerTestCase.class, "ejbthree989.jar");
   }
}
