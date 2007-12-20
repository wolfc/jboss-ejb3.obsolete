/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.clusteredsession.unit;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ejb3.test.common.unit.DBSetup;

/**
 * Tests for extended persistence under clustering with a scoped classloader.
 *
 * @author Brian Stansberry
 * @version $Id: ExtendedPersistenceUnitTestCase.java 60065 2007-01-27 23:05:44Z bstansberry@jboss.com $
 */

public class ScopedExtendedPersistenceUnitTestCase extends ExtendedPersistenceUnitTestCase
{
   public ScopedExtendedPersistenceUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      Test t1 = getDeploySetup(ScopedExtendedPersistenceUnitTestCase.class,
                               "clusteredsession-xpc-scoped.jar");

      suite.addTest(t1);

      // Create an initializer for the test suite
      DBSetup wrapper = new DBSetup(suite);
      return wrapper;     
   }

}
