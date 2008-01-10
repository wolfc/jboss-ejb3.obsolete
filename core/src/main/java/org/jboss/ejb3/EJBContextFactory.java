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
package org.jboss.ejb3;

import java.util.Hashtable;

import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class EJBContextFactory implements ObjectFactory
{
   /* (non-Javadoc)
    * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
         throws Exception
   {
      Reference ref = (Reference) obj;
      String containerGuid = (String) ref.get("containerGuid").getContent();
      boolean isClustered = Boolean.getBoolean((String)ref.get("isClustered").getContent());
      
      EJBContainer container = (EJBContainer)Ejb3Registry.getContainer(containerGuid);
      if (container == null && isClustered)
      {
         String containerClusterUid = (String) ref.get("containerClusterUid").getContent();
         container = (EJBContainer)Ejb3Registry.getClusterContainer(containerClusterUid);
      }
         
      return container.peekContext().getEJBContext();
   }
}
