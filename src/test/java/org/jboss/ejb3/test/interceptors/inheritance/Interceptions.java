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
package org.jboss.ejb3.test.interceptors.inheritance;

import java.util.ArrayList;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class Interceptions
{
   private static ArrayList<Class<?>> aroundInvokes = new ArrayList<Class<?>>();
   private static ArrayList<Class<?>> postConstructs = new ArrayList<Class<?>>();
   private static ArrayList<Class<?>> preDestroys = new ArrayList<Class<?>>();
   
   public static void addAroundInvoke(Class<?> clazz)
   {
      aroundInvokes.add(clazz);
   }
   
   public static void addPostConstruct(Class<?> clazz)
   {
      postConstructs.add(clazz);
   }
   
   public static void addPreDestroy(Class<?> clazz)
   {
      preDestroys.add(clazz);
   }
   
   public static void clear()
   {
      aroundInvokes.clear();
      postConstructs.clear();
      preDestroys.clear();
   }
   
   public static ArrayList<Class<?>> getAroundInvokes()
   {
      return aroundInvokes;
   }

   public static ArrayList<Class<?>> getPostConstructs()
   {
      return postConstructs;
   }

   public static ArrayList<Class<?>> getPreDestroys()
   {
      return preDestroys;
   }
}
