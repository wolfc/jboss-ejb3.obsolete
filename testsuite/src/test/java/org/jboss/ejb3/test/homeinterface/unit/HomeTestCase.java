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
package org.jboss.ejb3.test.homeinterface.unit;

import javax.naming.InitialContext;

import org.jboss.ejb3.test.homeinterface.Home;
import org.jboss.ejb3.test.homeinterface.RemoteBusinessInterface;
import org.jboss.ejb3.test.homeinterface.RemoteInterface;
import org.jboss.ejb3.test.homeinterface.Test;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class HomeTestCase
    extends JBossTestCase {

   private static final Logger log = Logger
         .getLogger(HomeTestCase.class);

   public HomeTestCase(String name)
   {
      super(name);
   }

   public void testDefaultStateless() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("DefaultStatelessBean/home");
      RemoteInterface remote = home.create();
      remote.test();
      
      RemoteBusinessInterface remoteBusiness = (RemoteBusinessInterface)jndiContext.lookup("DefaultStatelessBean/remote");
      remoteBusiness.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testDefaultStatelessLocal();
   }
   
   public void testExplicitStateless() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("ExplicitStatelessHome");
      RemoteBusinessInterface remote = home.create();
      remote.test();
      
      remote = (RemoteBusinessInterface)jndiContext.lookup("ExplicitStatelessRemote");
      remote.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testExplicitStatelessLocal();
   }
   
   public void testDescriptorStateless() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("DescriptorStatelessHome");
      RemoteBusinessInterface remote = home.create();
      remote.test();
      
      remote = (RemoteBusinessInterface)jndiContext.lookup("DescriptorStatelessBean/remote");
      remote.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testDescriptorStatelessLocal();
   }
   
   public void testDefaultStateful() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("DefaultStatefulBean/home");
      RemoteBusinessInterface remote = home.create();
      remote.test();
      
      remote = (RemoteBusinessInterface)jndiContext.lookup("DefaultStatefulBean/remote");
      remote.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testDefaultStatefulLocal();
   }
   
   public void testExplicitStateful() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("ExplicitStatefulHome");
      RemoteBusinessInterface remote = home.create();
      remote.test();
      
      remote = (RemoteBusinessInterface)jndiContext.lookup("ExplicitStatefulRemote");
      remote.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testExplicitStatefulLocal();
   }
   
   public void testDescriptorStateful() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("DescriptorStatefulHome");
      RemoteBusinessInterface remote = home.create();
      remote.test();
      
      remote = (RemoteBusinessInterface)jndiContext.lookup("DescriptorStatefulBean/remote");
      remote.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testDescriptorStatefulLocal();
   }
   
   public void testDuplicateStateful() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("DuplicateStatefulHome");
      RemoteInterface remote = home.create();
      remote.test();
      
      RemoteBusinessInterface remoteBusiness = (RemoteBusinessInterface) jndiContext.lookup("DuplicateStateful");
      remoteBusiness.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testDuplicateStatefulLocal();
   }
   
   public void testDuplicateStateless() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Home home = (Home)jndiContext.lookup("DuplicateStateless");
      RemoteBusinessInterface remote = home.create();
      remote.test();
      
      remote = (RemoteBusinessInterface)jndiContext.lookup("DuplicateStateless");
      remote.test();
      
      Test test = (Test)jndiContext.lookup("TestBean/remote");
      test.testDuplicateStatelessLocal();
   }

   
   public static junit.framework.Test suite() throws Exception
   {
      return getDeploySetup(HomeTestCase.class, "homeinterface-test.jar");
   }
}
