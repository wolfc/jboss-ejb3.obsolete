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
package org.jboss.ejb3.test.interceptors2;

import java.io.Serializable;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class Interception implements Serializable
{
   private String classname;
   private String method;
   private int instance = -1;
   
   public Interception(Object obj, String method)
   {
      if (obj instanceof Class)
         this.classname = ((Class)obj).getName();
      else
         this.classname = obj.getClass().getName();
      
      this.method = method;
   }
   
   public Interception(Object obj, String method, int instance)
   {
      this.classname = obj.getClass().getName();
      this.method = method;
      this.instance = instance;
   }
   
   public String getClassname()
   {
      return classname;
   }
   
   public int getInstance()
   {
      return instance;
   }
   
   public String getMethod()
   {
      return method;
   }
   
   public String toString()
   {
      return classname + "." + method + "-" + instance;
   }
}
