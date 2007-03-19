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

import java.util.Map;
import javax.management.ObjectName;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;

/**
 * An EjbModule represents a collection of beans that are deployed as a unit.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Ejb3Module extends ServiceMBeanSupport implements Ejb3ModuleMBean
{
   public final static String BASE_EJB3_JMX_NAME = "jboss.j2ee:service=EJB3";
   private static final Logger log = Logger.getLogger(Ejb3Module.class);

   private Ejb3JmxDeployment deployment;
   private DeploymentInfo di;

    public Ejb3Module(DeploymentInfo di)
    {
       DeploymentScope deploymentScope = null;
       if (di.parent != null)
       {
          if (di.parent.shortName.endsWith(".ear") || di.parent.shortName.endsWith(".ear/"))
          {
             synchronized(di.parent.context)
             {
                deploymentScope = (DeploymentScope)di.parent.context.get("EJB3_EAR_METADATA");
                if (deploymentScope == null)
                {
                   deploymentScope = new JmxDeploymentScopeImpl(di.parent.shortName);
                   di.parent.context.put("EJB3_EAR_METADATA", deploymentScope);
                }
             }
          }
       }
       deployment = new Ejb3JmxDeployment(di, deploymentScope);
       if (deploymentScope != null)
       {
          deploymentScope.register(deployment);
       }
       this.di = di;
    }

   protected void createService() throws Exception
   {
      super.createService();
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(di.ucl);
         deployment.create();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
   }

   protected void startService() throws Exception
   {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(di.ucl);
         deployment.start();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
      super.startService();

   }

   protected void stopService() throws Exception
   {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(di.ucl);
         deployment.stop();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
      super.stopService();
   }

   protected void destroyService() throws Exception
   {
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(di.ucl);
         deployment.destroy();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(old);
      }
      super.destroyService();
   }

   public Container getContainer(ObjectName name)
   {
      return deployment.getContainer(name);
   }

   public Map getContainers()
   {
      return deployment.getEjbContainers();
   }
}
