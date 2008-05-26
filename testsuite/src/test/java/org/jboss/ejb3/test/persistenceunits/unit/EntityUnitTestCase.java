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
package org.jboss.ejb3.test.persistenceunits.unit;

import java.util.Set;
import org.jboss.ejb3.test.persistenceunits.Entity1;
import org.jboss.ejb3.test.persistenceunits.Entity2;
import org.jboss.ejb3.test.persistenceunits.EntityTest;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class EntityUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(EntityUnitTestCase.class);

   public EntityUnitTestCase(String name)
   {
      super(name);
   }

   public void testPersistenceUnits() throws Exception
   {
      EntityTest test = (EntityTest) getInitialContext().lookup("EntityTestBean/remote");
      
      Entity1 entity1 = new Entity1();
      entity1.setData("ONE");
      Long id1 = test.persistEntity1(entity1);
      
      Entity2 entity2 = new Entity2();
      entity2.setData("TWO");
      Long id2 = test.persistEntity2(entity2);
      
      entity1 = test.loadEntity1(id1);
      assertEquals("ONE", entity1.getData());
      
      entity2 = test.loadEntity2(id2);
      assertEquals("TWO", entity2.getData());
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(EntityUnitTestCase.class, "persistenceunits-test.jar");
   }

}
