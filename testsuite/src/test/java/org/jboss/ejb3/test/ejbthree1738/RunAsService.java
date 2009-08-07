/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1738;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Remote;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.Service;
import org.jboss.logging.Logger;

/**
 * RunAsService
 * 
 * A Service Bean which executes under an authorized
 * identity.  
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Service
@SecurityDomain(ProtectedLocalBusiness.SECURITY_DOMAIN)
@PermitAll
@RunAs(ProtectedLocalBusiness.ROLE_ALLOWED)
@Remote(RunAsRemoteBusiness.class)
@RemoteBinding(jndiBinding = RunAsRemoteBusiness.JNDI_NAME)
public class RunAsService implements RunAsRemoteBusiness
{

   //------------------------------------------------------------------------||
   // Class Members ---------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(RunAsService.class);

   //------------------------------------------------------------------------||
   // Instance Members ------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Cached value obtained during bean start() from
    * the protected service
    */
   private String value;

   /**
    * Protected service from which we'll get a value
    */
   @EJB
   private ProtectedLocalBusiness protectedService;

   //------------------------------------------------------------------------||
   // Required Implementations ----------------------------------------------||
   //------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1738.unit.RunAsLocalBusiness#getProtectedValue()
    */
   public String getProtectedValueObtainedFromStart()
   {
      return value;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1738.RunAsRemoteBusiness#getValueFromProtectedService()
    */
   public String getValueFromProtectedService()
   {
      return protectedService.getValue();
   }

   //------------------------------------------------------------------------||
   // Lifecycle -------------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Callback by the container, invoked on start.  Here we try to get
    * a value from the protected service to see if the security context 
    * is set up properly.
    */
   public void start() throws Exception
   {
      log.info("Start");
      log.info("Attempting to get value from protected service");
      try
      {
         value = protectedService.getValue();
      }
      catch (final Exception e)
      {
         log.error("Could not get value from protected service in lifecycle start(): " + e.getMessage());
         return;
      }
      log.info("Got value from protected service: " + value);
   }

}
