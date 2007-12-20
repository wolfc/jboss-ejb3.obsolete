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


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import org.jboss.ejb3.remoting.Proxy;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 68144 $
 */
public abstract class LocalProxy implements InvocationHandler, Externalizable, Proxy
{
   private static Logger log = Logger.getLogger(LocalProxy.class);
   
   private transient Container container = null;
   protected String containerClusterUid;
   protected String containerGuid;
   protected String proxyName;


   public LocalProxy()
   {
   }

   protected LocalProxy(Container container)
   {
      this.container = container;
      this.containerGuid = Ejb3Registry.guid(container);
      this.containerClusterUid = Ejb3Registry.clusterUid(container);
      proxyName = container.getEjbName();
   }

   protected Container getContainer()
   {
      if(container == null)
         container = Ejb3Registry.findContainer(containerGuid);
      if(container == null)
         log.warn("Container " + containerGuid + " is not yet available");
      return container;
   }
   
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      this.containerGuid = in.readUTF();
      this.containerClusterUid = in.readUTF();
      this.proxyName = in.readUTF();
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(containerGuid);
      out.writeUTF(containerClusterUid);
      out.writeUTF(proxyName);
   }

   public abstract String toString();
}
