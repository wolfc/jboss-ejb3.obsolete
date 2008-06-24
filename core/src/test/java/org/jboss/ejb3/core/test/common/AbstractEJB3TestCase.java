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
package org.jboss.ejb3.core.test.common;

import java.net.URL;

import org.jboss.aop.AspectManager;
import org.jboss.aop.AspectXmlLoader;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.common.registrar.plugin.mc.Ejb3McRegistrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Create an environment with some basic facilities on which EJB 3 containers
 * depend.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractEJB3TestCase
{
   private static EmbeddedTestMcBootstrap bootstrap;
   
   @AfterClass
   public static void afterClass() throws Exception
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource("ejb3-interceptors-aop.xml");
      if(url != null)
         AspectXmlLoader.undeployXML(url);
      
      if(bootstrap != null)
         bootstrap.shutdown();
      bootstrap = null;
   }
   
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      // FIXME: weirdness in InitialContextFactory (see EJBTHREE-1097)
      InitialContextFactory.close(null, null);
      
      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();
      
      // Bind Registrar
      Ejb3RegistrarLocator.bindRegistrar(new Ejb3McRegistrar(bootstrap.getKernel()));
      
      deploy("basicbootstrap-beans.xml");
      
      // TODO: AspectDeployment
      URL url = Thread.currentThread().getContextClassLoader().getResource("ejb3-interceptors-aop.xml");
      if(url == null)
         throw new IllegalStateException("Can't find ejb3-interceptors-aop.xml on class loader " + Thread.currentThread().getContextClassLoader());
      AspectXmlLoader.deployXML(url);
   }
   
   private static void deploy(String resourceName)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
      if(url == null)
         throw new IllegalArgumentException("Can't find a resource named '" + resourceName + "'");
      assert bootstrap != null : "Can't deploy a resource, bootstrap is null";
      bootstrap.deploy(url);
   }
   
   protected static EmbeddedTestMcBootstrap getBootstrap()
   {
      return bootstrap;
   }
   
   protected static Domain getDomain(String domainName)
   {
      DomainDefinition domainDef = AspectManager.instance().getContainer(domainName);
      if(domainDef == null) throw new IllegalArgumentException("No such domain '" + domainName + "'");
      return (Domain) domainDef.getManager();
   }
}
