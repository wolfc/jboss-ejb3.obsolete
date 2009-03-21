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
package org.jboss.ejb3.remoting2.test.common;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.remoting.endpoint.RemotableEndpoint;
import org.jboss.ejb3.remoting.spi.Remotable;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RemoteKernelControllerImpl implements RemoteKernelController, Remotable, RemotableEndpoint
{
   private static final Logger log = Logger.getLogger(RemoteKernelControllerImpl.class);
   
   // make sure ProjectData is a loaded class in the server
   static {
      String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
      try
      {
         Class.forName(className);
         log.info("Loaded Cobertura");
      }
      catch(ClassNotFoundException e)
      {
         log.info("Could not find Cobertura");
      }
   }
   
   private Kernel kernel;

   public void dumpCobertura()
   {
      try
      {
         String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
         String methodName = "saveGlobalProjectData";
         Class<?> saveClass = Class.forName(className);
         java.lang.reflect.Method saveMethod = saveClass.getDeclaredMethod(methodName, new Class[0]);
         saveMethod.invoke(null, new Object[0]);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }
   
   public String install(BeanMetaData beanMetaData) throws Throwable
   {
      KernelControllerContext context = kernel.getController().install(beanMetaData);
      return (String) context.getName();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.remoting.spi.Remotable#getClassLoader()
    */
   public ClassLoader getClassLoader()
   {
      return RemoteKernelControllerImpl.class.getClassLoader();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.remoting.spi.Remotable#getId()
    */
   public Serializable getId()
   {
      return RemoteKernelController.class.getName();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.remoting.spi.Remotable#getTarget()
    */
   public Object getTarget()
   {
      return this;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.remoting.endpoint.RemotableEndpoint#invoke(java.io.Serializable, java.util.Map, org.jboss.ejb3.common.lang.SerializableMethod, java.lang.Object[])
    */
   public Object invoke(Serializable session, Map<String, Object> contextData, SerializableMethod method, Object[] args)
      throws Throwable
   {
      Method realMethod = method.toMethod(getClassLoader());
      try
      {
         return realMethod.invoke(this, args);
      }
      catch(InvocationTargetException e)
      {
         throw e.getCause();
      }
   }

   @Inject(bean="jboss.kernel:service=Kernel")
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }
}
