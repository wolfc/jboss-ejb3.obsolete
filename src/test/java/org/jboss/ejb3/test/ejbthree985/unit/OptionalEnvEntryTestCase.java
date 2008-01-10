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
package org.jboss.ejb3.test.ejbthree985.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree985.OptionalEnvEntry;
import org.jboss.test.JBossTestCase;

/**
 * Test to see if optional env-entry-value works (16.4.1.3).
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class OptionalEnvEntryTestCase extends JBossTestCase
{

   public OptionalEnvEntryTestCase(String name)
   {
      super(name);
   }

   private OptionalEnvEntry lookupBean() throws Exception
   {
      return (OptionalEnvEntry) getInitialContext().lookup("OptionalEnvEntryBean/remote");
   }
   
   public void test1() throws Exception
   {
      OptionalEnvEntry bean = lookupBean();
      Double actual = bean.getEntry();
      // 1.1 is defined in OptionalEnvEntryBean
      assertEquals(new Double(1.1), actual);
   }
   
   public void testLookup() throws Exception
   {
      OptionalEnvEntry bean = lookupBean();
      bean.checkLookup();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(OptionalEnvEntryTestCase.class, "ejbthree985.jar");
   }
}
