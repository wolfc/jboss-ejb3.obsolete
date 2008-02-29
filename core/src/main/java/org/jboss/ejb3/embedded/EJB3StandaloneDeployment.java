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
package org.jboss.ejb3.embedded;

import javax.management.MBeanServer;
import javax.security.jacc.PolicyConfiguration;

import org.jboss.ejb3.DependencyPolicy;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.MCDependencyPolicy;
import org.jboss.ejb3.MCKernelAbstraction;
import org.jboss.ejb3.javaee.JavaEEApplication;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.ejb3.security.JaccHelper;
import org.jboss.kernel.Kernel;

/**
 * Use this class when you want to manually specify classes you want deployed.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Deprecated
public class EJB3StandaloneDeployment extends Ejb3Deployment
{
   public EJB3StandaloneDeployment(DeploymentUnit di, Kernel kernel, MBeanServer mbeanServer)
   {
      super(di, null, null, null);
   
      defaultSFSBDomain = "Embedded Stateful Bean";
      kernelAbstraction = new MCKernelAbstraction(kernel, mbeanServer);
      this.mbeanServer = mbeanServer;
   }
   
   public void setMbeanServer(MBeanServer mbeanServer)
   {
      super.setMbeanServer(mbeanServer);
      
      kernelAbstraction.setMbeanServer(mbeanServer);
   }

   protected void putJaccInService(PolicyConfiguration pc, DeploymentUnit unit)
   {
      /*
      try
      {
         pc.commit();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }\
      */
   }

   protected PolicyConfiguration createPolicyConfiguration() throws Exception
   {
      return JaccHelper.initialiseJacc(getJaccContextId());
   }

/*
   protected Map getDefaultPersistenceProperties()
   {
      try
      {
         Properties hb = new Properties();
         InputStream hbstream = di.getResourceLoader().getResourceAsStream("default.persistence.properties");
         hb.load(hbstream);
         hbstream.close();
         return hb;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }


   @Override
   protected void registerContainer(Container container) throws Exception
   {
      super.registerContainer(container);
      container.create();
   }

   @Override
   public void start() throws Exception
   {
      try
      {
      super.start();
      ArrayList<Container> serviceBeans = new ArrayList<Container>();
      Iterator it = containers.keySet().iterator();
      while (it.hasNext())
      {
         ObjectName on = (ObjectName) it.next();
         Container con = (Container) containers.get(on);
         if (con instanceof ServiceManager || con instanceof ServiceContainer)
         {
            serviceBeans.add(con);
         }
         else
         {
            con.start();
         }
      }
      for (Container con : serviceBeans)
      {
         con.start();
      }
      } catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }


   @Override
   public void stop() throws Exception
   {
      Iterator it = containers.keySet().iterator();
      while (it.hasNext())
      {
         ObjectName on = (ObjectName) it.next();
         Container con = (Container) containers.get(on);
         con.stop();
      }
      super.stop();
   }

   @Override
   public void destroy() throws Exception
   {
      super.destroy();

      Iterator it = containers.keySet().iterator();
      while (it.hasNext())
      {
         ObjectName on = (ObjectName) it.next();
         Container con = (Container) containers.get(on);
         con.destroy();
      }
   }

   protected void startEntityManagerDeployment()
   {
      if (entityDeployment != null)
      {
         try
         {
            entityDeployment.start();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   protected void stopEntityManagerDeployment()
   {
      if (entityDeployment != null)
      {
         try
         {
            entityDeployment.stop();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }

*/

   public DependencyPolicy createDependencyPolicy(JavaEEComponent component)
   {
      return new MCDependencyPolicy(component);
   }
   
//   @Override
//   public void start() throws Exception
//   {
//      System.err.println("EJB3StandaloneDeployment.start");
//      super.start();
//      for(Object o : ejbContainers.values())
//      {
//         Container con = (Container) o;
//         if(con instanceof ServiceContainer)
//            con.start();
//      }
//   }
   
   public JavaEEApplication getApplication()
   {
      return null;
   }
}
