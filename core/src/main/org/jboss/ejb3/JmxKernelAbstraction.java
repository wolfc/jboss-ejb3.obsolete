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

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class JmxKernelAbstraction
   extends JmxClientKernelAbstraction
   implements KernelAbstraction
{
   private static final Logger log = Logger.getLogger(JmxKernelAbstraction.class);

   private MBeanServer server;
   private ServiceControllerMBean serviceController;
   private DeploymentInfo di;

   public JmxKernelAbstraction(DeploymentInfo di)
   {
      super(di.getServer());
      this.server = di.getServer();
      serviceController = (ServiceControllerMBean) MBeanProxyExt.create(ServiceControllerMBean.class, ServiceControllerMBean.OBJECT_NAME,
            di.getServer());
      this.di = di;
   }
   
   public JmxKernelAbstraction(MBeanServer server)
   {
      super(server);
      serviceController = (ServiceControllerMBean) MBeanProxyExt.create(ServiceControllerMBean.class, ServiceControllerMBean.OBJECT_NAME,
                                                                        server);
   }
   
   public void setMbeanServer(MBeanServer server)
   {
      this.server = server;
   }


   public void install(String name, DependencyPolicy dependencies, Object service)
   {
      if (!(service instanceof ServiceMBeanSupport) && !(service instanceof DynamicMBean))
      {
         log.debug("creating wrapper delegate for: " + service.getClass().getName());
         // create mbean delegate.
         service = new ServiceDelegateWrapper(service);
      }
      JmxDependencyPolicy policy = (JmxDependencyPolicy)dependencies;
      try
      {
         log.info("installing MBean: " + name + " with dependencies:");
         for (Object obj : policy.getDependencies())
         {
            log.info("\t" + obj);
         }
         ObjectName on = new ObjectName(name);
         
         if(policy.getDependencies().contains(on))
            throw new IllegalStateException("circular dependencies detected");
         
         server.registerMBean(service, on);
         addParentDependency(on);

         serviceController.create(on, policy.getDependencies());
         serviceController.start(on);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private void addParentDependency(ObjectName on)
   {
      //di.mbeans.add(on);

      // this is done so that we can get dependency error messages.
      // and this is the only reason this is done.
      // if you don't put add to the top DI mbean list, then no dependency
      // error message is printed out if there is one.
      DeploymentInfo parent = di;
      while (parent.parent != null)
      {
         parent = parent.parent;
      }
      parent.mbeans.add(on);

   }
   
   private void removeParentDependency(ObjectName on)
   {
      DeploymentInfo parent = di;
      while (parent.parent != null)
      {
         parent = parent.parent;
      }
      parent.mbeans.remove(on);
   }

   public void installMBean(ObjectName on, DependencyPolicy dependencies, Object service)
   {
      JmxDependencyPolicy policy = (JmxDependencyPolicy)dependencies;
      try
      {
         server.registerMBean(service, on);
         addParentDependency(on);
         serviceController.create(on, policy.getDependencies());
         serviceController.start(on);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void uninstallMBean(ObjectName on)
   {
      try
      {
         serviceController.stop(on);
         serviceController.destroy(on);
         serviceController.remove(on);
         removeParentDependency(on);
         if(server.isRegistered(on))
            server.unregisterMBean(on);
         else
            log.warn(on + " is not registered");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void uninstall(String name)
   {
      ObjectName on;
      try
      {
         on = new ObjectName(name);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      
      uninstallMBean(on);
   }
}
