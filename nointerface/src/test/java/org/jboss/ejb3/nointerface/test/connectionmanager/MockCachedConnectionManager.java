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
package org.jboss.ejb3.nointerface.test.connectionmanager;

import java.util.Set;

import javax.resource.ResourceException;

import org.jboss.ejb3.EJBContainer;
import org.jboss.jca.spi.ComponentStack;

/**
 * MockCachedConnectionManager
 *
 * The {@link EJBContainer} requires a cached connection manager of type
 * {@link ComponentStack} to be injected. This {@link MockCachedConnectionManager}
 * is just for use in the no-interface tests, and does not provide any real
 * functionality
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MockCachedConnectionManager implements ComponentStack
{

   public void popMetaAwareObject(Set unsharableResources) throws ResourceException
   {
      // this is a mock - do nothing
   }

   public void pushMetaAwareObject(Object rawKey, Set unsharableResources) throws ResourceException
   {
      // this is a mock - do nothing

   }

}
