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
package org.jboss.ejb3.core.resolvers;

import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.common.resolvers.spi.EjbReference;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolverBase;
import org.jboss.ejb3.common.resolvers.spi.UnresolvableReferenceException;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ScopedEJBReferenceResolver extends EjbReferenceResolverBase
   implements EjbReferenceResolver
{
   protected String find(DeploymentUnit du, EjbReference reference)
   {
      JBossMetaData metadata = getMetaData(du);

      return getMatch(reference, metadata, du.getClassLoader());
   }
   
   /**
    * Search a deployment unit and it's children.
    * 
    * @param du
    * @param reference
    * @return
    */
   protected String findWithin(DeploymentUnit du, DeploymentUnit excludeChild, EjbReference reference)
   {
      String jndiName = find(du, reference);
      if(jndiName != null)
         return jndiName;
      
      List<DeploymentUnit> children = du.getChildren();
      if(children != null)
      {
         for(DeploymentUnit child : children)
         {
            // already searched that one
            if(child == excludeChild)
               continue;
            
            jndiName = findWithin(child, null, reference);
            if(jndiName != null)
               return jndiName;
         }
      }
      
      DeploymentUnit parent = du.getParent();
      if(parent != null)
         return findWithin(parent, du, reference);
      return null;
   }
   
   public String resolveEjb(DeploymentUnit du, EjbReference reference) throws UnresolvableReferenceException
   {
      String jndiName = findWithin(du, null, reference);
      if(jndiName == null)
         throw new UnresolvableReferenceException("Could not resolve reference " + reference + " in " + du);
      return jndiName;
   }
}
