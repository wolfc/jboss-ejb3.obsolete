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

import javax.annotation.Resource;
import javax.ejb.Remote;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Service;

/**
 * ResourceInjectionUsingMappedNameIntoPrimitiveBean
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Service
@Remote(ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.class)
@Depends(ResourceInjectionLifecycleManagement.OBJECT_NAME)
// So JNDI entries are bound first
public class ResourceInjectionUsingMappedNameIntoPrimitiveBean
      implements
         ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   @Resource(mappedName = ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_STRING)
   private String stringValue;

   @Resource(mappedName = ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INT)
   private int intValue;

   @Resource(mappedName = ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INTEGER)
   private Integer integerValue;

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1647.ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness#getIntValue()
    */
   public int getIntValue()
   {
      return intValue;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1647.ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness#getIntegerValue()
    */
   public Integer getIntegerValue()
   {
      return integerValue;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1647.ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness#getStringValue()
    */
   public String getStringValue()
   {
      return stringValue;
   }

}
