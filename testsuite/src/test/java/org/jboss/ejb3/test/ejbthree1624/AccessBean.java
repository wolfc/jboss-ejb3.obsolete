/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1624;

import javax.ejb.Remote;

import org.jboss.ejb3.annotation.Service;
import org.jboss.ejb3.common.registrar.spi.DuplicateBindException;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.common.registrar.spi.NotBoundException;
import org.jboss.logging.Logger;

/**
 * AccessBean
 * 
 * An EJB to act as a proxy to the underlying MC Bean
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Service
@Remote(AccessRemoteBusiness.class)
public class AccessBean implements AccessRemoteBusiness, AccessManagement
{
   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(AccessBean.class);

   private static final String beanBindName = "testMcBean";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private McBean delegate;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Adds the specified arguments by way of the 
    * local business delegate
    * 
    * @param args
    * @return
    */
   public int addUsingLocalBusinessView(int... args)
   {
      return delegate.addUsingLocalBusinessView(args);
   }

   /**
    * Adds the specified arguments by way of the 
    * remote business delegate
    * 
    * @param args
    * @return
    */
   public int addUsingRemoteBusinessView(int... args)
   {
      return delegate.addUsingRemoteBusinessView(args);
   }

   /**
    * Adds the specified arguments by way of the 
    * local component (EJB2.x) delegate
    * 
    * @param args
    * @return
    */
   public int addUsingLocalComponentView(int... args)
   {
      return delegate.addUsingLocalComponentView(args);
   }

   /**
    * Adds the specified arguments by way of the 
    * remote component (EJB2.x) delegate
    * 
    * @param args
    * @return
    */
   public int addUsingRemoteComponentView(int... args)
   {
      return delegate.addUsingRemoteComponentView(args);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Installs the MC Bean Delegate
    */
   public void start() throws Exception
   {
      // Initialize
      Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();

      // Make an POJO and install into MC
      McBean bean = new McBean();
      registrar.rebind(beanBindName, bean);
      log.info("Installed MC Bean delegate " + bean + " at " + beanBindName);

      // Set delegate
      this.delegate = bean;
   }

   /**
    * Removes the MC Bean Delegate
    */
   public void stop()
   {
      // Unbind MC Bean
      try
      {
         Ejb3RegistrarLocator.locateRegistrar().unbind(beanBindName);
      }
      catch (NotBoundException nbe)
      {
         // Ignore  
      }
      log.info("Removed MC Bean Delegate from " + beanBindName);

      // Null out delegate
      this.delegate = null;
   }
}
