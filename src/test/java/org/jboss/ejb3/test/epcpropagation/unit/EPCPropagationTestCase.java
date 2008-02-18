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
package org.jboss.ejb3.test.epcpropagation.unit;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.test.epcpropagation.StatefulRemote;
import org.jboss.ejb3.test.epcpropagation.StatelessRemote;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class EPCPropagationTestCase extends JBossTestCase
{

   public EPCPropagationTestCase(String name)
   {
      super(name);
   }
   
   public void testBMTPropagation() throws Exception
   {
      StatelessRemote stateless = (StatelessRemote) new InitialContext().lookup("StatelessBean/remote");
      stateless.createEntity(1, "EntityName");
      
      StatefulRemote stateful = (StatefulRemote) new InitialContext().lookup("StatefulBean/remote");
      boolean equal = stateful.execute(1, "EntityName");
      
      assertTrue("Name changes should propagate", equal);
   }
   
   public void testBMTEPCPropagation() throws Exception
   {
      StatelessRemote stateless = (StatelessRemote) new InitialContext().lookup("StatelessBean/remote");
      stateless.createEntity(2, "EntityName");
      
      StatefulRemote stateful = (StatefulRemote) new InitialContext().lookup("EPCStatefulBean/remote");
      boolean equal = stateful.execute(2, "EntityName");
      
      assertTrue("Name changes should propagate", equal);
   }
   
   public void testCMTPropagation() throws Exception
   {
      StatelessRemote stateless = (StatelessRemote) new InitialContext().lookup("StatelessBean/remote");
      stateless.createEntity(3, "EntityName");
      
      StatefulRemote stateful = (StatefulRemote) new InitialContext().lookup("CMTStatefulBean/remote");
      boolean equal = stateful.execute(3, "EntityName");
      
      assertTrue("Name changes should propagate", equal);
   }
   
   public void testCMTEPCPropagation() throws Exception
   {
      StatelessRemote stateless = (StatelessRemote) new InitialContext().lookup("StatelessBean/remote");
      stateless.createEntity(4, "EntityName");
      
      StatefulRemote stateful = (StatefulRemote) new InitialContext().lookup("CMTEPCStatefulBean/remote");
      boolean equal = stateful.execute(4, "EntityName");
      
      assertTrue("Name changes should propagate", equal);
   }
   
   public void testNoTxPropagation() throws Exception
   {
      StatelessRemote stateless = (StatelessRemote) new InitialContext().lookup("StatelessBean/remote");
      stateless.createEntity(5, "EntityName");
      
      StatefulRemote stateful = (StatefulRemote) new InitialContext().lookup("NoTxStatefulBean/remote");
      boolean equal = stateful.execute(5, "EntityName");
      
      assertFalse("Name changes should not propagate", equal);
   }
   
   public void testNoTxEPCPropagation() throws Exception
   {
      StatelessRemote stateless = (StatelessRemote) new InitialContext().lookup("StatelessBean/remote");
      stateless.createEntity(6, "EntityName");
      
      StatefulRemote stateful = (StatefulRemote) new InitialContext().lookup("NoTxEPCStatefulBean/remote");
      boolean equal = stateful.execute(6, "EntityName");
      
      assertTrue("Name changes should propagate", equal);
   }
   
   public void testIntermediateEPCPropagation() throws Exception
   {
      StatelessRemote stateless = (StatelessRemote) new InitialContext().lookup("StatelessBean/remote");
      stateless.createEntity(7, "EntityName");
      
      StatefulRemote stateful = (StatefulRemote) new InitialContext().lookup("InitEPCStatefulBean/remote");
      boolean equal = stateful.execute(7, "EntityName");
      
      assertTrue("Name changes should propagate", equal);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(EPCPropagationTestCase.class, "epcpropagation-test.jar");
   }

}
