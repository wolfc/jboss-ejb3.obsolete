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
package org.jboss.ejb3.test.arjuna.unit;

import org.jboss.ejb3.test.arjuna.StatefulTx;
import org.jboss.ejb3.test.arjuna.Entity;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class ArjunaTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(ArjunaTestCase.class);

   public ArjunaTestCase(String name)
   {
      super(name);
   }
   
   public void testStatefulTx() throws Exception
   {     
      StatefulTx stateful = (StatefulTx)getInitialContext().lookup("StatefulTx");
      assertNotNull(stateful);
      
      boolean arjunaTransacted = stateful.isArjunaTransactedRequired();
      assertTrue(arjunaTransacted);
      arjunaTransacted = stateful.isArjunaTransactedRequiresNew();
      assertTrue(arjunaTransacted);
      
      Entity entity = new Entity();
      entity.setName("test-entity");
      entity.setId(1234L);
      
      arjunaTransacted = stateful.clear(entity);
      assertTrue(arjunaTransacted);
      
      arjunaTransacted = stateful.persist(entity);
      assertTrue(arjunaTransacted);
      
      stateful.clear(entity);
   }
 
   public static Test suite() throws Exception
   {
      return getDeploySetup(ArjunaTestCase.class, "arjuna-test.jar");
   }

}
