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

import java.lang.reflect.Method;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.plugins.AbstractConstructorMetaData;
import org.jboss.beans.metadata.plugins.AbstractDemandMetaData;
import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.SupplyMetaData;
import org.jboss.ejb3.embedded.resource.RARDeployment;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.logging.Logger;

/**
 * Abstraction layer for installing beans into the micro container.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class MCKernelAbstraction
   extends MCClientKernelAbstraction
   implements KernelAbstraction
{
   private static final Logger log = Logger.getLogger(MCKernelAbstraction.class);

   public static class AlreadyInstantiated extends AbstractConstructorMetaData
   {
      private static final long serialVersionUID = 8120833830553872619L;
      
      private Object bean;

      public class Factory
      {

         public Object create()
         {
            return bean;
         }
      }

      public AlreadyInstantiated(Object bean)
      {
         this.bean = bean;
         this.setFactory(new AbstractValueMetaData(new Factory()));
         this.setFactoryClass(Factory.class.getName());
         this.setFactoryMethod("create");
      }
   }
   
   private MBeanServer server;

   public MCKernelAbstraction(Kernel kernel, MBeanServer server)
   {
      super(kernel);
      this.server = server;
   }
   
   public void setMbeanServer(MBeanServer server)
   {
      this.server = server;
   }

   private boolean hasOperation(MBeanInfo info, String operationName)
   {
      for(MBeanOperationInfo operationInfo : info.getOperations())
      {
         if(operationInfo.getName().equals(operationName) == false)
            continue;
         
         // void return type
         if(operationInfo.getReturnType().equals("void") == false)
            continue;
         
         // no parameters
         if(operationInfo.getSignature().length != 0)
            continue;
         
         return true;
      }
      
      return false;
   }
   
   public void install(String name, DependencyPolicy dependencies, Object service)
   {
      AbstractBeanMetaData bean = new AbstractBeanMetaData(name, service.getClass().getName());
      bean.setConstructor(new AlreadyInstantiated(service));
      MCDependencyPolicy policy = (MCDependencyPolicy) dependencies;
      bean.setDepends(policy.getDependencies());
      bean.setDemands(policy.getDemands());
      bean.setSupplies(policy.getSupplies());
      log.info("installing bean: " + name);
      log.info("  with dependencies:");
      for (Object obj : policy.getDependencies())
      {
         Object msgObject = obj;
         if (obj instanceof AbstractDemandMetaData)
         {
            msgObject = ((AbstractDemandMetaData)obj).getDemand();
         }
         log.info("\t" + msgObject);
      }
      log.info("  and demands:");
      for(DemandMetaData dmd : policy.getDemands())
      {
         log.info("\t" + dmd.getDemand());
      }
      log.info("  and supplies:");
      for(SupplyMetaData smd : policy.getSupplies())
      {
         log.info("\t" + smd.getSupply());
      }
      try
      {
         try 
         {
            kernel.getController().uninstall(name);
         }
         catch (IllegalStateException e){}
               
         kernel.getController().install(bean);
      }
      catch (Throwable throwable)
      {
         throw new RuntimeException(throwable);
      }
   }

   public void installMBean(ObjectName on, DependencyPolicy dependencies, Object service)
   {
      try
      {
         server.registerMBean(service, on);
         install(on.getCanonicalName(), dependencies, service);
         
         // EJBTHREE-606: emulate the ServiceController calls
         MBeanInfo info = server.getMBeanInfo(on); // redundant call for speed
         invokeOptionalMethod(on, info, "create");
         invokeOptionalMethod(on, info, "start");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   private void invokeOptionalMethod(ObjectName on, MBeanInfo info, String operationName) throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      Object params[] = { };
      String signature[] = { };
      if(hasOperation(info, operationName))
         server.invoke(on, operationName, params, signature);
   }
   
   public void uninstallMBean(ObjectName on)
   {
      try
      {
         // EJBTHREE-606: emulate the ServiceController calls
         MBeanInfo info = server.getMBeanInfo(on); // redundant call for speed
         try
         {
            invokeOptionalMethod(on, info, "stop");
         }
         catch(Exception e)
         {
            // TODO: invalidate bean?
            log.warn("stop on " + on + " failed", e);
         }
         try
         {
            invokeOptionalMethod(on, info, "destroy");
         }
         catch(Exception e)
         {
            // TODO: invalidate bean?
            log.warn("destroy on " + on + " failed", e);
         }
         
         server.unregisterMBean(on);
         kernel.getController().uninstall(on.getCanonicalName());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void uninstall(String name)
   {
      try
      {
         log.info("uninstalling bean: " + name);
         kernel.getController().uninstall(name);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public Object invoke(ObjectName objectName, String operationName, Object[] params, String[] signature) throws Exception
   {
      String name = objectName.getCanonicalName();
      KernelRegistryEntry entry = kernel.getRegistry().getEntry(name);
      if (entry != null)
      {
         Object target = entry.getTarget();
         if (target instanceof RARDeployment)
         {
            RARDeployment deployment = (RARDeployment)target;
            return deployment.invoke(operationName, params, signature);
         } 
         else
         {
            Class[] types = new Class[signature.length];
            for (int i = 0; i < signature.length; ++i)
            {
               types[i] = Thread.currentThread().getContextClassLoader().loadClass(signature[i]);
            }
            Method method = target.getClass().getMethod(operationName, types);
            return method.invoke(target, params);
         }
      }
      return null;
   }
}
