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
package org.jboss.injection.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.injection.Injection;
import org.jboss.injection.Injector;
import org.jboss.injection.InjectorProcessor;
import org.jboss.injection.MapInjectorFactory;
import org.jboss.injection.PostConstructProcessor;
import org.jboss.injection.Processor;
import org.jboss.injection.ResourceClassProcessor;
import org.jboss.logging.Logger;

/**
 * Intercepts construction of new objects and fires up injection.
 * 
 * Note that this is useless for EJB 3, because the lifecycle of a bean
 * contains passivation and activation.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ConstructorInterceptor implements Interceptor
{
   private static final Logger log = Logger.getLogger(ConstructorInterceptor.class);
   
   public String getName()
   {
      return "ConstructorInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      log.info("here");
      Class<?> cls = invocation.getTargetObject().getClass();
      
      Map<String, Object> env = InjectionEnvironment.getCurrent();
      
      Collection<Processor<Class<?>, Collection<Injector>>> handlers = new ArrayList<Processor<Class<?>, Collection<Injector>>>();
      handlers.add(new ResourceClassProcessor(new MapInjectorFactory(env)));
      Collection<Injector> injectors = Injection.doIt(cls, handlers);
      
      Collection<Processor<Class<?>, Collection<Method>>> postConstructProcessors = new ArrayList<Processor<Class<?>, Collection<Method>>>();
      postConstructProcessors.add(new PostConstructProcessor());
      Collection<Method> postConstructs = Injection.doIt(cls, postConstructProcessors);
      
      InjectorProcessor.process(invocation.getTargetObject(), injectors, postConstructs);
      return invocation.invokeNext();
   }
}
