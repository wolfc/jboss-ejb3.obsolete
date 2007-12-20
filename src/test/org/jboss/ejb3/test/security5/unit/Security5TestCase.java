/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.security5.unit;

import javax.ejb.EJBAccessException;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.test.security5.SimpleSessionInterface;
import org.jboss.security.auth.callback.AppCallbackHandler;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

//$Id$

/**
 *  Test case for JBoss Security 
 *  
 *  No JBoss Specific annotations are used.
 *  Customization to be done via jboss.xml
 *  
 *  @author Anil.Saldhana@redhat.com
 *  @since  Aug 16, 2007 
 *  @version $Revision$
 */
public class Security5TestCase extends JBossTestCase
{ 
   private InitialContext context = null; 
   private SecurityClient client = null; 
   
   @Override
   protected void setUp() throws Exception
   { 
      super.setUp();
      client = SecurityClientFactory.getSecurityClient();
      context = new InitialContext();
   }

   public Security5TestCase(String name)
   {
      super(name); 
   }
   
   public void testStateless() throws Exception
   { 
      String jndiName = "SimpleStatelessBean/remote"; 
      checkSessionBean((SimpleSessionInterface)context.lookup(jndiName)); 
   }
   
   public void testStateful() throws Exception
   {
      String jndiName = "SimpleStatefulBean/remote"; 
      checkSessionBean((SimpleSessionInterface)context.lookup(jndiName)); 
   }
   
   private void checkSessionBean(SimpleSessionInterface ssi) throws Exception
   {
      client.logout();
      AppCallbackHandler acbh = new AppCallbackHandler("scott","echoman".toCharArray());
      client.setJAAS("simple", acbh);
      client.login();
      assertEquals("echo==hi", "hi", ssi.echo("hi"));
      assertEquals("CallerPrincipal==scott", "scott", ssi.echoCallerPrincipal().getName());
      assertEquals("CallerRole==Echo", true, ssi.isCallerInRole("Echo"));
      client.logout();
      try
      {
         ssi.echo("hi again");
         fail("Should not have reached here");
      }
      catch(Exception e)
      {
         if(e instanceof EJBAccessException == false)
            fail("Wrong exception:"+e.getLocalizedMessage());
      }
   } 
   
   public static Test suite() throws Exception
   {
      try 
      {
         return getDeploySetup(Security5TestCase.class, "security5.jar");
      }
      catch (Exception e)
      { 
         throw e;
      }
   }
}
