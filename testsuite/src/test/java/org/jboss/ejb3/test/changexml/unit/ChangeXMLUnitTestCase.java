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

import org.jboss.ejb3.test.changexml.TesterRemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
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
      TesterRemote tester = (TesterRemote)getInitialContext().lookup("TesterBean/remote");
      String deployDir = System.getProperty("jbosstest.deploy.dir");
      File file = new File(deployDir + "/" + "changexml.jar"); 
      tester.runTest(file.toURL());
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ChangeXMLUnitTestCase.class, "changexml.jar");
   }

}
