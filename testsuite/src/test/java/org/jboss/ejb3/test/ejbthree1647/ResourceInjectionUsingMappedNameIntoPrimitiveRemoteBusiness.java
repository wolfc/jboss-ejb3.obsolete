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
package org.jboss.ejb3.test.ejbthree1647;

/**
 * ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness
 * 
 * A remote business interface for an EJB which will inject
 * into a primitive/Wrapper/String type using @Resource.mappedName
 * 
 * EJBTHREE-1647
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness
{
   // --------------------------------------------------------------------------------||
   // Constants ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * The following defines JNDI locations
    */

   String JNDI_LOCATION_PREFIX = "EJBTHREE1647/";

   String JNDI_LOCATION_STRING = JNDI_LOCATION_PREFIX + "String";

   String JNDI_LOCATION_INT = JNDI_LOCATION_PREFIX + "int";

   String JNDI_LOCATION_INTEGER = JNDI_LOCATION_PREFIX + "Integer";

   /*
    * The following will be placed into JNDI
    * at their corresponding location.
    * 
    * All are String values because this is how metadata
    * env-refs are stored
    */

   String VALUE_STRING = "A test String value";

   String VALUE_INT = new Integer(8453459).toString();

   String VALUE_INTEGER = VALUE_INT;

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the injected String value
    * 
    * @return
    */
   String getStringValue();

   /**
    * Obtains the injected int value
    * 
    * @return
    */
   int getIntValue();

   /**
    * Obtains the injected Integer value
    * @return
    */
   Integer getIntegerValue();
}
