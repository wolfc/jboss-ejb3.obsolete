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
package org.jboss.ejb3.stateless;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 * An EJB stateless session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision$
 */
public class StatelessHandleRemoteImpl
      implements Handle
{
   /** Serial Version Identifier. */
   static final long serialVersionUID = 3811452873535097661L;
   
   private EJBObject proxy;

   // Constructors --------------------------------------------------

   public StatelessHandleRemoteImpl()
   {
      
   }
   
   /**
    * Construct a <tt>StatelessHandleImpl</tt>.
    *
    * @param handle    The initial context handle that will be used
    *                  to restore the naming context or null to use
    *                  a fresh InitialContext object.
    * @param name      JNDI name.
    */
   public StatelessHandleRemoteImpl(EJBObject proxy)
   {
      this.proxy = proxy;
   }

   // Public --------------------------------------------------------

   public EJBObject getEJBObject() throws RemoteException
   {
      return this.proxy;
   }
}
