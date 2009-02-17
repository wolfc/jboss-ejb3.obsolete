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
package org.jboss.ejb3.test.ejbthree1530;

import org.jboss.logging.Logger;

/**
 * LifecycleEventRegisteringServiceBase
 * 
 * A base for a @Service that registers lifecycle events
 * with a reporter
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class LifecycleEventRegisteringServiceBase implements LifecycleManagement
{

   // ----------------------------------------------------------------||
   // Class Members --------------------------------------------------||
   // ----------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(LifecycleEventRegisteringServiceBase.class);

   // ----------------------------------------------------------------||
   // Required Implementations ---------------------------------------||
   // ----------------------------------------------------------------||

   public void start() throws Exception
   {
      // Log and register
      String objectName = this.getObjectName();
      log.info("START: " + objectName);
      LifecycleReporterBean.STARTED_SERVICE_NAMES.add(objectName);
   }
   
   public void create() throws Exception
   {
      // Log and register
      String objectName = this.getObjectName();
      log.info("CREATE: " + objectName);
      LifecycleReporterBean.CREATED_SERVICE_NAMES.add(objectName);
   }

   // ----------------------------------------------------------------||
   // Contracts ------------------------------------------------------||
   // ----------------------------------------------------------------||

   /**
    * Returns the ObjectName (String form) for this service
    */
   public abstract String getObjectName();

}
