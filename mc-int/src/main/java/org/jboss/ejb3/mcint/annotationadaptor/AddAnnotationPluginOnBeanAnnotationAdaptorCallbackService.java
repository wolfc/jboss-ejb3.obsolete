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
package org.jboss.ejb3.mcint.annotationadaptor;

import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.annotations.BeanAnnotationAdapter;
import org.jboss.kernel.plugins.annotations.BeanAnnotationAdapterFactory;

/**
 * AddAnnotationPluginOnBeanAnnotationAdaptorCallbackService
 * 
 * Service which, upon start, will install "addAnnotationPlugin" as a method
 * callback for the Bean Annotation Adapter.
 * 
 * This is to allow annotation plugins to be automatically registered upon 
 * their installation into MC
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class AddAnnotationPluginOnBeanAnnotationAdaptorCallbackService
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   private static final String MC_NAMESPACE_EJB3 = "org.jboss.ejb3.";

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   private Kernel kernel;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public AddAnnotationPluginOnBeanAnnotationAdaptorCallbackService(Kernel kernel)
   {
      assert kernel != null : "Kernel is required";
      this.setKernel(kernel);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   @Start
   public void start() throws Exception
   {
      // Install the BeanAnnotationAdapter w/ callback to add annotation plugins on install
      BeanAnnotationAdapter beanAnnotationAdapter = BeanAnnotationAdapterFactory.getInstance()
            .getBeanAnnotationAdapter();
      String beanAnnotationAdapterBindName = MC_NAMESPACE_EJB3 + "BeanAnnotationAdapter";
      BeanMetaDataBuilder bmdb = BeanMetaDataBuilder.createBuilder(beanAnnotationAdapterBindName, beanAnnotationAdapter
            .getClass().getName());
      bmdb.addMethodInstallCallback("addAnnotationPlugin");
      try
      {
         this.getKernel().getController().install(bmdb.getBeanMetaData(), beanAnnotationAdapter);
      }
      catch (Throwable e)
      {
         throw new RuntimeException("Could not install " + beanAnnotationAdapter, e);
      }
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   protected Kernel getKernel()
   {
      return kernel;
   }

   protected void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

}
