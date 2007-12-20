/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.jbas4489.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.jbas4489.BMTCleanUp;
import org.jboss.test.JBossTestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 63564 $
 */
public class BMTCleanUpUnitTestCase extends JBossTestCase
{
   public BMTCleanUpUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(BMTCleanUpUnitTestCase.class, "jbas4489.jar");
   }
   
   public void testIncomplete() throws Exception
   {
      BMTCleanUp bean = getBean();
      bean.testIncomplete();
   }
   
   public void testTxTimeout() throws Exception
   {
      BMTCleanUp bean = getBean();
      bean.testTxTimeout();
   }

   private BMTCleanUp getBean() throws Exception
   {
      return (BMTCleanUp) getInitialContext().lookup("BMTCleanUpBean/remote");
   }
}
