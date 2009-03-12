/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.singleton.test.simple.unit;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.Domain;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.common.lang.SerializableMethod;
import org.jboss.ejb3.common.metadata.MetadataUtil;
import org.jboss.ejb3.proxy.intf.SessionProxy;
import org.jboss.ejb3.singleton.SingletonContainer;
import org.jboss.ejb3.singleton.metadata.SingletonProcessor;
import org.jboss.ejb3.singleton.test.common.MockEjb3Deployment;
import org.jboss.ejb3.singleton.test.simple.SimpleSingletonBean;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.AbstractCreator;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.process.chain.ProcessorChain;
import org.jboss.naming.JavaCompInitializer;
import org.jnp.server.SingletonNamingServer;
import org.junit.Test;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleSingletonTestCase
{
   private static final Logger log = Logger.getLogger(SimpleSingletonTestCase.class);
   
   private static void deployAop(String name) throws Exception
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(name);
      if (url == null)
         throw new IllegalStateException("Can't find " + name + " on class loader "
               + Thread.currentThread().getContextClassLoader());
      AspectXmlLoader.deployXML(url);
   }
   
   private static JBossSessionBeanMetaData getMetaDataFromBeanImplClass(Class<?> beanClass)
   {
      // emulate annotation deployer
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      Collection<Class<?>> classes = new HashSet<Class<?>>();
      /*
      for (Class<?> beanImplClass : beanImplClasses)
      {
         boolean unique = classes.add(beanImplClass);
         if (!unique)
         {
            log.warn("Specified class " + beanImplClass + " was not unique, skipping...");
         }
      }
      */
      classes.add(beanClass);
      
      AbstractCreator<JBossMetaData> creator = new JBoss50Creator(finder);
      creator.addProcessor(new SingletonProcessor(finder));
      JBossMetaData metadata = creator.create(classes);

      /*
       * Mock the EjbMetadataJndiPolicyDecoratorDeployer
       */

      // Decorate w/ JNDI Policy
      log.debug("Decorating EJB3 EJBs in " + metadata + " with JNDI Policy");
      MetadataUtil.decorateEjbsWithJndiPolicy(metadata, AccessController
            .doPrivileged(new PrivilegedAction<ClassLoader>()
            {

               public ClassLoader run()
               {
                  return Thread.currentThread().getContextClassLoader();
               }

            }));

      /*
       * Mock the post-merge processing deployers
       */
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      ProcessorChain<JBossMetaData> chain = MetadataUtil.getPostMergeMetadataProcessorChain(cl);
      chain.process(metadata);

      // Return
      return (JBossSessionBeanMetaData) metadata.getEnterpriseBeans().iterator().next();
   }
   
   @Test
   public void test1() throws Throwable
   {
      new SingletonNamingServer();
      new JavaCompInitializer().start();
      
      deployAop("ejb3-interceptors-aop.xml");
      deployAop("simple-singleton-aop.xml");
      
      String containerName = "Singleton Bean";
      
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String beanClassName = SimpleSingletonBean.class.getName();
      String ejbName = "SimpleSingletonBean";
      Domain domain = (Domain) AspectManager.instance().getContainer(containerName).getManager();
      Hashtable<?, ?> ctxProperties = null;
      Ejb3Deployment deployment = new MockEjb3Deployment();
      JBossSessionBeanMetaData beanMetaData = getMetaDataFromBeanImplClass(SimpleSingletonBean.class);
      SingletonContainer container = new SingletonContainer(cl, beanMetaData.getEjbClass(), beanMetaData.getEjbName(), domain, ctxProperties, deployment, beanMetaData);
      
      SessionProxy proxy = new SessionProxy() {
         @Override
         public Object getTarget()
         {
            return null;
         }
         
         @Override
         public void setTarget(Object target)
         {
            // TODO Auto-generated method stub
            //
            throw new RuntimeException("NYI");
         }
         
      };
      Method realMethod = SimpleSingletonBean.class.getMethod("getState");
      SerializableMethod method = new SerializableMethod(realMethod, SimpleSingletonBean.class);
      Object args[] = null;
      container.invoke(proxy, method, args);
   }
}
