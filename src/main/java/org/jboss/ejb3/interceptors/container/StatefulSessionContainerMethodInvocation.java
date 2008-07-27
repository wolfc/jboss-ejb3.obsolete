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
package org.jboss.ejb3.interceptors.container;

import java.lang.reflect.Method;

import org.jboss.aop.Advisor;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.advice.Interceptor;
import org.jboss.ejb3.common.lang.SerializableMethod;

/**
 * StatefulSessionContainerMethodInvocation
 * 
 * An invocation on an EJB3 SFSB Container
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class StatefulSessionContainerMethodInvocation extends ContainerMethodInvocation
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private Object sessionId;

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /*
    * These all delegate to the superclass implementation
    * and are here for visibility alone
    */
   
   public StatefulSessionContainerMethodInvocation(MethodInfo info)
   {
      super(info);
   }

   public StatefulSessionContainerMethodInvocation(Interceptor[] interceptors, long methodHash, Method advisedMethod,
         Method unadvisedMethod, Advisor advisor)
   {
      super(interceptors, methodHash, advisedMethod, unadvisedMethod, advisor);
   }

   public StatefulSessionContainerMethodInvocation(Interceptor[] newchain)
   {
      super(newchain);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public Object getSessionId()
   {
      return sessionId;
   }

   public void setSessionId(Object sessionId)
   {
      this.sessionId = sessionId;
   }

}
