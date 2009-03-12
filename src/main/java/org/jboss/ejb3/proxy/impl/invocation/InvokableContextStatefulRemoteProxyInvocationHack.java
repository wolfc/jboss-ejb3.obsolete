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
package org.jboss.ejb3.proxy.impl.invocation;

import java.lang.reflect.Method;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.remoting.PojiProxy;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.proxy.impl.remoting.SessionSpecRemotingMetadata;
import org.jboss.ejb3.proxy.impl.remoting.StatefulSessionRemotingMetadata;
import org.jboss.ejb3.proxy.spi.container.InvokableContext;
import org.jboss.ejb3.proxy.spi.intf.SessionProxy;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;

/**
 * InvokableContextStatefulRemoteProxyInvocationHack
 * 
 * Constructs a Proxy to the Container using an underlying
 * StatefulRemoteInvocation when invocations are made.
 * 
 * Looking forward, should be using a more flexible
 * invocation mechanism to handle SFSB, SLSB, etc invocations
 * in an agnostic manner.  This is put into place to avoid
 * further refactoring within EJB3 Core 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 * @deprecated 
 */
@Deprecated
public class InvokableContextStatefulRemoteProxyInvocationHack extends PojiProxy
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(InvokableContextStatefulRemoteProxyInvocationHack.class);

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The target (Session ID)
    */
   private Object target;

   /**
    * The Globally-Unique ID of the target Container
    */
   private String containerGuid;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public InvokableContextStatefulRemoteProxyInvocationHack(Object oid, String containerGuid, InvokerLocator uri,
         Interceptor[] interceptors, Object target)
   {
      // Call Super Implementation
      super(oid, uri, interceptors);

      // Some sanity checks
      assert oid != null : "Specified OID is null";
      assert containerGuid != null : "Specified Container GUID is null";

      // Set additional properties
      this.setTarget(target);
      this.setContainerGuid(containerGuid);
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructs a MethodInvocation from the specified Method and
    * arguments
    * 
    * This implementation uses a StatefulRemoteInvocation as the underlying 
    * Invocation made, in order to support legacy EJB3 Core Containers
    * 
    * Additionally will compute the hash of the SerializableMethod passed
    * to InvokableContext.invoke() for handling by the container
    * 
    * @param method
    * @param args
    * @return
    */
   @Override
   protected MethodInvocation constructMethodInvocation(Method method, Object[] args)
   {

      /*
       * Ensure this method is expected
       */

      // Obtain expected method
      Method expectedInvokeMethod = null;
      try
      {
         expectedInvokeMethod = InvokableContext.class.getDeclaredMethod("invoke", new Class<?>[]
         {SessionProxy.class, SerializableMethod.class, Object[].class});
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException("Could not find expected method \"invoke\" from "
               + InvokableContext.class.getName(), e);
      }

      // Ensure expected method equal to what was passed
      assert method.equals(expectedInvokeMethod) : "Expected method should have been " + expectedInvokeMethod
            + ", but was instead " + method;

      /*
       * Obtain the Method to be dynamically invoked
       */

      // Get the SerializableMethod argument
      Object serializableMethodArg = args[1];
      assert serializableMethodArg instanceof SerializableMethod : "2nd Argument was expected to be of type "
            + SerializableMethod.class.getName() + ", was instead " + serializableMethodArg;
      SerializableMethod serializableMethod = (SerializableMethod) serializableMethodArg;

      // Obtain the Method represented by the SerializableMethod
      Method dynamicInvokeMethod = serializableMethod.toMethod();
      long hash = MethodHashing.calculateHash(dynamicInvokeMethod);

      // Log
      log.debug("Received invocation request to method " + serializableMethod + "; using hash: " + hash);

      /*
       * Build the invocation and return
       * 
       * This is going to be intercepted by the Container's "dynamicInvoke"
       * because the Container itself will be registered w/ Remoting Dispatcher
       * via ProxyTestClassProxyHack
       */
      MethodInvocation sri = new StatefulRemoteInvocation(this.getInterceptors(), hash, dynamicInvokeMethod,
            dynamicInvokeMethod, null, this.getTarget());

      // Manually add metadata for invoked method
      sri.getMetaData().addMetaData(SessionSpecRemotingMetadata.TAG_SESSION_INVOCATION,
            SessionSpecRemotingMetadata.KEY_INVOKED_METHOD, serializableMethod, PayloadKey.AS_IS);

      return sri;
   }

   /**
    * Adds a Session ID, if specified, to the invocation metadata 
    */
   //FIXME Not SFSB/SLSB -agnostic
   @Override
   protected void addMetadataToInvocation(MethodInvocation methodInvocation)
   {
      // Call Super implementation
      super.addMetadataToInvocation(methodInvocation);

      // Obtain target
      Object target = this.getTarget();

      // Add to the Invocation Metadata, if exists
      if (target != null)
      {
         methodInvocation.getMetaData().addMetaData(StatefulSessionRemotingMetadata.TAG_SFSB_INVOCATION,
               StatefulSessionRemotingMetadata.KEY_SESSION_ID, target, PayloadKey.AS_IS);
      }

      // Add Container Name
      //FIXME Hardcoded Strings bad, IsLocalInterceptor should be sharing these references or otherwise accessible somehow
      methodInvocation.getMetaData().addMetaData("IS_LOCAL", "GUID", this.getContainerGuid(), PayloadKey.AS_IS);

   }

   /**
    * Add the target arguments for the invocation itself, not to those
    * initially passed to InvokableContext.invoke()
    */
   @Override
   protected void addArgumentsToInvocation(MethodInvocation invocation, Object[] originalArguments)
   {
      // Get the arguments, perform sanity checks, and set the true arguments to the destination method
      Object objArguments = originalArguments[2];
      assert objArguments instanceof Object[] : "3rd Argument to " + InvokableContext.class.getSimpleName()
            + ".invoke should be an array of Objects";
      Object[] arguments = (Object[]) objArguments;
      invocation.setArguments(arguments);
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected Object getTarget()
   {
      return target;
   }

   private void setTarget(Object target)
   {
      this.target = target;
   }

   protected String getContainerGuid()
   {
      return containerGuid;
   }

   private void setContainerGuid(String containerGuid)
   {
      this.containerGuid = containerGuid;
   }

}
