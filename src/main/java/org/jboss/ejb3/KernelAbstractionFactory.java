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
package org.jboss.ejb3;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;

import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision: 46471 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class KernelAbstractionFactory
{
   private static final Logger log = Logger.getLogger(KernelAbstractionFactory.class);
   
   private static ClientKernelAbstraction clientKernelAbstraction = null;
   private static KernelAbstraction kernelAbstraction = null;
   private static Kernel kernel = null;
  
   public static KernelAbstraction getInstance() throws Exception
   {  
      if (kernelAbstraction == null)
      {
         MBeanServer mbeanServer = (MBeanServer)getMBeanServer();
         if (kernel != null)
            kernelAbstraction = new MCKernelAbstraction(kernel, mbeanServer);
         else
         {
            kernelAbstraction = new JmxKernelAbstraction(mbeanServer);
         }
      }
     
     return kernelAbstraction;
   }
   
   public static ClientKernelAbstraction getClientInstance() throws Exception
   {
      if (clientKernelAbstraction == null)
      {
         if (kernel != null)
         {
            clientKernelAbstraction = new MCClientKernelAbstraction(kernel);
         }
         else
         {
            MBeanServerConnection mbeanServer = (MBeanServerConnection)getMBeanServer();
            clientKernelAbstraction = new JmxClientKernelAbstraction(mbeanServer);
         }
      }
     
     return clientKernelAbstraction;
   }
  
   public static void setKernel(Kernel k)
   {
      kernel = k;
      kernelAbstraction = null;
      clientKernelAbstraction = null;
   }
   
   protected static MBeanServerConnection getMBeanServer() throws Exception
   {
      MBeanServerConnection mbeanServer;
         
      try
      {
         mbeanServer = org.jboss.mx.util.MBeanServerLocator.locateJBoss();
      }
      catch (IllegalStateException e)
      {
         String adaptorName = System.getProperty("jbosstest.server.name", "jmx/invoker/RMIAdaptor");
         mbeanServer = (MBeanServerConnection)InitialContextFactory.getInitialContext().lookup(adaptorName);
      }
      
      return mbeanServer;
   }
}

