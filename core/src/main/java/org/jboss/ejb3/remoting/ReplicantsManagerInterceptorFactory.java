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
package org.jboss.ejb3.remoting;

import java.util.Map;

import org.jboss.aop.Advisor;
import org.jboss.aop.InstanceAdvisor;
import org.jboss.aop.advice.AspectFactory;
import org.jboss.aop.joinpoint.Joinpoint;
import org.jboss.aspects.remoting.ReplicantsManagerInterceptor;
import org.jboss.ejb3.session.SessionContainer;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ReplicantsManagerInterceptorFactory implements AspectFactory
{
   public Object createPerVM()
   {
      throw new RuntimeException("IMPROPER USAGE");
   }

   public Object createPerClass(Advisor advisor)
   {
      SessionContainer container = SessionContainer.getEJBContainer(advisor);
      Map<?, ?> families = container.getClusterFamilies();
      assert families != null : "families is null";
      return new ReplicantsManagerInterceptor(families);
   }

   public Object createPerInstance(Advisor advisor, InstanceAdvisor instanceAdvisor)
   {
      throw new RuntimeException("IMPROPER USAGE");
   }

   public Object createPerJoinpoint(Advisor advisor, Joinpoint jp)
   {
      throw new RuntimeException("IMPROPER USAGE");
   }

   public Object createPerJoinpoint(Advisor advisor, InstanceAdvisor instanceAdvisor, Joinpoint jp)
   {
      throw new RuntimeException("IMPROPER USAGE");
   }

   public String getName()
   {
      return this.getClass().getName();
   }
}
