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

package org.jboss.ejb3.test.ejbthree1807;

import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.Cache;
import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;


/**
 * @author Brian Stansberry
 *
 */
@Stateful(name = "RemoveRejecterBean")
@Remote(RemoveRejecter.class)
@RemoteBinding(jndiBinding = "RemoveRejecter")
@CacheConfig(name = "jboss.cache:service=EJB3SFSBClusteredCache", maxSize = 1000, idleTimeoutSeconds = 0, removalTimeoutSeconds = 1)
@Cache("StatefulTreeCache")
public class RemoveRejecterBean implements RemoveRejecter
{
   private static final Logger log = Logger.getLogger(RemoveRejecterBean.class);
   
   private boolean reject;
   
   
   @PreDestroy
   public void remove()
   {
      if (reject)
      {
         reject = false;
         log.info("Rejecting remove");
         throw new RuntimeException("Remove rejected");
      }
      else
      {
         log.info("Allowing remove");
      }
   }


   public void setRejectRemove(boolean reject)
   {
      this.reject = reject;      
   }

}
