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
package org.jboss.ejb3.api.spi;

import javax.ejb.EJBContainer;

/**
 * Provides a simple wrapper around an EJBContainer to see whether
 * is has been closed.
 * 
 * @deprecated the whole concept of current EJB container is deprecated
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Deprecated
public class EJBContainerWrapper extends EJBContainer
{
   private boolean closed = false;
   private EJBContainer delegate;
   
   public EJBContainerWrapper(EJBContainer delegate)
   {
      this.delegate = delegate;
   }
   
   @Override
   public void close()
   {
      this.closed = true;
      delegate.close();
   }
   
   public EJBContainer getDelegate()
   {
      return delegate;
   }
   
   public boolean isClosed()
   {
      return closed;
   }
}
