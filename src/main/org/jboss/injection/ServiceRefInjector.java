/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.injection;

// $Id: ServiceRefInjector.java 66584 2007-10-30 22:22:57Z thomas.diesler@jboss.com $

import java.lang.reflect.AnnotatedElement;

import javax.naming.Context;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.serviceref.ServiceReferenceHandler;
import org.jboss.metadata.serviceref.VirtualFileAdaptor;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

/**
 * Inject a web service ref.
 *
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: 66584 $
 */
public class ServiceRefInjector implements EncInjector
{
   private static final Logger log = Logger.getLogger(ServiceRefInjector.class);

   private String name;
   private ServiceReferenceMetaData sref;

   public ServiceRefInjector(String name, AnnotatedElement anElement, ServiceReferenceMetaData sref)
   {
      this.name = name;
      this.sref = sref;
      this.sref.setAnnotatedElement(anElement);
   }

   public void inject(InjectionContainer container)
   {
      try
      {
         Context envCtx = container.getEnc();
         ClassLoader loader = container.getClassloader();
         UnifiedVirtualFile vfsRoot = new VirtualFileAdaptor(container.getRootFile());
         new ServiceReferenceHandler().bindServiceRef(envCtx, name, vfsRoot, loader, sref);
      }
      catch (Exception e)
      {
         throw new WebServiceException("Unable to bind ServiceRef [enc=" + name + "]", e);
      }
   }

   public String toString()
   {
      return super.toString() + "{enc=" + name + "}";
   }
}
