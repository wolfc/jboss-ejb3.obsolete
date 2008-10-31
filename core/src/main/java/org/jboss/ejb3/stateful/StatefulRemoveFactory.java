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
package org.jboss.ejb3.stateful;

import java.lang.reflect.Method;

import javax.ejb.Remove;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aop.joinpoint.MethodJoinpoint;
import org.jboss.ejb3.annotation.impl.RemoveImpl;
import org.jboss.logging.Logger;

/**
 * comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 */
public class StatefulRemoveFactory implements AspectFactory
{
   private static final Logger log = Logger.getLogger(StatefulRemoveFactory.class);
   
   public Object createPerVM()
   {
      throw new IllegalStateException("PER_VM NOT APPLICABLE");
   }

   public Object createPerClass(Advisor advisor)
   {
      throw new IllegalStateException("PER_CLASS NOT APPLICABLE");
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      throw new IllegalStateException("PER_INSTANCE NOT APPLICABLE");
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      if (jp instanceof MethodJoinpoint == false)
         throw new IllegalArgumentException("Joinpoint is not a method: " + jp);
      MethodJoinpoint methodJoinpoint = MethodJoinpoint.class.cast(jp);
      Method method = methodJoinpoint.getMethod();
      Remove rm = (Remove) advisor.resolveAnnotation(method, Remove.class);
      if (rm == null)
      {
         log.warn("Cannot find @" + Remove.class.getName() + " for " + method + " assuming defaults");
         rm = new RemoveImpl();
      }
      return new StatefulRemoveInterceptor(rm.retainIfException());
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      throw new IllegalStateException("PER_CLASS_JOINPOINT NOT APPLICABLE");
   }

   public String getName()
   {
      return "StatefulRemoveInterceptor";
   }
}
