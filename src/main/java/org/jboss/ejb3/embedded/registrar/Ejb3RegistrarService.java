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
package org.jboss.ejb3.embedded.registrar;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.DuplicateBindException;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;

/**
 * Boot up the Ejb3Registrar.
 * 
 * Don't use the Ejb3Registrar. Instead obtain reference to MC beans via injection, so
 * that dependencies are properly setup.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Deprecated
public class Ejb3RegistrarService
{
   private static final Logger log = Logger.getLogger(Ejb3RegistrarService.class);
   
   private Kernel kernel;
   
   private Ejb3Registrar registrar;
   
   public Ejb3Registrar getRegistrar()
   {
      return registrar;
   }
   
   @Inject(bean="jboss.kernel:service=Kernel")
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }
   
   public void start() throws DuplicateBindException
   {
      log.info("Starting (deprecated) Ejb3RegistrarService");
      
      registrar = new Ejb3McRegistrar(kernel);
      
      Ejb3RegistrarLocator.bindRegistrar(registrar);
   }
   
   public void stop()
   {
      log.info("Stopping (deprecated) Ejb3RegistrarService");
      
      Ejb3RegistrarLocator.unbindRegistrar();
   }
}
