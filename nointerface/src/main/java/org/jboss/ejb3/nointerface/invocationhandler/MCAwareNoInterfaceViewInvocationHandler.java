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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.ejb.EJBContainer;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;

/**
 * MCAwareNoInterfaceViewInvocationHandler
 *
 * An {@link InvocationHandler} which corresponds to the
 * no-interface view of a {@link EJBContainer}. All calls on the no-interface
 * view are routed through this {@link InvocationHandler} to the container.
 *
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MCAwareNoInterfaceViewInvocationHandler implements InvocationHandler
{

   /**
    * Equals and hashCode methods are handled within this invocation handler
    */
   private static final Method METHOD_EQUALS;

   private static final Method METHOD_HASH_CODE;

   private static final Method METHOD_TO_STRING;

   static
   {
      try
      {
         METHOD_EQUALS = Object.class.getDeclaredMethod("equals", Object.class);
         METHOD_HASH_CODE = Object.class.getDeclaredMethod("hashCode");
         METHOD_TO_STRING = Object.class.getDeclaredMethod("toString");
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }

   }

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(MCAwareNoInterfaceViewInvocationHandler.class);

   /**
    * The KernelControllerContext corresponding to the endpoint for which
    * the no-interface view is to be created by this factory. This context
    * may <i>not</i> be in INSTALLED state. This factory is responsible
    * for pushing it to INSTALLED state whenever necessary. 
    * 
    * All calls to this invocation handler will be forwarded to the container represented
    * by this context
    * 
    *
    */
   private KernelControllerContext endpointContext;

   /**
    * The session used to interact with the {@link Endpoint}
    */
   private Serializable session;

   /**
    * Constructor
    * @param container
    */
   public MCAwareNoInterfaceViewInvocationHandler(KernelControllerContext endpointContext, Serializable session)
   {
      assert endpointContext != null : "Endpoint context is null for no-interface view invocation handler";
      this.endpointContext = endpointContext;
      this.session = session;
   }

   /**
    * The entry point when a client calls any methods on the no-interface view of a bean,
    * returned through JNDI.
    *
    *
    * @param proxy
    * @param method The invoked method
    * @param args The arguments to the method
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      // handle equals() and hashCode() in this InvocationHandler
      try
      {
         return handleDirectly(proxy, method, args);
      }
      catch (CannotHandleDirectlyException chde)
      {
         //ignore
         if (logger.isTraceEnabled())
         {
            logger.trace("Cannot handle method: " + method.getName() + " in " + this.getClass().getName());
         }
      }
      // get the endpoint (which will involve pushing it to INSTALLED state)
      Endpoint endpoint = getInstalledEndpoint();
      assert endpoint != null : "No endpoint associated with context " + this.endpointContext
            + " - cannot invoke the method on bean";

      // finally pass-on the control to the endpoint
      return endpoint.invoke(this.session, null, method, args);
   }

   /**
    * Returns the context corresponding to the container, associated with this invocation handler
    *
    * @return
    */
   public KernelControllerContext getContainerContext()
   {
      return this.endpointContext;
   }

   /**
    * Returns the {@link Endpoint} container corresponding to this 
    * {@link MCAwareNoInterfaceViewInvocationHandler}. Internally, the {@link Endpoint} 
    * will be first pushed to the INSTALLED state 
    * 
    * @return
    */
   public Endpoint getInstalledEndpoint()
   {
      if (logger.isTraceEnabled())
      {
         logger.trace("Pushing the endpoint context to INSTALLED state from its current state = "
               + this.endpointContext.getState().getStateString());
      }
      try
      {
         // first push the context corresponding to the endpoint to INSTALLED
         this.endpointContext.getController().change(this.endpointContext, ControllerState.INSTALLED);
         // get hold of the endpoint from its context
         Endpoint endpoint = (Endpoint) this.endpointContext.getTarget();
         return endpoint;
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error getting endpoint out of container KernelControllerContext "
               + this.endpointContext, t);
      }

   }

   /**
    * Returns the {@link Endpoint} container corresponding to this 
    * {@link MCAwareNoInterfaceViewInvocationHandler}. Note that this method does NOT
    * change the state of the KernelControllerContext of this Endpoint. As such,
    * the Endpoint returned by this method is NOT guaranteed to be in INSTALLED state.
    * If the Endpoint with INSTALLED state is required, then use the {@link #getInstalledEndpoint()}
    * method. 
    *  
    * @return
    * @see #getInstalledEndpoint()
    */
   private Endpoint getEndpoint()
   {
      Object endpoint = this.endpointContext.getTarget();
      assert endpoint instanceof Endpoint : "Unexpected type " + endpoint.getClass().getName() + " found in context "
            + this.endpointContext + " Expected " + Endpoint.class.getName();
      return (Endpoint) endpoint;
   }

   /**
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(Object other)
   {
      // simple object comparison
      if (this == other)
      {
         return true;
      }

      // equals() method contract specifies that if the other object is null
      // then equals() should return false
      if (other == null)
      {
         return false;
      }

      // If the other object is not an instance of MCAwareNoInterfaceViewInvocationHandler
      // then they are not equal
      if (!(other instanceof MCAwareNoInterfaceViewInvocationHandler))
      {
         return false;
      }

      MCAwareNoInterfaceViewInvocationHandler otherNoInterfaceViewInvocationHandler = (MCAwareNoInterfaceViewInvocationHandler) other;

      // First check whether the Endpoints of both these InvocationHandlers are equal. If 
      // not, then no need for any further comparison, just return false
      if (!(this.getInstalledEndpoint().equals(otherNoInterfaceViewInvocationHandler.getInstalledEndpoint())))
      {
         return false;
      }

      // If the endpoints are equal, then let's next check whether the sessions for
      // these invocation handlers are equal. If not, return false.
      if (!(this.session.equals(otherNoInterfaceViewInvocationHandler.session)))
      {
         return false;
      }
      // All possible, inequality conditions have been tested, so return true.
      return true;
   }

   /**
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      int hashCode = this.getInstalledEndpoint().hashCode();
      if (this.session != null)
      {
         hashCode += this.session.hashCode();
      }
      return hashCode;
   }

   /**
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("No-Interface view for endpoint [ " + this.getEndpoint() + " ]");
      if (this.session != null)
      {
         sb.append(" and session " + this.session);
      }
      return sb.toString();
   }

   /**
    * Handles {@link Object#equals(Object)} and {@link Object#hashCode()} method invocations on the 
    * proxy.
    * 
    * @param proxy
    * @param method The method that was invoked on the no-interface view
    * @param args The arguments to the method
    * @return  
    * @throws CannotHandleDirectlyException If the <code>method</code> is neither {@link Object#equals(Object) nor Object#hashCode()
    */
   private Object handleDirectly(Object proxy, Method method, Object[] args) throws CannotHandleDirectlyException
   {

      // if equals()
      if (method.equals(METHOD_EQUALS))
      {
         Object other = args[0];
         // if the other object is null, then it's a false straight-away as per the
         // contract of equals method
         if (other == null)
         {
            return false;
         }
         // simple instance comparison
         if (this == other)
         {
            return true;
         }

         // This is the important one (and a good one) - thanks to Carlo
         // When the equals is called on the no-interface view - view1.equals(view2)
         // we somehow have to get the invocation handler (which hold the session information) for view2.
         // So the trick here is to first check whether the other is an instance of 
         // MCAwareNoInterfaceViewInvocationHandler. If not, then invoke the equals on that object.
         //  - If the "other" happens to be an no-interface view, the call will be redirected
         // to the invocation handler of view2 and thus we have the session information that we needed
         // from view2.
         //  - If the "other" happens to be an instance of some other class, then that class' equals
         // would return false since its not an instance of MCAwareNoInterfaceViewInvocationHandler.
         if (!(other instanceof MCAwareNoInterfaceViewInvocationHandler))
         {
            return other.equals(this);
         }

         return this.equals(other);
      }
      // handle hashCode
      else if (method.equals(METHOD_HASH_CODE))
      {
         return this.hashCode();
      }
      else if (method.equals(METHOD_TO_STRING))
      {
         return this.toString();
      }
      throw new CannotHandleDirectlyException();
   }

   /**
    * 
    * CannotHandleDirectlyException
    * 
    * Will be used to indicate that this {@link MCAwareNoInterfaceViewInvocationHandler} cannot 
    * handle the method invocation in the invocation handler. 
    *
    * @author Jaikiran Pai
    * @version $Revision: $
    */
   private class CannotHandleDirectlyException extends Exception
   {
   }

}
