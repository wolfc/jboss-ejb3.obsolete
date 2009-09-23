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
package org.jboss.ejb3.test.changexml.unit;

import java.io.File;

import junit.framework.Test;

import org.jboss.ejb3.test.changexml.ShouldNotBeHereException;
import org.jboss.ejb3.test.changexml.TesterRemote;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * The purpose of this test is to show that the meta data bridges dynamically
 * pick up configuration changes.
 * 
 * As an example we modify the security domain. Note that because we have
 * an unauthenticated principal on the "other" security domain, we set some fake
 * credentials to make sure the security domain gets exercised.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class ChangeXMLUnitTestCase extends JBossTestCase
{
   public ChangeXMLUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testChangeXML() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
      try
      {
         TesterRemote tester = (TesterRemote)getInitialContext().lookup("TesterBean/remote");
         String deployDir = System.getProperty("jbosstest.deploy.dir");
         File file = new File(deployDir + "/" + "changexml.jar"); 
         tester.runTest(file.toURL());
      }
      catch(ShouldNotBeHereException e)
      {
         fail(e.getMessage());
      }
      finally
      {
         client.logout();
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ChangeXMLUnitTestCase.class, "changexml.jar");
   }

}
