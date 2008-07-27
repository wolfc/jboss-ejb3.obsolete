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
package org.jboss.ejb3.proxy.remoting;

import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.remoting.InvokerLocator;

/**
 * InvokableContextProxy
 * 
 * An EJB3-specific implementation of the Remoting PojiProxy
 * for an InvokableContext, responsible for rewriting the target method
 * "invoke" with that specified as the method to invoke
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class InvokableContextProxy extends PojiProxy
{
   // ------------------------------------------------------------------------------||
   // Class Members  ---------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   // ------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   // ------------------------------------------------------------------------------||

   public InvokableContextProxy(Object oid, InvokerLocator uri, Interceptor[] interceptors)
   {
      super(oid, uri, interceptors);
   }

   // ------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------||
   // ------------------------------------------------------------------------------||

   //TODO Pending Release of jboss-remoting-aspects, stalled by failed build
//   /**
//    * Adds EJB3-specific metadata to the invocation before it's made
//    */
//   @Override
//   protected void addMetadataToInvocation(MethodInvocation methodInvocation)
//   {
//      // Call Super
//      super.addMetadataToInvocation(methodInvocation);
//
//      // Add metadata for the IsLocalProxyFactoryInterceptor
//      //TODO
//
//   }

   //TODO Pending Release of jboss-remoting-aspects, stalled by failed build
//   /**
//    * Proceed with the invocation upon InvokableContext.invoke(), 
//    * setting the target method
//    */
//   @Override
//   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
//   {
//      MethodInvocation sri = this.constructMethodInvocation(method, args);
//      sri.setArguments(args);
//      this.addMetadataToInvocation(sri);
//      return sri.invokeNext();
//   }

   //TODO Pending Release of jboss-remoting-aspects, stalled by failed build
//   /**
//    * Constructs a MethodInvocation from the specified Method and
//    * arguments
//    * 
//    * @param method
//    * @param args
//    * @return
//    */
//   /*
//    * Hack Smell, this would not be necessary if the Container could 
//    * resolve InvokableContext.invoke()'s methodHash from the advisor.getMethodInfo(hash).
//    * 
//    * Code review requested by ALR
//    */
//   @Override
//   protected MethodInvocation constructMethodInvocation(Method method, Object[] args)
//   {
//      // Some sanity checks
//      assert args.length == 3; // InvokableContext.invoke(Object proxy, SerializableMethod method, Object[] args)
//      Object targetMethodArg = args[1];
//      assert targetMethodArg instanceof SerializableMethod : "Second argument expected to be "
//            + SerializableMethod.class.getName();
//      assert targetMethodArg != null : "Target method may not be null";
//
//      // Get the target method      
//      SerializableMethod targetMethod = (SerializableMethod) targetMethodArg;
//      Method realizedMethod = targetMethod.toMethod();
//
//      // Create and invoke upon a MethodInvocation
//      long hash = MethodHashing.calculateHash(realizedMethod);
//      MethodInvocation sri = new MethodInvocation(this.getInterceptors(), hash, realizedMethod, realizedMethod, null);
//
//      // Return
//      return sri;
//   }

}
