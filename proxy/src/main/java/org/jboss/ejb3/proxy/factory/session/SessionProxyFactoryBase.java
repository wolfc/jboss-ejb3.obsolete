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
package org.jboss.ejb3.proxy.factory.session;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.jboss.ejb3.proxy.factory.ProxyFactoryBase;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.util.NotImplementedException;

/**
 * SessionProxyFactoryBase
 * 
 * Base upon which Session Proxy Factory implementations
 * may build
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionProxyFactoryBase extends ProxyFactoryBase implements SessionProxyFactory
{

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private JBossSessionBeanMetaData metadata;

   /**
    * Constructor for the default Proxy Constructor (All
    * business interfaces and, if bound together, the EJB2.x Home)
    */
   private Constructor<?> constructorProxyDefault;

   /**
    * Map of Proxy Constructors, one per business interface, with key 
    * as the fully-qualified class name of the interface, and value
    * of the constructor to use
    */
   private Map<String, Constructor<?>> constructorsProxySpecificBusinessInterface;

   /**
    * Constructor for the EJB2.x Home Proxy
    */
   private Constructor<?> constructorProxyHome;

   /**
    * Constructor for the EJB2.x View Proxy
    */
   private Constructor<?> constructorProxyEjb2x;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param metadata The metadata representing this Session Bean
    * @param classloader The ClassLoader associated with the SessionContainer
    *       for which this ProxyFactory is to generate Proxies
    */
   public SessionProxyFactoryBase(final JBossSessionBeanMetaData metadata, final ClassLoader classloader)
   {
      // Call Super
      super(classloader);

      // Set metadata
      this.setMetadata(metadata);
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Create an EJB2.x Home Proxy
    * 
    * @return
    */
   public Object createProxyHome()
   {
      throw new NotImplementedException("ALR");
   }

   /**
    * Create an EJB3 Business proxy with no 
    * specific target business interface.  The 
    * returned proxy will implement all appropriate
    * business interfaces. 
    * 
    * @return
    */
   public Object createProxyBusiness()
   {
      throw new NotImplementedException("ALR");
   }

   /**
    * Create a Proxy for both EJB2.x Home and 
    * Business Views, used when both the Home and Business
    * interfaces are bound together
    * 
    * @return
    */
   public Object createProxyBusinessAndHome()
   {
      throw new NotImplementedException("ALR");
   }

   /**
    * Create an EJB3 Business Proxy specific to the specified
    * target business interface name (expressed as 
    * a fully-qualified class name)
    * 
    * @param id
    * @param businessInterfaceName
    * @return
    */
   public Object createProxyBusiness(String businessInterfaceName)
   {
      throw new NotImplementedException("ALR");
   }

   /**
    * Create an EJB2.x Proxy 
    * 
    * @return
    */
   public Object createProxyEjb2x()
   {
      throw new NotImplementedException("ALR");
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle callback to be invoked by the ProxyFactoryDeployer
    * before the ProxyFactory is able to service requests
    * 
    *  @throws Exception
    */
   @Override
   public void start() throws Exception
   {
      super.start();
      //TODO
   }

   /**
    * Lifecycle callback to be invoked by the ProxyFactoryDeployer
    * before the ProxyFactory is taken out of service, 
    * possibly GC'd
    * 
    * @throws Exception
    */
   @Override
   public void stop() throws Exception
   {
      super.stop();
      //TODO
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public JBossSessionBeanMetaData getMetadata()
   {
      return metadata;
   }

   protected void setMetadata(final JBossSessionBeanMetaData metadata)
   {
      this.metadata = metadata;
   }

   protected Constructor<?> getConstructorProxyDefault()
   {
      return constructorProxyDefault;
   }

   protected void setConstructorProxyDefault(Constructor<?> constructorProxyDefault)
   {
      this.constructorProxyDefault = constructorProxyDefault;
   }

   protected Map<String, Constructor<?>> getConstructorsProxySpecificBusinessInterface()
   {
      return constructorsProxySpecificBusinessInterface;
   }

   protected void setConstructorsProxySpecificBusinessInterface(
         Map<String, Constructor<?>> constructorsProxySpecificBusinessInterface)
   {
      this.constructorsProxySpecificBusinessInterface = constructorsProxySpecificBusinessInterface;
   }

   protected Constructor<?> getConstructorProxyHome()
   {
      return constructorProxyHome;
   }

   protected void setConstructorProxyHome(Constructor<?> constructorProxyHome)
   {
      this.constructorProxyHome = constructorProxyHome;
   }

   protected Constructor<?> getConstructorProxyEjb2x()
   {
      return constructorProxyEjb2x;
   }

   protected void setConstructorProxyEjb2x(Constructor<?> constructorProxyEjb2x)
   {
      this.constructorProxyEjb2x = constructorProxyEjb2x;
   }

}
