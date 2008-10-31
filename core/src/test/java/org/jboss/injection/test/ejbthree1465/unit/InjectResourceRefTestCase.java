/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.injection.test.ejbthree1465.unit;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.AccessibleObject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.NonSerializableFactory;
import org.jboss.injection.EncInjector;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.ResourceHandler;
import org.jboss.injection.test.common.DummyInjectionContainer;
import org.jboss.injection.test.ejbthree1465.MyClass;
import org.jboss.metadata.javaee.jboss.JBossRemoteEnvironmentRefsGroupMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ResourceAuthorityType;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceSharingScopeType;
import org.jnp.server.SingletonNamingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.ORB;


/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InjectResourceRefTestCase
{
   private static SingletonNamingServer naming;
   private static InitialContext ctx;
   
   @AfterClass
   public static void afterClass() throws NamingException
   {
      ctx.close();
      
      naming.destroy();
      naming = null;
   }
   
   @BeforeClass
   public static void beforeClass() throws NamingException
   {
      naming = new SingletonNamingServer();
      
      ctx = new InitialContext();
   }
   
   @Test
   public void test1() throws NamingException
   {
      Context enc = ((Context) ctx.lookup("java:")).createSubcontext("comp");
      NonSerializableFactory.bind(enc, "ORB", ORB.init());
      
      ResourceInjectionTargetMetaData injectionTarget = new ResourceInjectionTargetMetaData();
      injectionTarget.setInjectionTargetClass(MyClass.class.getName());
      injectionTarget.setInjectionTargetName("orb");
      
      Set<ResourceInjectionTargetMetaData> injectionTargets = new HashSet<ResourceInjectionTargetMetaData>();
      injectionTargets.add(injectionTarget);
      
      ResourceReferenceMetaData resourceReference = new ResourceReferenceMetaData();
      resourceReference.setResourceRefName("orb");
      resourceReference.setType(ORB.class.getName());
      resourceReference.setResAuth(ResourceAuthorityType.Container);
      resourceReference.setResSharingScope(ResourceSharingScopeType.Shareable);
      resourceReference.setInjectionTargets(injectionTargets);
      
      ResourceReferencesMetaData resourceReferences = new ResourceReferencesMetaData();
      resourceReferences.add(resourceReference);
      
      JBossRemoteEnvironmentRefsGroupMetaData xml = new JBossRemoteEnvironmentRefsGroupMetaData();
      xml.setResourceReferences(resourceReferences);
      
      DummyInjectionContainer container = new DummyInjectionContainer(enc);
      
      ResourceHandler<RemoteEnvironment> handler = new ResourceHandler<RemoteEnvironment>();
      
      Collection<InjectionHandler<RemoteEnvironment>> handlers = new HashSet<InjectionHandler<RemoteEnvironment>>();
      handlers.add(handler);

      for(InjectionHandler<RemoteEnvironment> h : handlers) h.loadXml(xml, container);
      
      Map<AccessibleObject, Injector> tmp = InjectionUtil.processAnnotations(container, handlers, MyClass.class);

      for(EncInjector encInjector : container.getEncInjectors().values())
         encInjector.inject(container);
      
      MyClass instance = new MyClass();
      
      for(Injector injector : tmp.values())
         injector.inject(instance);
      
      assertNotNull(instance.getORB());
   }
}
