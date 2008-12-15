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
package org.jboss.ejb3.test.defaultremotebindings.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.defaultremotebindings.StatelessRemote;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class DefaultRemoteBindingsTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(DefaultRemoteBindingsTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public DefaultRemoteBindingsTestCase(String name)
   {
      super(name);
   }
 
   public void testDefault() throws Exception
   {
      try
      {
         StatelessRemote slsb = (StatelessRemote) getInitialContext().lookup("StatelessBean/remote");
         fail("Default binding not used");
      }
      catch (javax.naming.NamingException e)
      {
         
      }
      
      StatelessRemote slsb = (StatelessRemote) getInitialContext().lookup("DefaultedStateless");
      assertNotNull(slsb);
      
      slsb.test();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(DefaultRemoteBindingsTestCase.class, "defaultremotebindings-test.jar");
   }
}