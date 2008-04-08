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
package org.jboss.ejb3.stateful;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.aop.metadata.SimpleMetaData;
import org.jboss.ejb3.Ejb3Registry;
import org.jboss.ejb3.session.SessionSpecContainer;

/**
 * Overrides superclass to not use MarshalledValue in externalization,
 * as a nested context is meant to be serialized as part of its parent
 * context and to share with it object references to any XPC or managed 
 * entities.  Serializing with a MarshalledValue would result in separate
 * deserializations of the XPCs and managed entities. 
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class NestedStatefulBeanContext extends StatefulBeanContext implements Externalizable
{   
   /** The serialVersionUID */
   private static final long serialVersionUID = 7835719320529968045L;
   

   public NestedStatefulBeanContext(SessionSpecContainer container, Object bean)
   {
      super(container, bean);
   }
   
   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeUTF(Ejb3Registry.clusterUid(getContainer()));
      out.writeUTF(Ejb3Registry.guid(getContainer()));
      out.writeObject(id);
      out.writeBoolean(isClustered);
      out.writeObject(metadata);
      out.writeObject(bean);
      out.writeObject(persistenceContexts);
      out.writeObject(interceptorInstances);
      out.writeObject(contains);
      // Cannot write a ref to our parent as that seems to blow up serialization
      //out.writeObject(containedIn);
      out.writeBoolean(removed);
      out.writeBoolean(replicationIsPassivation);
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      containerClusterUid = in.readUTF();
      containerGuid = in.readUTF();
      id = in.readObject();
      isClustered = in.readBoolean();
      metadata = (SimpleMetaData) in.readObject();
      bean = in.readObject();
      persistenceContexts = (HashMap<String, EntityManager>)  in.readObject();
      interceptorInstances = (HashMap<Class, Object>)in.readObject();
      contains = (List<StatefulBeanContext>) in.readObject();
      removed = in.readBoolean();
      replicationIsPassivation = in.readBoolean();
      
      // Since we can't write a ref to our parent, our children also
      // don't have a ref to use.  So reestablish it.
      if (contains != null)
      {
         for (StatefulBeanContext contained : contains)
         {
            contained.containedIn = this;
         }
      }
      
      // If we've just been deserialized, we are passivated
      passivated = true;
   }

}
