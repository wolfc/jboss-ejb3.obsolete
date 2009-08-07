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

/**
 * RunAsLocalBusiness
 * 
 * Local business interface of operations that will 
 * be run as another identity
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface RunAsRemoteBusiness
{
   //------------------------------------------------------------------------||
   // Constants -------------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * JNDI Name to which this Service will be bound
    */
   String JNDI_NAME = "RunAs/remote";

   //------------------------------------------------------------------------||
   // Contracts -------------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Delegates to a protected service {@link ProtectedLocalBusiness#getValue()}
    * as cached from a lookup to the protected service during start()
    */
   String getProtectedValueObtainedFromStart();

   /**
    * Obtains the String value directly from the service
    * 
    * @return
    */
   String getValueFromProtectedService();
}
