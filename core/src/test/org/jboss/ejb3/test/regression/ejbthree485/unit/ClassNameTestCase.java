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
package org.jboss.ejb3.test.regression.ejbthree485.unit;

import java.rmi.RMISecurityManager;

import javax.naming.Context;
import junit.framework.Test;

import org.jboss.ejb3.test.regression.ejbthree485.MyException;
import org.jboss.ejb3.test.regression.ejbthree485.MyRemote;
import org.jboss.test.JBossTestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision: 61136 $
 */
public class ClassNameTestCase extends JBossTestCase
{
   public ClassNameTestCase(String name)
   {
      super(name);
   }

   public void testException() throws Exception {
      Context ctx = getInitialContext();
      MyRemote remote = (MyRemote) ctx.lookup("StatelessBean/remote");
      try {
         remote.giveMeAnException();
         
         fail("expected an exception");
      }
      catch(MyException e) {
         e.printStackTrace();
         
         StackTraceElement stackTrace[] = e.getStackTrace();
         int i = 0;
         while(i < stackTrace.length && !stackTrace[i].getClassName().equals(ClassNameTestCase.class.getName()))
            i++;
         //assertTrue(i < stackTrace.length); // let's error if this happens
         i--;
         String actual = stackTrace[i].getClassName();
         String expected = "?";
         assertFalse("class name must not start with $Proxy", actual.startsWith("$Proxy"));
         assertEquals(expected, actual);
         return;
      }
   }
   
   public void testRuntimeException() throws Exception {
      Context ctx = getInitialContext();
      MyRemote remote = (MyRemote) ctx.lookup("StatelessBean/remote");
      try {
         remote.giveMeARuntimeException();
         
         fail("expected an exception");
      }
      catch(RuntimeException re) {
         Throwable e = re.getCause();
         StackTraceElement stackTrace[] = e.getStackTrace();
         int i = 0;
         while(i < stackTrace.length && !stackTrace[i].getClassName().equals(ClassNameTestCase.class.getName()))
            i++;
         //assertTrue(i < stackTrace.length); // let's error if this happens
         i--;
         String actual = stackTrace[i].getClassName();
         String expected = "?";
         assertFalse("class name must not start with $Proxy", actual.startsWith("$Proxy"));
         assertEquals(expected, actual);
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(ClassNameTestCase.class, "ejbthree485.jar");
   }
}
