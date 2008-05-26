/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1127;

import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * A Delegate Bean for testing the locals
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(DelegateRemoteBusiness.class)
@RemoteBinding(jndiBinding = DelegateRemoteBusiness.JNDI_NAME)
public class DelegateBean implements DelegateRemoteBusiness
{
   // Class Members
   private static final Logger logger = Logger.getLogger(DelegateBean.class);

   // Instance Members
   @EJB
   TestLocalBusiness localBusiness;

   // Required Implementations

   public int testLocalBusinessOnlyDefined()
   {
      logger.info("testLocalBusinessOnlyDefined");
      return localBusiness.test();
   }

   public int testNoLocalExplicitlyDefined()
   {
      logger.info("testNoLocalExplicitlyDefined");

      // Obtain reference to home
      TestLocalHome home = null;
      try
      {
         home = (TestLocalHome)new InitialContext().lookup(TestLocalHome.JNDI_NAME);
      }
      catch (NamingException ne)
      {
         throw new RuntimeException(ne);
      }
      
      // Invoke
      try
      {
         return home.create().test();
      }
      catch (CreateException e)
      {
         throw new RuntimeException(e);
      }
   }

   public int testNoLocalHomeDefined()
   {
      logger.info("testNoLocalHomeDefined");
      
      // Obtain reference to local
      TestLocal local = null;
      try
      {
         local = (TestLocal)new InitialContext().lookup(TestLocal.JNDI_NAME_NO_LOCAL_HOME);
      }
      catch (NamingException ne)
      {
         throw new RuntimeException(ne);
      }
      
      
      return local.test();
   }

}
