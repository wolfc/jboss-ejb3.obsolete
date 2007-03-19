/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3;

import java.util.HashSet;

import org.jboss.beans.metadata.plugins.AbstractDemandMetaData;
import org.jboss.beans.metadata.spi.DemandMetaData;

/**
 * dependency registry for Microcontainer
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class MCDependencyPolicy implements DependencyPolicy
{
   protected HashSet dependencies = new HashSet();

   public void addDependency(String dependency)
   {
      dependencies.add(dependency);
   }

   public void addDatasource(String jndiName)
   {
      String ds = jndiName;
      if (ds.startsWith("java:/"))
      {
         ds = ds.substring(6);

      }
      else if (ds.startsWith("java:"))
      {
         ds = ds.substring(5);
      }
      addDependency(ds);
   }

   public HashSet<DemandMetaData> getDependencies()
   {
      HashSet<DemandMetaData> set = new HashSet<DemandMetaData>();
      for (Object depends : dependencies)
      {
         set.add(new AbstractDemandMetaData(depends));
      }
      return set;
   }
}
