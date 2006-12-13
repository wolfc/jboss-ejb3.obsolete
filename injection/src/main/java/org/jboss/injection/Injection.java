/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.injection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The facade for the injection framework.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class Injection
{
   /**
    * Fires of a collection of processors on a given object.
    * 
    * @param <T1>   the object type
    * @param <T2>   the generic type of the resultant collection
    * @param t      the object on which processing takes place
    * @param c      the collection of processors
    * @return       the resultant collection
    */
   public static <T1, T2> Collection<T2> doIt(T1 t, Collection<Processor<T1, Collection<T2>>> c)
   {
      Collection<T2> list = new ArrayList<T2>();
      for(Processor<T1, Collection<T2>> processor : c)
      {
         list.addAll(processor.process(t));
      }
      return list;
   }
   
   /**
    * Process an object for injection.
    * Find out which injectors are registered for this object's class and
    * run them.
    * 
    * @param instance
    */
   public static void process(Object instance)
   {
      
   }
}
