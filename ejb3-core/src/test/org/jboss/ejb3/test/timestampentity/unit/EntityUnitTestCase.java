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
package org.jboss.ejb3.test.timestampentity.unit;

import java.util.Date;

import org.jboss.ejb3.test.timestampentity.*;

import org.jboss.test.JBossTestCase;
import junit.framework.Test;

public class EntityUnitTestCase
extends JBossTestCase
{
   public EntityUnitTestCase(String name)
   {

      super(name);

   }

   public void testOneToMany() throws Exception
   {
      /* TestManager test = (TestManager) getInitialContext().lookup(TestManagerConstants.JNDI_BINDING);
      test.createTestInteger("keyField1", 1, "keyField3", "field1", "field2", new Date());
      test.createTestTimestamp("keyField1", 1, "keyField3", "field1", "field2", new Date());
      
      test.findTestInteger("keyField1", 1, "keyField3");
      test.findTestTimestamp("keyField1", 1, "keyField3");*/
	   
	   TestManager testManager = (TestManager) getInitialContext().lookup(TestManagerConstants.JNDI_BINDING);
/*	   try {
	      testManager.createTestInteger("keyField1", 1, "keyField3", "field1", "field2", new Date(System.currentTimeMillis()));
	      testManager.updateTestInteger("keyField1", 1, "keyField3", "field-new", "field-new", new Date(System.currentTimeMillis()));
	    }
	    catch (Exception e) {
	      System.out.println("Exception caught during test #1.  " + e);
	      e.printStackTrace();
	    }*/
	    System.out.println("Exectuing test #2");
	    try {
	      testManager.createTestTimestamp("keyField1", 1, "keyField3", "field1", "field2", new Date(System.currentTimeMillis()));
	      testManager.updateTestTimestamp("keyField1", 1, "keyField3", "field1-new", "field2-new", new Date(System.currentTimeMillis()));
	    }
	    catch (Exception e) {
	      System.out.println("Exception caught during test #2.  " + e);
	      e.printStackTrace();
	    }

   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityUnitTestCase.class, "timestampentity-test.jar");
   }

}
