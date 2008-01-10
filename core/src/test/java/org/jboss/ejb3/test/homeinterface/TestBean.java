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
package org.jboss.ejb3.test.homeinterface;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision: 60233 $</tt>
 */
@Stateless
@Remote(Test.class)
public class TestBean implements Test
{
   private static final Logger log = Logger.getLogger(TestBean.class);
   
   public void testDefaultStatelessLocal() throws Exception
   {
	   InitialContext jndiContext = new InitialContext();
	   LocalHome home = (LocalHome)jndiContext.lookup("DefaultStatelessBean/localHome");
	   LocalInterface local = home.create();
	   local.testLocal();
	   
	   LocalBusinessInterface localBusiness = (LocalBusinessInterface)jndiContext.lookup("DefaultStatelessBean/local");
	   localBusiness.testLocal();
   }
   
   public void testExplicitStatelessLocal() throws Exception
   {
	   InitialContext jndiContext = new InitialContext();
	   LocalHome home = (LocalHome)jndiContext.lookup("ExplicitStatelessLocalHome");
	   LocalBusinessInterface local = home.create();
	   local.testLocal();
	   
	   local = (LocalBusinessInterface)jndiContext.lookup("ExplicitStatelessLocal");
	   local.testLocal();
   }
   
   public void testDescriptorStatelessLocal() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      LocalHome home = (LocalHome)jndiContext.lookup("DescriptorStatelessLocalHome");
      LocalInterface local = home.create();
      local.testLocal();
      
      LocalBusinessInterface localBusiness = (LocalBusinessInterface)jndiContext.lookup("DescriptorStatelessBean/local");
      localBusiness.testLocal();
   }
   
   public void testDefaultStatefulLocal() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      LocalHome home = (LocalHome)jndiContext.lookup("DefaultStatefulBean/localHome");
      LocalBusinessInterface local = home.create();
      local.testLocal();
      
      local = (LocalBusinessInterface)jndiContext.lookup("DefaultStatefulBean/local");
      local.testLocal();
   }
   
   public void testExplicitStatefulLocal() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      LocalHome home = (LocalHome)jndiContext.lookup("ExplicitStatefulLocalHome");
      LocalBusinessInterface local = home.create();
      local.testLocal();
      
      local = (LocalBusinessInterface)jndiContext.lookup("ExplicitStatefulLocal");
      local.testLocal();
   }
   
   public void testDescriptorStatefulLocal() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      LocalHome home = (LocalHome)jndiContext.lookup("DescriptorStatefulLocalHome");
      LocalBusinessInterface local = home.create();
      local.testLocal();
      
      local = (LocalBusinessInterface)jndiContext.lookup("DescriptorStatefulBean/local");
      local.testLocal();
   }
   
   public void testDuplicateStatelessLocal() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      LocalHome home = (LocalHome)jndiContext.lookup("DuplicateStatelessLocal");
      LocalBusinessInterface local = home.create();
      local.testLocal();
      
      local = (LocalBusinessInterface)jndiContext.lookup("DuplicateStatelessLocal");
      local.testLocal();
   }
   
   public void testDuplicateStatefulLocal() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      LocalHome home = (LocalHome)jndiContext.lookup("DuplicateStatefulLocal");
      LocalBusinessInterface local = home.create();
      local.testLocal();
      
      local = (LocalBusinessInterface)jndiContext.lookup("DuplicateStatefulLocal");
      local.testLocal();
   }

}
