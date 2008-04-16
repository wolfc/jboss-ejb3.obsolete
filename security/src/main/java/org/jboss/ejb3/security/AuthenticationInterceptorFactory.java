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
package org.jboss.ejb3.security;

import org.jboss.aop.Advisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;

/**
 * Authentication Interceptor Factory
 * @author bill.burke@jboss.org
 * @author Anil.Saldhana@redhat.com 
 */
public class AuthenticationInterceptorFactory  extends PerClassAspectFactoryAdaptor 
implements AspectFactory
{
   private static final Logger log = Logger.getLogger(AuthenticationInterceptorFactory.class);
   
   public Object createPerClass(Advisor advisor)
   {
      Container container = EJBContainer.getEJBContainer(advisor);
      AuthenticationManager manager = container.getSecurityManager(AuthenticationManager.class);
      log.debug("Creating interceptor with authentication manager '" + manager + "'" + (manager != null ? " (security domain '" + manager.getSecurityDomain() + "')" : ""));
      //return new Ejb3AuthenticationInterceptor(manager, container);
      
      return new Ejb3AuthenticationInterceptorv2(container);
   } 
}

