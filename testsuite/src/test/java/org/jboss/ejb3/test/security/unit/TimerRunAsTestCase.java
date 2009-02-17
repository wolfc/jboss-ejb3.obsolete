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
package org.jboss.ejb3.test.security.unit;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import junit.framework.Test;

import org.jboss.ejb3.test.security.TimerTester;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public class TimerRunAsTestCase extends JBossTestCase
{
   private static final Logger log = Logger
   .getLogger(TimerRunAsTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public TimerRunAsTestCase(String name)
   {
      super(name);
   } 
   
   //TODO SecurityAssociation is Deprecated, this should be replaced with equivalent
   // testing for SecurityClient
   public void testSecurityAssociation()
   {
      SecurityAssociation.clear();
      SecurityAssociation.pushSubjectContext(null, new SimplePrincipal("bill"), "password".toCharArray());
      assertEquals("bill", SecurityAssociation.getPrincipal().getName());
      SecurityAssociation.popSubjectContext();
      assertNull(SecurityAssociation.getPrincipal());
   }
   
   public void testNoSecurityAssociationPrincipal() throws Exception
   {  
      SecurityAssociation.clear();
      
      AppConfigurationEntry[] entries;
      XMLLoginConfigImpl config = XMLLoginConfigImpl.getInstance();
      config.setConfigResource("jaas-test-config.xml");
      config.loadConfig();
      Configuration.setConfiguration(config);
      
      entries = Configuration.getConfiguration().getAppConfigurationEntry("timer-runas-test");
      assertEquals(1, entries.length);
      
      UsernamePasswordHandler handler = new UsernamePasswordHandler(null, null);
      LoginContext lc = new LoginContext("timer-runas-test", handler);
      
      lc.login();
      
      TimerTester test = (TimerTester) getInitialContext().lookup("TimerTester");
      assertNotNull(test);
      
      test.startTimer(5000);
      Thread.sleep(6000);
      assertTrue(test.isTimerCalled());
      
      lc.logout();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(TimerRunAsTestCase.class, "timer-runas-security.jar");
   }
}