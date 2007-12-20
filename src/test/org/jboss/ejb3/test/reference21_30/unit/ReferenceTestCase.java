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
package org.jboss.ejb3.test.reference21_30.unit;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.InitialContext;
import org.jboss.ejb3.test.reference21_30.Session21;
import org.jboss.ejb3.test.reference21_30.Session30;
import org.jboss.ejb3.test.reference21_30.Session30Home;
import org.jboss.ejb3.test.reference21_30.StatefulSession30;
import org.jboss.ejb3.test.reference21_30.StatefulSession30Home;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Test for EJB3.0/EJB2.1 references
 * 
 * @version <tt>$Revision: 61219 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class ReferenceTestCase
    extends JBossTestCase {

   private static final Logger log = Logger
         .getLogger(ReferenceTestCase.class);

   public ReferenceTestCase(String name)
   {
      super(name);
   }

   public void testSession21() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      Session21 session = (Session21)jndiContext.lookup("Session21Remote");
      String access = session.access();
      assertEquals("Session21", access);
      access = session.access30();
      assertEquals("Session30", access);
   }
   
   public void testSession30() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
 
      Session30 session = (Session30) jndiContext.lookup("Session30Remote");
      String access = session.access();
      assertEquals("Session30", access);
      access = session.access21();
      assertEquals("Session21", access);
   } 
   
   public void testSessionHome30() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      Session30 session = (Session30) jndiContext.lookup("Session30Remote");
      assertNotNull(session);
      String access = session.access();
      assertEquals("Session30", access);
      
      Session30Home home = (Session30Home) jndiContext.lookup("Session30/home");
      assertNotNull(home);
      session = (Session30)home.create();
      assertNotNull(session);
      access = session.access();
      assertEquals("Session30", access);
   }

   public void testStatefulRemove() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      StatefulSession30Home home = (StatefulSession30Home) jndiContext.lookup("StatefulSession30/home");
      assertNotNull(home);
      StatefulSession30 session = (StatefulSession30)home.create();
      assertNotNull(session);
      session.setValue("123");
      String value = session.getValue();
      assertEquals("123", value);
      
      EJBObject ejbObject = (EJBObject)session;
      
      Handle handle = session.getHandle();
      assertNotNull(handle);
      
      home.remove(handle);
      
      try {
         session.getValue();
         assertTrue(false);
      } catch (EJBException e)
      {
         assertTrue(e instanceof EJBException);
      }
      
      session = (StatefulSession30)home.create();
      assertNotNull(session);
      session.setValue("123");
      value = session.getValue();
      assertEquals("123", value);
      
      session.remove();
      
      try {
         session.getValue();
         assertTrue(false);
      } catch (EJBException e)
      {
         assertTrue(e instanceof EJBException);
      }
   }
 
   public void testStatefulSessionHome30() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      StatefulSession30 session = (StatefulSession30) jndiContext.lookup("StatefulSession30Remote");
      assertNotNull(session);
      session.setValue("testing");
      String value = session.getValue();
      assertEquals("testing", value);
      
      StatefulSession30Home home = (StatefulSession30Home) jndiContext.lookup("StatefulSession30/home");
      assertNotNull(home);
      session = (StatefulSession30)home.create();
      assertNotNull(session);
      session.setValue("123");
      value = session.getValue();
      assertEquals("123", value);
      
      session = (StatefulSession30)home.create("456");
      assertNotNull(session);
      value = session.getValue();
      assertEquals("456", value);
      
      session = (StatefulSession30)home.create("combined", new Integer("789"));
      assertNotNull(session);
      value = session.getValue();
      assertEquals("combined789", value);
   }
   
   public void testRemoteHomeAnnotation() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      StatefulSession30Home home = (StatefulSession30Home) jndiContext.lookup("HomedStatefulSession30/home");
      assertNotNull(home);
      StatefulSession30 session = (StatefulSession30)home.create();
      assertNotNull(session);
      session.setValue("123");
      String value = session.getValue();
      assertEquals("123", value);
      
      session = (StatefulSession30)home.create("456");
      assertNotNull(session);
      value = session.getValue();
      assertEquals("456", value);
      
      session = (StatefulSession30)home.create("combined", new Integer("789"));
      assertNotNull(session);
      value = session.getValue();
      assertEquals("combined789", value);
   }
   
   public void testLocalHomeAnnotation() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      StatefulSession30 session = (StatefulSession30) jndiContext.lookup("StatefulSession30Remote");
      
      String access = session.accessLocalHome();
      assertEquals("LocalHome", access);
   }
   
   public void testLocalHome() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      StatefulSession30 statefulSession = (StatefulSession30) jndiContext.lookup("StatefulSession30Remote");
      assertNotNull(statefulSession);
      String access = statefulSession.accessLocalStateless();
      assertEquals("Session30", access);
      
      Session30 session = (Session30) jndiContext.lookup("Session30Remote");
      assertNotNull(session);
      access = session.accessLocalStateful();
      assertEquals("default", access);
      
      access = session.accessLocalStateful("testing");
      assertEquals("testing", access);
      
      access = session.accessLocalStateful("testing", new Integer(123));
      assertEquals("testing123", access);
   } 
 
   public void testStatefulState() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
           
      StatefulSession30 session1 = (StatefulSession30) jndiContext.lookup("StatefulSession30Remote");
      assertNotNull(session1);
      session1.setValue("testing");
      assertEquals("testing", session1.getValue());
      
      StatefulSession30 session2 = (StatefulSession30) jndiContext.lookup("StatefulSession30Remote");
      assertNotNull(session2);
      assertEquals("default", session2.getValue());
     
      StatefulSession30Home home = (StatefulSession30Home) jndiContext.lookup("StatefulSession30/home");
      assertNotNull(home);
      StatefulSession30 session3 = (StatefulSession30)home.create();
      assertNotNull(session3);
      session3.setValue("123");
      assertEquals("123", session3.getValue());
      
      StatefulSession30 session4 = (StatefulSession30)home.create();
      assertNotNull(session4);
      assertEquals("default", session4.getValue());
      assertEquals("default", session4.getValue());
      
      StatefulSession30 session5 = (StatefulSession30)home.create("init");
      assertNotNull(session5);
      assertEquals("init", session5.getValue());
      
      StatefulSession30 session6 = (StatefulSession30)home.create("init", new Integer(123));
      assertNotNull(session6);
      assertEquals("init123", session6.getValue());
      
      StatefulSession30 session7 = (StatefulSession30)home.create("secondinit");
      assertNotNull(session7);
      assertEquals("secondinit", session7.getValue());
      
      StatefulSession30 session8 = (StatefulSession30) jndiContext.lookup("StatefulSession30Remote");
      assertNotNull(session8);
      assertEquals("default", session8.getValue());
      
      assertEquals("testing", session1.getValue());
      assertEquals("default", session2.getValue());
      assertEquals("123", session3.getValue());
      assertEquals("default", session4.getValue());
      assertEquals("init", session5.getValue());
      assertEquals("init123", session6.getValue());
      assertEquals("secondinit", session7.getValue());
   }
   
   public void testStateful21Interfaces() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      StatefulSession30Home home = (StatefulSession30Home) jndiContext.lookup("StatefulSession30/home");
      assertNotNull(home);
      
      EJBMetaData metadata = home.getEJBMetaData();
      assertNotNull(metadata);
      assertEquals("org.jboss.ejb3.test.reference21_30.StatefulSession30",metadata.getRemoteInterfaceClass().getName());
      
      HomeHandle homeHandle = home.getHomeHandle();
      assertNotNull(homeHandle);
      
      EJBHome ejbHome = homeHandle.getEJBHome();
      assertNotNull(ejbHome);
      metadata = ejbHome.getEJBMetaData();
      assertNotNull(metadata);
      assertEquals("org.jboss.ejb3.test.reference21_30.StatefulSession30",metadata.getRemoteInterfaceClass().getName());
      
      StatefulSession30 session = (StatefulSession30)home.create();
      assertNotNull(session);
      ejbHome = session.getEJBHome();
      assertNotNull(ejbHome);
      Object primaryKey = session.getPrimaryKey();
      assertNotNull(primaryKey);
      
      Handle handle = session.getHandle();
      assertNotNull(handle);
      
      EJBObject ejbObject = handle.getEJBObject();
      assertNotNull(ejbObject);
      
      ejbHome = ejbObject.getEJBHome();
      assertNotNull(ejbHome);
    
      Handle handle1 = ejbObject.getHandle();
      assertNotNull(handle1);
      
      StatefulSession30 session1 = (StatefulSession30)home.create();
      assertFalse(session.isIdentical(session1));
      assertTrue(session.isIdentical(session));
   }
 
   public void testStateless21Interfaces() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      Session30Home home = (Session30Home) jndiContext.lookup("Session30/home");
      assertNotNull(home);
      
      EJBMetaData metadata = home.getEJBMetaData();
      assertNotNull(metadata);
      assertEquals("org.jboss.ejb3.test.reference21_30.Session30",metadata.getRemoteInterfaceClass().getName());
      
      HomeHandle homeHandle = home.getHomeHandle();
      assertNotNull(homeHandle);
      
      EJBHome ejbHome = homeHandle.getEJBHome();
      assertNotNull(ejbHome);
      metadata = ejbHome.getEJBMetaData();
      assertNotNull(metadata);
      assertEquals("org.jboss.ejb3.test.reference21_30.Session30",metadata.getRemoteInterfaceClass().getName());
      
      Session30 session = (Session30)home.create();
      assertNotNull(session);
      ejbHome = session.getEJBHome();
      assertNotNull(ejbHome);
      Object primaryKey = session.getPrimaryKey();
      assertNull(primaryKey);
      
      Handle handle = session.getHandle();
      assertNotNull(handle);
      
      EJBObject ejbObject = handle.getEJBObject();
      assertNotNull(ejbObject);
      
      ejbHome = ejbObject.getEJBHome();
      assertNotNull(ejbHome);
    
      Handle handle1 = ejbObject.getHandle();
      assertNotNull(handle1);
      
      Session30 session1 = (Session30)home.create();
      assertFalse(session.isIdentical(session1));
   }

   protected void setUp() throws Exception
   {
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ReferenceTestCase.class, "reference.jar");
   }
}
