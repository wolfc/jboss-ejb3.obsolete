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
package org.jboss.ejb3.resolvers;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * The default implementation of a MessageDestinationReferenceResolver.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DefaultMessageDestinationReferenceResolver implements MessageDestinationReferenceResolver
{
   private static String findMessageDestination(DeploymentUnit deploymentUnit, MessageDestinationsMetaData destinations, String name)
   {
      if(destinations == null)
         return null;
      MessageDestinationMetaData destination = destinations.get(name);
      if(destination != null)
      {
         String jndiName = destination.getMappedName();
         if(jndiName != null)
            return jndiName;
         else
            throw new IllegalStateException("Message destination " + name + " in " + deploymentUnit + " does not define a mapped name");
      }
      return null;
   }
   
   private static String findWithinApplication(DeploymentUnit unit, String name)
   {
      String jndiName = findWithinModule(unit, name);
      if(jndiName != null)
         return jndiName;
      
      for(DeploymentUnit child : unit.getChildren())
      {
         jndiName = findWithinApplication(child, name);
         if(jndiName != null)
            return jndiName;
      }
      return null;
   }
   
   private static String findWithinModule(DeploymentUnit deploymentUnit, String name)
   {
      JBossMetaData ejbMetaData = deploymentUnit.getAttachment(JBossMetaData.class);
      if(ejbMetaData != null)
      {
         MessageDestinationsMetaData destinations = ejbMetaData.getAssemblyDescriptor().getMessageDestinations();
         String jndiName = findMessageDestination(deploymentUnit, destinations, name);
         if(jndiName != null)
            return jndiName;
      }
      
      JBossWebMetaData webMetaData = deploymentUnit.getAttachment(JBossWebMetaData.class);
      if(webMetaData != null)
      {
         MessageDestinationsMetaData destinations = webMetaData.getMessageDestinations();
         String jndiName = findMessageDestination(deploymentUnit, destinations, name);
         if(jndiName != null)
            return jndiName;
      }
      
      JBossClientMetaData clientMetaData = deploymentUnit.getAttachment(JBossClientMetaData.class);
      if(clientMetaData != null)
      {
         MessageDestinationsMetaData destinations = clientMetaData.getMessageDestinations();
         String jndiName = findMessageDestination(deploymentUnit, destinations, name);
         if(jndiName != null)
            return jndiName;
      }
      return null;
   }
   
   // TODO: duplicate of DefaultPersistenceUnitDependencyResolver.getDeploymentUnit
   private static DeploymentUnit getDeploymentUnit(DeploymentUnit current, String path)
   {
      if(path.startsWith("/"))
         return getDeploymentUnit(current.getTopLevel(), path.substring(1));
      if(path.startsWith("./"))
         return getDeploymentUnit(current, path.substring(2));
      if(path.startsWith("../"))
         return getDeploymentUnit(current.getParent(), path.substring(3));
      int i = path.indexOf('/');
      String name;
      if(i == -1)
         name = path;
      else
         name = path.substring(0, i);
      for(DeploymentUnit child : current.getChildren())
      {
         if(child.getSimpleName().equals(name))
            return child;
      }
      throw new IllegalArgumentException("Can't find a deployment unit named " + name + " at " + current);
   }
   
   public String resolveMessageDestinationJndiName(DeploymentUnit deploymentUnit, String link)
   {
      int i = (link == null ? -1 : link.indexOf('#'));
      if(i != -1)
      {
         String path = link.substring(0, i);
         link = link.substring(i + 1);
         // Since we want to look relatively to the component jar, we should start in
         // our parent.
         deploymentUnit = getDeploymentUnit(deploymentUnit, "../" + path);
         String jndiName = findWithinModule(deploymentUnit, link);
         if(jndiName != null)
            return jndiName;
      }
      else
      {
         String jndiName = findWithinModule(deploymentUnit, link);
         if(jndiName != null)
            return jndiName;
         // TODO: can't find this in the spec, but apparently it's allowed.
         jndiName = findWithinApplication(deploymentUnit.getTopLevel(), link);
         if(jndiName != null)
            return jndiName;
      }
      throw new IllegalArgumentException("Deployment unit " + deploymentUnit + " does not define a message destination " + link);
   }
}
