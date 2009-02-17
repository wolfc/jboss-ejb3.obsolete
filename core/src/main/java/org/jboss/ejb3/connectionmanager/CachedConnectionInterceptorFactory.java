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
package org.jboss.ejb3.connectionmanager;

import java.util.HashSet;
import java.util.Set;

import org.jboss.aop.Advisor;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.interceptors.aop.AbstractInterceptorFactory;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class CachedConnectionInterceptorFactory extends AbstractInterceptorFactory
{
   @Override
   public Object createPerClass(Advisor advisor)
   {
      EJBContainer container = EJBContainer.getEJBContainer(advisor);
      Set<String> unsharableResources = new HashSet<String>();
      JBossEnterpriseBeanMetaData metaData = container.getXml();
      ResourceReferencesMetaData resRefs = metaData.getResourceReferences();
      if(resRefs != null)
      {
         for(ResourceReferenceMetaData resRef : resRefs)
         {
            String jndiName = resRef.getJndiName();
            if(jndiName == null)
               jndiName = resRef.getResolvedJndiName();
            // for res-url resources
            if(jndiName == null)
               continue;
            int i = jndiName.indexOf(':');
            if(jndiName.charAt(i + 1) == '/')
               i++;
            unsharableResources.add(jndiName.substring(i + 1));
         }
      }
      return new CachedConnectionInterceptor(unsharableResources);
   }
}
