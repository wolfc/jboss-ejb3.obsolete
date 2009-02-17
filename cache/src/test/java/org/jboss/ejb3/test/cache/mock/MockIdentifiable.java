/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.cache.mock;

import java.io.Serializable;

import org.jboss.ejb3.cache.Identifiable;
import org.jboss.logging.Logger;

/**
 * Mock implementation of an Identifiable.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockIdentifiable implements Identifiable, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 828205583403324513L;
   
   private static volatile int currentId = 0;
   
   protected Logger log = Logger.getLogger(getClass());
   
   public static int createId()
   {
      return ++currentId;
   }
   
   private int id;
   
   public MockIdentifiable(int id)
   {
      this.id = id;
   }
   
   public Object getId()
   {
      return id;
   }

   @Override
   public String toString()
   {
      return super.toString() + "{id=" + id + "}";
   }
}
