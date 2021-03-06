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
package org.jboss.ejb3.annotation.impl;

import java.lang.annotation.Annotation;

import org.jboss.ejb3.annotation.Pool;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class PoolImpl implements Pool
{
   public String value;

   public int maxSize = 30;

   public long timeout = Long.MAX_VALUE;

   public PoolImpl()
   {
   }

   public String value()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   public int maxSize()
   {
      return maxSize;
   }

   public void setMaxSize(int maxSize)
   {
      this.maxSize = maxSize;
   }

   public long timeout()
   {
      return timeout;
   }

   public void setTimeout(long timeout)
   {
      this.timeout = timeout;
   }

   public Class<? extends Annotation> annotationType()
   {
      return Pool.class;
   }

   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("[PoolFactory name:");
      sb.append("value=").append(value);
      sb.append(", maxSize=").append(maxSize);
      sb.append(", timeout=").append(timeout);
      sb.append("]");
      return sb.toString();
   }
}
