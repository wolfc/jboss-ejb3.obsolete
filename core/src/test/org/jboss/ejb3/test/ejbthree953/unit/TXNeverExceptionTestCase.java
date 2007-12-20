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
package org.jboss.ejb3.test.ejbthree953.unit;

import javax.ejb.EJBException;
import javax.transaction.UserTransaction;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree953.MyStateless;
import org.jboss.test.JBossTestCase;

/**
 * Test exception type on a method with transaction attribute NEVER.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class TXNeverExceptionTestCase extends JBossTestCase
{

   public TXNeverExceptionTestCase(String name)
   {
      super(name);
   }

   public void test1() throws Exception
   {
      UserTransaction tx = (UserTransaction) this.getInitialContext().lookup("UserTransaction");
      tx.begin();
      try
      {
         MyStateless test = (MyStateless) this.getInitialContext().lookup("MyStatelessBean/remote");
         try
         {
            test.check();
         }
         catch(EJBException e)
         {
            // good
         }
         catch(IllegalStateException e)
         {
            fail("Should have caught an EJBException, not an IllegalStateException");
         }
      }
      finally
      {
         tx.rollback();
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(TXNeverExceptionTestCase.class, "ejbthree953.jar");
   }

}
