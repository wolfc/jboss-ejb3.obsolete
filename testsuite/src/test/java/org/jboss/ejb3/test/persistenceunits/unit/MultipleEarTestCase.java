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

import java.io.IOException;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.client.spi.IncompleteDeployments;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.ejb3.test.persistenceunits.Entity1;
import org.jboss.ejb3.test.persistenceunits.Entity2;
import org.jboss.ejb3.test.persistenceunits.EntityTest;
import org.jboss.injection.PersistenceUnitHandler;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class MultipleEarTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(MultipleEarTestCase.class);

   public MultipleEarTestCase(String name)
   {
      super(name);
   }

   public void testGoodEar() throws Exception
   {
      EntityTest test = (EntityTest) getInitialContext().lookup("persistenceunitscope-test1/EntityTestBean/remote");
      
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
   
   public void testBadEar() throws Exception
   {
      try
      {
         EntityTest test = (EntityTest) getInitialContext().lookup("persistenceunitscope-test2/EntityTestBean/remote");
         fail("Should not have deployed - got PU from persistenceunitscope-test1");
      } catch (javax.naming.NameNotFoundException e)
      {
      }
   }

   public void testServerFound() throws Exception
   {
      try
      {
         serverFound();
      }
      catch(DeploymentException e)
      {
         IncompleteDeploymentException cause = (IncompleteDeploymentException) e.getCause();
         IncompleteDeployments incomplete = cause.getIncompleteDeployments();
         Map<String, Throwable> deploymentsInError = incomplete.getDeploymentsInError();
         assertEquals("only persistenceunitscope-test2.ear should have failed", 1, deploymentsInError.size());
         Map.Entry<String, Throwable> entry = deploymentsInError.entrySet().iterator().next();
         assertTrue(entry.getKey().endsWith("persistenceunitscope-test2.ear"));

         // Check that it's Entity1 PU that cannot be resolved 
         String message = entry.getValue().getMessage();
         assertTrue(message.contains(PersistenceUnitHandler.ERROR_MESSAGE_FAILED_TO_RESOVLE_PU));
         assertTrue(message.contains("Entity1"));
      }
   }

   public static Test suite() throws Exception
   {
      Test test = getDeploySetup(MultipleEarTestCase.class, "persistenceunitscope-test1.ear, persistenceunitscope-test2.ear");
      TestSetup setup = new TestSetup(test) {
         private ObjectName on = new ObjectName("jboss.pojo:name='PersistenceUnitDependencyResolver'");
         
         private boolean previousSetting = false;
         
         private Object getAttribute(ObjectName on, String name) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException, Exception
         {
            return getServer().getAttribute(on, name);
         }
         
         // TODO: should come from JBossTestServices
         private MBeanServerConnection getServer() throws NamingException
         {
            String adaptorName = System.getProperty("jbosstest.server.name", "jmx/invoker/RMIAdaptor");
            return (MBeanServerConnection) new InitialContext().lookup(adaptorName);
         }
         
         private void setAttribute(ObjectName on, String name, Object value) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException, Exception
         {
            getServer().setAttribute(on, new Attribute(name, value));
         }
         
         @Override
         protected void setUp() throws Exception
         {
            previousSetting = (Boolean) getAttribute(on, "SpecCompliant");
            setAttribute(on, "SpecCompliant", true);
         }
         
         @Override
         protected void tearDown() throws Exception
         {
            setAttribute(on, "SpecCompliant", previousSetting);
         }
      };
      return setup;
   }

}
