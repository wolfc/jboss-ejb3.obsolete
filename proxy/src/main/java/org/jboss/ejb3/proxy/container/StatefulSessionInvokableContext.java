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
package org.jboss.ejb3.proxy.container;

import org.jboss.ejb3.proxy.invocation.StatefulSessionContainerMethodInvocation;

/**
 * StatefulSessionInvokableContext
 * 
 * Represents an object capable of carrying out 
 * EJB3 SFSB Invocations as described by an 
 * Invocation descriptor
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface StatefulSessionInvokableContext<T extends StatefulSessionContainerMethodInvocation>
      extends
         InvokableContext<StatefulSessionContainerMethodInvocation>
{

   /**
    * The name of the "createSession" method for use in reflection
    */
   String METHOD_NAME_CREATESESSION = "createSession";

   String[] METHOD_SIGNATURE_CREATESESSION = new String[]
   {};

   /**
    * Creates a new user session and returns the unique Session ID
    * 
    * @return
    */
   Object createSession();
}
