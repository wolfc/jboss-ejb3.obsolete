/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.locator.integration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry to map all business interfaces of currently deployed EJBs
 * to their names in JNDI 
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision $
 */
public class DeployedEjbsRegistry
{

   // Class Members

   // Instance Members
   private final Map<Class<?>, String> interfacesToJndiNameMappings = Collections
         .synchronizedMap(new HashMap<Class<?>, String>());

   // Constructor
   public DeployedEjbsRegistry()
   {
      super();
   }

   // Functional Methods
   
   /**
    *
    */
   public void registerBusinessInterface(Class<?> clazz,String jndiName)
   {
      throw new RuntimeException("REVIEW API AND IMPLEMENT");
   }

}
