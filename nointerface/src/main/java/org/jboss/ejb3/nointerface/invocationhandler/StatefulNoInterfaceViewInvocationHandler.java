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
package org.jboss.ejb3.nointerface.invocationhandler;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.container.InvokableContext;
import org.jboss.logging.Logger;

/**
 * StatefulNoInterfaceViewInvocationHandler
 *
 * Responsible for handling invocations to a stateful bean through the no-interface
 * view of the bean
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class StatefulNoInterfaceViewInvocationHandler extends NoInterfaceViewInvocationHandler
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(StatefulNoInterfaceViewInvocationHandler.class);

   /**
    * A stateful bean is invoked within a session. This represents the
    * session id.
    */
   private Serializable sessionId;

   /**
    * Constructor
    *
    * @param container
    */
   public StatefulNoInterfaceViewInvocationHandler(InvokableContext container)
   {
      super(container);
   }

   /**
    * Associates a session with this invocation handler
    *
    * @param session
    */
   public void setSessionId(Serializable session)
   {
      this.sessionId = session;
   }

   /**
    * Returns the session id corresponds to this invocation handler
    * @return
    */
   public Serializable getSessionId()
   {
      return this.sessionId;
   }

   /**
    * This is where the invocation to the container takes place.
    */
   @Override
   public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable
   {

      assert this.container != null : "Container not yet available to the no-interface view invocation handler";

      SerializableMethod serializableMethod = new SerializableMethod(method);
      if (logger.isTraceEnabled())
      {
         logger.trace("Invoking container method " + serializableMethod + " for session " + this.getSessionId());
      }

      return this.container.invoke(this.getSessionId(), serializableMethod, args);

   }

}
