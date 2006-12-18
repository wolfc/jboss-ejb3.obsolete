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
package org.jboss.injection.test.appclient.unit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import junit.framework.TestCase;

import org.jboss.injection.AnnotatedMethodFinder;
import org.jboss.injection.Injection;
import org.jboss.injection.Injector;
import org.jboss.injection.InjectorProcessor;
import org.jboss.injection.MapInjectorFactory;
import org.jboss.injection.PostConstructProcessor;
import org.jboss.injection.Processor;
import org.jboss.injection.ResourceClassProcessor;
import org.jboss.injection.test.appclient.HelloWorldClient;
import org.jboss.injection.test.common.Counter;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AppClientTestCase extends TestCase
{
   public void test1() throws Exception
   {
      Counter.reset();
      
      // There are multiple ways to initialize the injectors, annotation
      // is one of them.
      
      //
      // the environment to use
      //
      
      Map<String, Object> env = new HashMap<String, Object>();
      env.put(HelloWorldClient.class.getName() + "/value", "Hello world");
      
      //
      // the setup of injectors
      //
      
      // TODO: weird, shouldn't this work?
      //Collection<InjectionProcessor<Class<?>>> handlers = new ArrayList<InjectionProcessor<Class<?>>>();
      Collection<Processor<Class<?>, Collection<Injector>>> handlers = new ArrayList<Processor<Class<?>, Collection<Injector>>>();
      handlers.add(new ResourceClassProcessor(new MapInjectorFactory(env)));
      Collection<Injector> injectors = Injection.doIt(HelloWorldClient.class, handlers);
      
      assertEquals("Wrong number of injectors", 1, injectors.size());
      
      Collection<Processor<Class<?>, Collection<Method>>> postConstructProcessors = new ArrayList<Processor<Class<?>, Collection<Method>>>();
      postConstructProcessors.add(new PostConstructProcessor());
      Collection<Method> postConstructs = Injection.doIt(HelloWorldClient.class, postConstructProcessors);
      
      assertEquals("Wrong number of post-constructs", 1, postConstructs.size());
      
      Collection<Processor<Class<?>, Collection<Method>>> preDestroyProcessors = new ArrayList<Processor<Class<?>, Collection<Method>>>();
      preDestroyProcessors.add(new AnnotatedMethodFinder<PreDestroy>(PreDestroy.class));
      Collection<Method> preDestroys = Injection.doIt(HelloWorldClient.class, preDestroyProcessors);
      
      assertEquals("Wrong number of pre-destroys", 1, preDestroys.size());
      
      //
      // the target
      //
      
      // With app client there is no target instance.
      
      //
      // the injection
      //
      
      InjectorProcessor.process(injectors, postConstructs);
      
      //
      // do some business
      //
      
      HelloWorldClient.check();
      
      assertEquals("postConstruct should have been called once", 1, Counter.postConstructs);
      
      //
      // destroy phase
      //
      
      InjectorProcessor.destroy(preDestroys);
      
      assertEquals("preDestroy should have been called once", 1, Counter.preDestroys);
   }
}
