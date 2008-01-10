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
package org.jboss.ejb3.test.ejbthree786;

import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * Defines a custom "remove" method that is not associated w/ EJBOBject/EJBLocalObject.remove
 *
 * @author <a href="mailto:arubinge@redhat.com">ALR</a>
 * @version $Revision$
 */
@Stateless
@LocalBinding(jndiBinding = StatelessRemoveBean.JNDI_NAME_LOCAL)
@RemoteBinding(jndiBinding = StatelessRemoveBean.JNDI_NAME_REMOTE)
public class StatelessRemoveBean extends AbstractRemoveBean implements RemoveStatelessRemote, RemoveStatelessLocal
{
   // Class Members
   public static final String JNDI_NAME_REMOTE = "StatelessRemoveBean/remote";

   public static final String JNDI_NAME_LOCAL = "StatelessRemoveBean/local";
}