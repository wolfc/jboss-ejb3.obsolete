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
package org.jboss.ejb3.test.dd.override.unit;

import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Test for EJB3 deployment of EJB2.0 Bank EJBs
 * 
 * @version <tt>$Revision: 61136 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class OverrideTestCase
    extends JBossTestCase {

   private static final Logger log = Logger
         .getLogger(OverrideTestCase.class);

   public OverrideTestCase(String name)
   {
      super(name);
   }
/*   
   public void testTxOverride() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      Foo foo = (Foo) jndiContext.lookup("Foo");
      assertNotNull(foo);
      
      SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      assertFalse(foo.transactionExisted());
      
      try {
         foo.bar();
         assertTrue(false);
      } catch (Exception e){
         assertTrue(e instanceof SecurityException);
      }
      
      SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
      SecurityAssociation.setCredential("password".toCharArray());
      
      foo.bar();
      assertFalse(foo.transactionExisted());
   }
 
   public void testRemoteOverride() throws Exception
   {
      try {
      InitialContext jndiContext = new InitialContext();
      
      LocalFoo foo = (LocalFoo) jndiContext.lookup("LocalFoo");
      assertNotNull(foo);
      
      foo.bar();
      } catch (Exception e){
         e.printStackTrace();
         throw e;
      }
   } */
   
   public void testNaming()
   {
      lookup("");
   }
   
   private void lookup(String name)
   {
      log.info("lookup " + name);
      try {
         InitialContext jndiContext = new InitialContext();
         NamingEnumeration names = jndiContext.list(name);
         if (names != null){
            while (names.hasMore()){
               log.info("  " + names.next());
            }
         }
      } catch (Exception e){
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(OverrideTestCase.class, "dd-override1B.jar");
   }

}
