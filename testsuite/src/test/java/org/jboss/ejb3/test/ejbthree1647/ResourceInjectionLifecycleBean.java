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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.jboss.naming.Util;

/**
 * ResourceInjectionLifecycleBean
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Service(objectName = ResourceInjectionLifecycleManagement.OBJECT_NAME)
@Management(ResourceInjectionLifecycleManagement.class)
public class ResourceInjectionLifecycleBean implements ResourceInjectionLifecycleManagement
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * Get global JNDI Naming Context
    */
   private static Context context;
   static
   {
      try
      {
         context = new InitialContext();
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not get naming context", e);
      }
   }

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Binds all requisite values into JNDI
    */
   public void start() throws Exception
   {
      // String
      Util.rebind(context, ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_STRING,
            ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.VALUE_STRING);

      // int
      Util.rebind(context, ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INT,
            ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.VALUE_INT);

      // Integer
      Util.rebind(context, ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INTEGER,
            ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.VALUE_INTEGER);

   }

   /**
    * Cleans up the values bound into JNDI
    */
   public void stop()
   {
      try
      {
         // String
         Util.unbind(context, ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_STRING);

         // int
         Util.unbind(context, ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INT);

         // Integer
         Util.unbind(context, ResourceInjectionUsingMappedNameIntoPrimitiveRemoteBusiness.JNDI_LOCATION_INTEGER);
      }
      catch (NamingException ne)
      {
         throw new RuntimeException(ne);
      }
   }
}
