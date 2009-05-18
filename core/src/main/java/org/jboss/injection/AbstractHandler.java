/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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

import javax.naming.NameNotFoundException;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.deployers.JBoss5DependencyPolicy;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;

/**
 * Common base for annotation/xml handlers.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractHandler<X extends RemoteEnvironment> implements InjectionHandler<X>
{
   private static final Logger log = Logger.getLogger(AbstractHandler.class);
   
//   protected void addDependency(String refName, EJBContainer refcon, InjectionContainer container)
//   {
//      // Do not depend on myself
//      if(!container.equals(refcon))
//         container.getDependencyPolicy().addDependency(refcon.getObjectName().getCanonicalName());
//   }
   
   
   /**
    * @deprecated resolve until a bean name is acquired, do not depend on a business interface
    */
   @Deprecated
   protected void addDependency(InjectionContainer container, Class<?> businessIntf)
   {
      log.warn("EJBTHREE-1828: calling deprecated addDependency");
      
      EJBContainer refCon = null;
      try
      {
         refCon = (EJBContainer) container.resolveEjbContainer(businessIntf);
      }
      catch(NameNotFoundException e)
      {
         // ignore
      }
      // Do not depend on myself
      if(container.equals(refCon))
         return;
      
      ((JBoss5DependencyPolicy) container.getDependencyPolicy()).addDependency(businessIntf);
   }
   
   /**
    * @deprecated resolve until a bean name is acquired, do not depend on a business interface
    */
   @Deprecated
   protected void addDependency(InjectionContainer container, String link, Class<?> businessIntf)
   {
      log.warn("EJBTHREE-1828: calling deprecated addDependency");
      
      EJBContainer refCon = (EJBContainer) container.resolveEjbContainer(link, businessIntf);
      
      // Do not depend on myself
      if(container.equals(refCon))
         return;
      
      ((JBoss5DependencyPolicy) container.getDependencyPolicy()).addDependency(link, businessIntf);
   }
   
   protected void addJNDIDependency(InjectionContainer container, String jndiName)
   {
      // Do not depend on myself
      if(container.hasJNDIBinding(jndiName))
         return;
      ((JBoss5DependencyPolicy) container.getDependencyPolicy()).addJNDIName(jndiName);
   }
}
