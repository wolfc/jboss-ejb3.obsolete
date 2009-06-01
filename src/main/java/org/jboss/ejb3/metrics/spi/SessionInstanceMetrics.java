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
package org.jboss.ejb3.metrics.spi;

/**
 * SessionInstanceMetrics
 * 
 * Represents backing instance metrics of any type of 
 * EJB 3.x Session Bean, typically targeting the underlying 
 * instance pool or cache
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface SessionInstanceMetrics
{
   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the current size of the underlying 
    * pool/cache of bean instances
    * 
    * @return
    */
   int getCurrentSize();

   /**
    * Obtains the number of instances created for this EJB
    * 
    * @return
    */
   int getCreateCount();

   /**
    * Obtains the number of instances removed for this EJB
    * 
    * @return
    */
   int getRemoveCount();

   /**
    * Obtains the number of instances currently available for 
    * service for this EJB
    * 
    * @return
    */
   int getAvailableCount();

   /**
    * Obtains the size of the underlying instance pool/cache
    * at it highest 
    * 
    * @return
    */
   int getMaxSize();
}
