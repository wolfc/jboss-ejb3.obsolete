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
package org.jboss.ejb3.remoting.endpoint;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.spi.Remotable;

/**
 * An endpoint that only takes serializable parameters. This is one level higher that a Remotable,
 * because now we know the target implements RemotableEndpoint.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public interface RemotableEndpoint
{
   Method INVOKE_METHOD = MethodHelper.getMethod(RemotableEndpoint.class, "invoke", Serializable.class, Map.class, SerializableMethod.class, Object[].class);
   
   /**
    * The remotable which has this endpoint as target.
    */
   Remotable getRemotable();
   
   /**
    * The invokedBusinessInterface is method.actualClass
    * 
    * @param session
    * @param contextData
    * @param method
    * @param args
    * @return
    * @throws Throwable
    */
   Object invoke(Serializable session, Map<String, Object> contextData, SerializableMethod method, Object args[]) throws Throwable;
}
