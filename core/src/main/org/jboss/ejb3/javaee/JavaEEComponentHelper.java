/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.javaee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.ejb3.Ejb3Module;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 64075 $
 */
public class JavaEEComponentHelper
{
   /**
    * Create an object name refering to the enterprise bean in this module.
    * 
    * @param module
    * @param ejbName
    * @return
    */
   public static String createObjectName(JavaEEModule module, String ejbName)
   {
      return createObjectName(module, module.getName(), ejbName);
   }
   
   /**
    * Create an object name refering the enterprise bean in another module.
    * 
    * If the module name is not known, it will create an object name pattern.
    * 
    * @param module         this module
    * @param unitName       the name of the other module or null if not known
    * @param ejbName        the name of the enterprise bean
    * @return               the canonical object name
    */
   public static String createObjectName(JavaEEModule module, String unitName, String ejbName)
   {
      // TODO: currently it's only for EJB3 service
      StringBuilder sb = new StringBuilder(Ejb3Module.BASE_EJB3_JMX_NAME + ",");
      JavaEEApplication ear = module.getApplication();
      if (ear != null)
      {
         sb.append("ear=");
         sb.append(ear.getName());
         sb.append(",");
      }
      if(unitName == null)
      {
         sb.append("*");
      }
      else
      {
         sb.append("jar=");
         sb.append(unitName);
      }
      sb.append(",name=");
      sb.append(ejbName);
      try
      {
         ObjectName on = new ObjectName(sb.toString());
         return on.getCanonicalName();
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException(e);
      }
   }
}
