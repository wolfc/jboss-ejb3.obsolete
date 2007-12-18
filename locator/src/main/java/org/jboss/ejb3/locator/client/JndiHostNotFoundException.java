/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.locator.client;

/**
 * JndiHostNotFoundException
 * 
 * Thrown upon failed attempt to lookup a JNDI Host
 * by its ID
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision $$
 *
 */
public class JndiHostNotFoundException extends Exception
{
   // Class Members

   private static final long serialVersionUID = 2054153832183595691L;

   // Constructors
   public JndiHostNotFoundException(String arg0)
   {
      super(arg0);
   }

   public JndiHostNotFoundException(String arg0, Throwable arg1)
   {
      super(arg0, arg1);
   }

   public JndiHostNotFoundException(Throwable arg0)
   {
      super(arg0);
   }
}
