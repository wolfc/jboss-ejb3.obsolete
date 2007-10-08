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
package org.jboss.ejb3.cache.impl;

import org.jboss.ejb3.cache.PassivationManager;
import org.jboss.ejb3.cache.StatefulObjectFactory;
import org.jboss.ejb3.cache.grouped.SerializationGroup;
import org.jboss.logging.Logger;

/**
 * Comment
 *  
 *  FIXME determine whether SerializationGroup clustering support should
 *  be controlled by a property of this container or via a param passed
 *  to create(). 
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SerializationGroupContainer implements StatefulObjectFactory<SerializationGroup>, PassivationManager<SerializationGroup>
{
   private static final Logger log = Logger.getLogger(SerializationGroupContainer.class);
   
   private boolean clustered;
   
   public boolean isClustered()
   {
      return clustered;
   }
   
   public void setClustered(boolean clustered)
   {
      this.clustered = clustered;
   }

   public SerializationGroup create(Class<?>[] initTypes, Object[] initValues)
   {
      SerializationGroup group = new SerializationGroupImpl();
      // TODO should this be controlled via one of the initValues?
      group.setClustered(clustered);
      return group;
   }

   public void destroy(SerializationGroup obj)
   {
      // TODO: nothing?
   }

   public void postActivate(SerializationGroup obj)
   {
      log.trace("post activate " + obj);
      obj.postActivate();
   }

   public void prePassivate(SerializationGroup obj)
   {
      log.trace("pre passivate " + obj);
      obj.prePassivate();
   }

   public void postReplicate(SerializationGroup obj)
   {
      if (!clustered)
         throw new UnsupportedOperationException("Clustering not supported");
      log.trace("post replicate " + obj);
      obj.postReplicate();
   }

   public void preReplicate(SerializationGroup obj)
   {
      if (!clustered)
         throw new UnsupportedOperationException("Clustering not supported");
      log.trace("pre replicate " + obj);
      obj.preReplicate();
   }

   
}
