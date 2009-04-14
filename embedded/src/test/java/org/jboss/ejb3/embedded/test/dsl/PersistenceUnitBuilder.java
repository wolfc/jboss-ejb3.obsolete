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
package org.jboss.ejb3.embedded.test.dsl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.metadata.jpa.spec.PersistenceUnitMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PersistenceUnitBuilder extends PersistenceUnitMetaData
{
   private static final long serialVersionUID = 1L;

   public static PersistenceUnitBuilder unit(String name)
   {
      return new PersistenceUnitBuilder(name);
   }

   protected PersistenceUnitBuilder(String name)
   {
      setName(name);
   }
   
   public PersistenceUnitBuilder classes(Set<String> classes)
   {
      setClasses(classes);
      return this;
   }
   
   public PersistenceUnitBuilder description(String description)
   {
      setDescription(description);
      return this;
   }
   
   public PersistenceUnitBuilder jtaDataSource(String jtaDataSource)
   {
      setJtaDataSource(jtaDataSource);
      return this;
   }
   
   public PersistenceUnitBuilder properties(Map<String, String> properties)
   {
      setProperties(properties);
      return this;
   }
   
   public PersistenceUnitBuilder property(String name, String value)
   {
      if(getProperties() == null)
         setProperties(new HashMap<String, String>());
      getProperties().put(name, value);
      return this;
   }
}
