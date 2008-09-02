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
package org.jboss.ejb3.enc;

import java.util.HashMap;
import java.util.Map;

import org.jboss.ejb3.DeploymentScope;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.MessageDestinationMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationsMetaData;

/**
 * Find a message destination link within any module of
 * this JavaEE application deployment.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 * @deprecated use org.jboss.ejb3.resolvers.MessageDestinationReferenceResolver
 */
@Deprecated
public class MessageDestinationResolver
{
   private static final Logger log = Logger.getLogger(MessageDestinationResolver.class);
   
   private DeploymentScope deploymentScope;
   private Map<String, MessageDestinationMetaData> messageDestinations = new HashMap<String, MessageDestinationMetaData>();
   
   public MessageDestinationResolver(DeploymentScope deploymentScope, MessageDestinationsMetaData mergedMessageDestinations)
   {
      this.deploymentScope = deploymentScope;
      if (mergedMessageDestinations != null)
      {
         for (MessageDestinationMetaData dest : mergedMessageDestinations)
         {
            messageDestinations.put(dest.getMessageDestinationName(), dest);
         }
      }
      // TODO Auto-generated constructor stub
   }

   private String getMessageDestinationJNDIName(String name)
   {
      MessageDestinationMetaData dest = messageDestinations.get(name);
      if(dest != null)
         return dest.getJndiName();
      return null;
   }
   
   /**
    * Resolve the message destination link name and return the
    * matching jndi name.
    * 
    * @param link   name of the message destination
    * @return       jndi name
    */
   public String resolveMessageDestination(String link)
   {
      int hashIndex = link.indexOf('#');
      if (hashIndex != -1)
      {
         if (deploymentScope == null)
         {
            log.warn("Message destination link '" + link + "' is relative, but no deployment scope found");
            return null;
         }
         String relativePath = link.substring(0, hashIndex);
         Ejb3Deployment dep = deploymentScope.findRelativeDeployment(relativePath);
         if (dep == null)
         {
            log.warn("Can't find a deployment for path '" + relativePath + "' of message destination link '" + link + "'");
            return null;
         }
         String name = link.substring(hashIndex + 1);
         return dep.resolveMessageDestination(name);
      }
      String jndiName = getMessageDestinationJNDIName(link);
      if(jndiName != null)
         return jndiName;
      for(Ejb3Deployment dep : deploymentScope.getEjbDeployments())
      {
         jndiName = dep.resolveMessageDestination(link);
         if(jndiName != null)
            return jndiName;
      }
      log.warn("Can't find a message destination for link '" + link + "' anywhere");
      return null;
   }
}
