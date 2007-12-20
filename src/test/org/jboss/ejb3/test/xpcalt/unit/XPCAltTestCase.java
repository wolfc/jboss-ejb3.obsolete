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
package org.jboss.ejb3.test.xpcalt.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.xpcalt.Inspector;
import org.jboss.ejb3.test.xpcalt.Keeper;
import org.jboss.ejb3.test.xpcalt.Master;
import org.jboss.ejb3.test.xpcalt.Thingy;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Test the an alternative to extended persistence context.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 64905 $
 */
public class XPCAltTestCase extends JBossTestCase
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(XPCAltTestCase.class);

   public XPCAltTestCase(String name)
   {
      super(name);
   }
   
   public void test1() throws Exception
   {
      Master master = (Master) getInitialContext().lookup("MasterBean/remote");
      
      master.doSomething();
      
      master.remove();
   }
   
   public void test2() throws Exception
   {
      Master master = (Master) getInitialContext().lookup("MasterBean/remote");
      Inspector inspector = (Inspector) getInitialContext().lookup("InspectorBean/remote");
      
      long id = master.createThingy(2);
      
      Thingy thingy = inspector.find(Thingy.class, id);
      assertNull("thingy should not have been committed", thingy);
      
      master.save();
      
      thingy = inspector.find(Thingy.class, id);
      assertNotNull("thingy should have been committed", thingy);
      assertEquals((Long) 2l, thingy.getId());
      
      master.remove();
   }
   
   public void test3() throws Exception
   {
      Master master = (Master) getInitialContext().lookup("MasterBean/remote");
      Inspector inspector = (Inspector) getInitialContext().lookup("InspectorBean/remote");
      
      long id = master.createThingy(3);
      
      Thingy thingy = inspector.find(Thingy.class, id);
      assertNull("thingy should not have been committed", thingy);
      
      // make sure everything is passivated
      sleep(10000);
      
      master.checkThingy(3);
      
      master.save();
      
      thingy = inspector.find(Thingy.class, id);
      assertNotNull("thingy should have been committed", thingy);
      assertEquals((Long) 3l, thingy.getId());
      
      master.remove();
   }
   
   public void testKeeper() throws Exception
   {
      Keeper keeper = (Keeper) getInitialContext().lookup("KeeperBean/remote");
      Inspector inspector = (Inspector) getInitialContext().lookup("InspectorBean/remote");
      
      long id = keeper.createThingy(4);
      
      Thingy thingy = inspector.find(Thingy.class, id);
      assertNull("thingy should not have been committed", thingy);
            
      Thread.sleep(10000);
      
      keeper.checkThingy(4);
      
      keeper.updateKeep("Showdown");
      
      keeper.save();
      
      thingy = inspector.find(Thingy.class, id);
      assertEquals("Showdown", thingy.getText());
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(XPCAltTestCase.class, "xpcalt.jar");
   }
}