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
package org.jboss.ejb3.proxy.impl.invocation;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 62769 $
 * @deprecated To be replaced by another invocation mechanism which provides way for
 * Stateless/Stateful Invocations - probably descended from ContainerMethodInvocation in
 * EJB3 Interceptors
 */
@Deprecated
public class StatefulRemoteInvocation extends MethodInvocation implements java.io.Externalizable
{
   private static final long serialVersionUID = 523913901046490941L;
   
   protected Object id;


   public StatefulRemoteInvocation(Interceptor[] interceptors, long methodHash, Method advisedMethod, Method unadvisedMethod, Advisor advisor, Object id)
   {
      super(interceptors, methodHash, advisedMethod, unadvisedMethod, advisor);
      this.id = id;
   }

   public StatefulRemoteInvocation()
   {
   }

   public Object getId()
   {
      return id;
   }

   public Invocation getWrapper(Interceptor[] newchain)
   {
      throw new RuntimeException("NOT IMPLEMENTED");
   }

   public Invocation copy()
   {
      throw new RuntimeException("NOT IMPLEMENTED");
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      super.writeExternal(out);
      out.writeObject(id);
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      id = in.readObject();
   }

   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("[");
      sb.append("id=").append(id);
      sb.append(", MethodInvocation=").append(super.toString());
      sb.append("]");
      return sb.toString();
   }

}
