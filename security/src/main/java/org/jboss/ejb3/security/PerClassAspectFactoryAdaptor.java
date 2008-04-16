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
package org.jboss.ejb3.security;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.joinpoint.Joinpoint;

//$Id$

/**
 *  Adaptor class that throws Runtime Exception
 *  for all Non_PerClass create requests
 *  @author Anil.Saldhana@redhat.com
 *  @since  Aug 14, 2007 
 *  @version $Revision$
 */
public abstract class PerClassAspectFactoryAdaptor implements AspectFactory
{ 
   private RuntimeException rte = new RuntimeException("Only PER_CLASS supported in " +
        "this interceptor factory");
   
   public abstract Object createPerClass(Advisor advisor);

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   { 
      throw rte;
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   { 
      throw rte;
   }

   public Object createPerJoinpoint(Advisor advisor, 
         InstanceAdvisor instanceAdvisor, Joinpoint jp)
   { 
      throw rte;
   }

   public Object createPerVM()
   { 
      throw rte;
   }

   public String getName()
   {
      return getClass().getName();
   }
}
