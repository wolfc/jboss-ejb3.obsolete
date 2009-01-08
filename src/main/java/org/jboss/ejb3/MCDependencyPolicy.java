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
import java.util.Set;

import org.jboss.beans.metadata.plugins.AbstractDemandMetaData;
import org.jboss.beans.metadata.plugins.AbstractSupplyMetaData;
import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.SupplyMetaData;
import org.jboss.ejb3.dependency.EjbLinkDemandMetaData;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.kernel.JNDIKernelRegistryPlugin;

/**
 * dependency registry for Microcontainer
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class MCDependencyPolicy implements DependencyPolicy
{
   private JavaEEComponent component;
   private Set<DependencyMetaData> dependencies = new HashSet<DependencyMetaData>();
   private Set<DemandMetaData> demands = new HashSet<DemandMetaData>();
   private Set<SupplyMetaData> supplies = new HashSet<SupplyMetaData>();

   public MCDependencyPolicy(JavaEEComponent component)
   {
      assert component != null : "component is null";
      
      this.component = component;
   }
   
   public void addDependency(String dependency)
   {
      addDependency(new AbstractDemandMetaData(dependency));
   }
   public void addDependency(DemandMetaData dependency)
   {
      demands.add(dependency);      
   }
   public void addDependency(DependencyMetaData dependency)
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

   
   public void addDependency(Class<?> businessInterface)
   {
      // TODO: more sophisticated
      addDependency("Class:" + businessInterface.getName());
   }
   
   /**
    * Add a dependency on an enterprise bean.
    * 
    * Optionally the ejb link is prefixed with the path name to
    * another ejb-jar file separated with a '#' to the enterprise bean's name.
    * 
    * @param ejbLink        the name of the target enterprise bean
    * @param businessInterface
    */
   public void addDependency(String ejbLink, Class<?> businessInterface)
   {
      assert ejbLink != null : "ejbLink is null";
      
      // Note that businessInterface is always ignored during resolving.
      
      // FIXME: less hacky
      
      int hashIndex = ejbLink.indexOf('#');
      if (hashIndex != -1)
      {
//         if (deploymentScope == null)
//         {
//            log.warn("ejb link '" + ejbLink + "' is relative, but no deployment scope found");
//            return null;
//         }
         String unitName = ejbLink.substring(0, hashIndex);
//         Ejb3Deployment dep = deploymentScope.findRelativeDeployment(relativePath);
//         if (dep == null)
//         {
//            log.warn("can't find a deployment for path '" + relativePath + "' of ejb link '" + ejbLink + "'");
//            return null;
//         }
         String ejbName = ejbLink.substring(hashIndex + 1);
         addDependency(component.createObjectName(unitName, ejbName));
      }
      else
         addDependency(new EjbLinkDemandMetaData(component, ejbLink));
   }
   
   public void addJNDIName(String name)
   {
      assert name != null : "name is null";
      assert name.length() > 0 : "name is empty";
      
      addDependency(JNDIKernelRegistryPlugin.JNDI_DEPENDENCY_PREFIX + name);
   }
   
   public Set<DependencyMetaData> getDependencies()
   {
      return dependencies;
   }
   public Set<DemandMetaData> getDemands()
   {
      return demands;
   }
   
   public void addSupply(Class<?> businessInterface)
   {
      supplies.add(new AbstractSupplyMetaData("Class:" + businessInterface.getName()));
   }
   
   public Set<SupplyMetaData> getSupplies()
   {
      return supplies;
   }
   
   public DependencyPolicy clone()
   {
      MCDependencyPolicy clone = new MCDependencyPolicy(this.component);
      clone.supplies.addAll(this.supplies);
      clone.demands.addAll(this.demands);
      clone.dependencies.addAll(this.dependencies);
      return clone;
   }
}
