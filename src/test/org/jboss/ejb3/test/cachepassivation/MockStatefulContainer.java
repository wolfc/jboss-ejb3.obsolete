/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.cachepassivation;

import java.util.Hashtable;

import org.jboss.aop.AspectManager;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.ejb3.stateful.StatefulContainer;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockStatefulContainer extends StatefulContainer
{

   @SuppressWarnings("unchecked")
   public MockStatefulContainer(ClassLoader cl, String beanClassName, String ejbName, AspectManager manager,
         Hashtable ctxProperties, InterceptorInfoRepository interceptorRepository, Ejb3Deployment deployment)
   {
      super(cl, beanClassName, ejbName, manager, ctxProperties, interceptorRepository, deployment);
   }
   
   @Override
   public Object createSession()
   {
      // TODO Auto-generated method stub
      return super.createSession();
   }
}
