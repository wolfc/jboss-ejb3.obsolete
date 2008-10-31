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
package org.jboss.ejb3.test.jms.managed.unit;

import javax.naming.InitialContext;
import org.jboss.ejb3.test.jms.managed.JMSTest;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;


public class ManagedTestCase
   extends JBossTestCase
{   
   protected InitialContext initialContext;
   
   private static Logger log = Logger.getLogger(ManagedTestCase.class);
   
   public ManagedTestCase(String name)
   {
      super(name);
   }
   
   public void setUp() throws Exception
   {
      initialContext = new InitialContext();
   }
   
   public void tearDown() throws Exception
   {
   }
   
   
   public void test1() throws Exception
   {   
      JMSTest testBean = (JMSTest)initialContext.lookup("jms-test-ejbs/JMSTest");
        
      testBean.test1();
      testBean.remove(); 
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(ManagedTestCase.class, "jms-managed.jar");
   }
}
