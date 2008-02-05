/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.sandbox.mc;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.plugins.bootstrap.standalone.StandaloneBootstrap;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MC
{
   private static final Logger log = Logger.getLogger(MC.class);
   
   private StandaloneBootstrap bootstrap;
   
   public MC()
   {
      this(null);
   }
   
   public MC(String args[])
   {
      try
      {
         bootstrap = new StandaloneBootstrap(args);
         bootstrap.run();
      }
      catch(Exception e)
      {
         // this actually never happens
         throw new RuntimeException(e);
      }
   }
   
   @Deprecated
   public KernelController getController()
   {
      return bootstrap.getKernel().getController();
   }
   
   public void install(String name, Class<?> beanClass) throws Throwable
   {
      AbstractBeanMetaData metaData = new AbstractBeanMetaData(name, beanClass.getName());
      metaData.setMode(ControllerMode.ON_DEMAND);
      KernelControllerContext context = getController().install(metaData);
      if(context.getError() != null)
         throw context.getError();
   }
   
   public <T> T lookup(String name, Class<T> expectedType) throws Throwable
   {
      KernelController controller = getController();
      ControllerContext context = controller.getContext(name, null);
      controller.change(context, ControllerState.INSTALLED);
      if(context.getError() != null)
         throw context.getError();
      
      if(context.getState() != ControllerState.INSTALLED) {
         System.err.println(context.getDependencyInfo().getUnresolvedDependencies());
      }
      // TODO: it can be stalled because of dependencies
      assert context.getState() == ControllerState.INSTALLED;
      
      return expectedType.cast(context.getTarget());
   }
}
