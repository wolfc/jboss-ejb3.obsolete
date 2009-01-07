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

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;

/**
 * Service1HasADependency
 * 
 * A @Service bean that depends upon another
 * service to start (and should therefore be started after
 * the dependency)
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Service(objectName = Service1HasADependency.OBJECT_NAME)
@Management(LifecycleManagement.class)
@Depends(Service2IsADependency.OBJECT_NAME)
// < Defines the dependency, and hence order of startup
public class Service1HasADependency extends StartLifecycleRegisteringServiceBase
      implements
         LifecycleManagement
{

   // ----------------------------------------------------------------||
   // Class Members --------------------------------------------------||
   // ----------------------------------------------------------------||

   public static final String OBJECT_NAME = "org.jboss.ejb3.test.ejbthree1530:Service=RequiresDependencyToStartService";

   // ----------------------------------------------------------------||
   // Required Implementations ---------------------------------------||
   // ----------------------------------------------------------------||

   @Override
   public String getObjectName()
   {
      return OBJECT_NAME;
   }

}
