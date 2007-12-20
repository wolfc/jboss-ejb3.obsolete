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
package org.jboss.injection;

import java.util.Collection;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.Environment;

/**
 * Process all ejb references. The non local references are processed
 * by inheritance.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 66558 $
 */
public class EJBHandler<X extends Environment> extends EJBRemoteHandler<X>
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(EJBHandler.class);

   public void loadXml(X xml, InjectionContainer container)
   {
      super.loadXml(xml, container);
      if (xml != null)
      {
         if (xml.getEjbLocalReferences() != null) loadEjbLocalXml(xml.getEjbLocalReferences(), container);
      }
   }

   protected void loadEjbLocalXml(Collection<EJBLocalReferenceMetaData> refs, InjectionContainer container)
   {
      for (EJBLocalReferenceMetaData ref : refs)
      {
         String interfaceName = ref.getLocal();
         String errorType = "<ejb-local-ref>";

         ejbRefXml(ref, interfaceName, container, errorType);
      }
   }
}
