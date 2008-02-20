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

import javax.naming.*;

import org.jboss.ejb3.test.reference21_30.*;
import org.jboss.logging.Logger;

import javax.ejb.EJBException;

import junit.framework.Test;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;

/**
 * Test for EJB3.0/EJB2.1 references
 * 
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class GlobalReferenceTestCase
    extends JBossTestCase {

   private static final Logger log = Logger
         .getLogger(GlobalReferenceTestCase.class);

   public GlobalReferenceTestCase(String name)
   {
      super(name);
   }

   public void testSession21() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
 
      Session21Home home = (Session21Home) jndiContext.lookup("Session21Remote");
      Session21 session = (Session21)home.create();
      String access = session.access();
      assertEquals("Session21", access);
      access = session.globalAccess30();
      assertEquals("Session30", access);
   }
   
   public void testSession30() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
 
      Session30RemoteBusiness session = (Session30RemoteBusiness) jndiContext.lookup("GlobalSession30Remote");
      String access = session.access();
      assertEquals("Session30", access);
      access = session.globalAccess21();
      assertEquals("Session21", access);
   }

   protected void setUp() throws Exception
   {
   }

   public static Test suite() throws Exception
   {
   //   return new TestSuite(BankDeploymentDescriptorTestCase.class);
      return getDeploySetup(GlobalReferenceTestCase.class, "globalReference-ejb3.jar,globalReference.jar");
   }

}
