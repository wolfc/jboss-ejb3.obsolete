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
package org.jboss.ejb3.test.proxy.remoteaccess;

import javax.management.MBeanServer;

import org.jboss.ejb3.test.proxy.remoteaccess.MockServer.MockServerRequest;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;

/**
 * MockServerInvocationHandler
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MockServerInvocationHandler implements ServerInvocationHandler
{

   /**
    * Instance of logger
    */
   private static Logger logger = Logger.getLogger(MockServerInvocationHandler.class);

   /**
    * Instance of {@link MockServer} to which the requests will be
    * forwarded
    */
   private MockServer mockServer;

   /**
    * Constructor
    * 
    * @param mockServer  
    */
   public MockServerInvocationHandler(MockServer mockServer)
   {
      this.mockServer = mockServer;
   }

   /**
    * @see org.jboss.remoting.ServerInvocationHandler#addListener(org.jboss.remoting.callback.InvokerCallbackHandler)
    */
   public void addListener(InvokerCallbackHandler callbackHandler)
   {
      // no asynchronous support required as of now. Implement later if required

   }

   /**
    * On receiving a {@link MockServerRequest} the invocation handler will
    * carry out appropriate operation on the {@link MockServer} <br>
    * 
    * Supported requests are <br/>
    * <li>
    *   <ul>
    *   {@link MockServerRequest.START} - On receiving this request, the invocation 
    *   handler will start the {@link MockServer}
    *   </ul>
    *   <ul>
    *   {@link MockServerRequest.STOP} - On receiving this request, the invocation 
    *   handler will stop the {@link MockServer}
    *   </ul>
    * </li>
    * @throws {@link IllegalArgumentException} If the <code>invocationRequest</code>
    *           is not supported
    * @see org.jboss.remoting.ServerInvocationHandler#invoke(org.jboss.remoting.InvocationRequest)
    */
   public Object invoke(InvocationRequest invocationRequest) throws Throwable
   {

      if (!(invocationRequest.getParameter() instanceof MockServerRequest))
      {
         throw new IllegalArgumentException("Unrecognized request type " + invocationRequest.getParameter());
      }
      MockServerRequest request = (MockServerRequest) invocationRequest.getParameter();
      logger.info("Received request: " + request);

      // The same invocation handler can be called by multiple threads.
      synchronized (this.mockServer)
      {
         if (request.equals(MockServerRequest.START))
         {
            this.mockServer.start();
         }
         else if (request.equals(MockServerRequest.STOP))
         {
            this.mockServer.stop();
            
         }
         else
         {
            throw new IllegalArgumentException("Unrecognized request " + invocationRequest.getParameter());
         }
         return mockServer.getStatus();
      }

      
   }

   /**
    * @see org.jboss.remoting.ServerInvocationHandler#removeListener(org.jboss.remoting.callback.InvokerCallbackHandler)
    */
   public void removeListener(InvokerCallbackHandler callbackHandler)
   {
      // do nothing - Implement later if needed

   }

   /**
    * @see org.jboss.remoting.ServerInvocationHandler#setInvoker(org.jboss.remoting.ServerInvoker)
    */
   public void setInvoker(ServerInvoker invoker)
   {
      // do nothing - Implement later if needed

   }

   /**
    * @see org.jboss.remoting.ServerInvocationHandler#setMBeanServer(javax.management.MBeanServer)
    */
   public void setMBeanServer(MBeanServer server)
   {
      // do nothing - Implement later if needed

   }

}
