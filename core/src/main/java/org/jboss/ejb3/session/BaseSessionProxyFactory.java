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
package org.jboss.ejb3.session;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.ProxyFactory;
import org.jboss.ejb3.ProxyFactoryHelper;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.proxy.EJBMetaDataImpl;
import org.jboss.ejb3.proxy.handle.HomeHandleImpl;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
public abstract class BaseSessionProxyFactory implements ProxyFactory, Externalizable
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(BaseSessionProxyFactory.class);
   
   private SessionContainer container;
   protected String containerGuid;
   protected String containerClusterUid;
   protected boolean isClustered = false;
   
   public BaseSessionProxyFactory()
   {
   }
   
   protected BaseSessionProxyFactory(SessionContainer container)
   {
      assert container != null : "container is null";
      
      setContainer(container);
   }
   
   public Object createHomeProxy()
   {
      throw new RuntimeException("NYI");
   }
   
   protected void setContainer(SessionContainer container)
   {
      this.container = container;
      this.containerGuid = Ejb3Registry.guid(container);
      this.containerClusterUid = Ejb3Registry.clusterUid(container);
      this.isClustered = container.isClustered();
   }
   
   protected SessionContainer getContainer()
   {
      if (container == null)
      {
         container = (SessionContainer)Ejb3Registry.findContainer(containerGuid);
         
         if (container == null && isClustered)
            container = (SessionContainer)Ejb3Registry.getClusterContainer(containerClusterUid);
      }
      
      return container;
   }
   
   protected void setEjb21Objects(BaseSessionRemoteProxy proxy)
   {
      proxy.setHandle(getHandle());
      proxy.setHomeHandle(getHomeHandle());
      proxy.setEjbMetaData(getEjbMetaData());
   }
   
   abstract protected Handle getHandle();
   
   protected HomeHandle getHomeHandle()
   {
      EJBContainer ejbContainer = (EJBContainer)container;
      
      HomeHandleImpl homeHandle = null;
      
      RemoteBinding remoteBindingAnnotation = ejbContainer.getAnnotation(RemoteBinding.class);
      if (remoteBindingAnnotation != null)
         homeHandle = new HomeHandleImpl(ProxyFactoryHelper.getHomeJndiName(container));
      
      return homeHandle;
   }
   
   protected EJBMetaData getEjbMetaData()
   {
      Class<?> remote = null;
      Class<?> home = null;
      Class<?> pkClass = Object.class;
      HomeHandleImpl homeHandle = null;
      
      EJBContainer ejbContainer = (EJBContainer)container;
      
      Remote remoteAnnotation = ejbContainer.getAnnotation(Remote.class);
      if (remoteAnnotation != null)
         remote = remoteAnnotation.value()[0];
      RemoteHome homeAnnotation = ejbContainer.getAnnotation(RemoteHome.class);
      if (homeAnnotation != null)
         home = homeAnnotation.value();
      RemoteBinding remoteBindingAnnotation = ejbContainer.getAnnotation(RemoteBinding.class);
      if (remoteBindingAnnotation != null)
         homeHandle = new HomeHandleImpl(remoteBindingAnnotation.jndiBinding());
      
      EJBMetaDataImpl metadata = new EJBMetaDataImpl(remote, home, pkClass, true, false, homeHandle);
      
      return metadata;
   }   
   
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      containerGuid = in.readUTF();
      containerClusterUid = in.readUTF();
      isClustered = in.readBoolean();
      
      if (getContainer() == null)
         throw new EJBException("Invalid (i.e. remote) invocation of local interface (null container) for " + containerGuid);
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(containerGuid);
      out.writeUTF(containerClusterUid);
      out.writeBoolean(isClustered);
   }
   
   /**
    * Ensures that an EJB 2.1 view is complete; the following rules apply:
    * 
    * 1) If EJBHome/EJBLocalHome is defined, at least one EJBObject/EJBLocalObject is defined.  
    * 2) If one EJBObject/EJBLocalObject is defined, an EJBHome/EJBLocalHome is defined.
    * 
    * @param home
    * @param localOrRemoteInterfaces
    * @throws RuntimeException
    */
   protected void ensureEjb21ViewComplete(Class<?> home,Class<?>[] localOrRemoteInterfaces) throws RuntimeException
   {
      // Ensure specified home is EJBHome or EJBLocalHome
      assert (home == null || (EJBHome.class.isAssignableFrom(home) || EJBLocalHome.class.isAssignableFrom(home)));

      // Ensure all interfaces passed are either EJBObject or EJBLocalObject
      for (Class<?> localOrRemoteInterface : localOrRemoteInterfaces)
      {
         assert (EJBObject.class.isAssignableFrom(localOrRemoteInterface) || EJBLocalObject.class
               .isAssignableFrom(localOrRemoteInterface));
      }
      
      // If home is defined and there are no local/remote interfaces
      if (home != null && localOrRemoteInterfaces.length == 0)
      {
         throw new RuntimeException("EJBTHREE-1075: " + container.getBeanClassName() + " defines home"
               + " but provides no local/remote interfaces extending " + EJBLocalObject.class.getName() + "/"
               + EJBObject.class.getName() + "; EJB 2.1 view cannot be realized");
      }

      // If local/remote interfaces are defined, but no remote home
      if (home == null && localOrRemoteInterfaces.length != 0)
      {
         throw new RuntimeException("EJBTHREE-1075: " + container.getBeanClassName()
               + " defines local/remote interfaces" + " but provides no home; EJB 2.1 view cannot be realized");
      }

   }
   
   protected abstract void ensureEjb21ViewComplete();
}
