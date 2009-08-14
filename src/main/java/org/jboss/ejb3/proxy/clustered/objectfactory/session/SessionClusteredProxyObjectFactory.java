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

package org.jboss.ejb3.proxy.clustered.objectfactory.session;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Name;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.ClusterChooserInterceptor;
import org.jboss.aspects.remoting.ClusteredPojiProxy;
import org.jboss.aspects.remoting.FamilyWrapper;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.ejb3.proxy.clustered.objectfactory.ClusteredProxyFactoryReferenceAddressTypes;
import org.jboss.ejb3.proxy.impl.factory.ProxyFactory;
import org.jboss.ejb3.proxy.impl.jndiregistrar.JndiSessionRegistrarBase;
import org.jboss.ejb3.proxy.impl.objectfactory.ProxyFactoryReferenceAddressTypes;
import org.jboss.ejb3.proxy.impl.objectfactory.ProxyObjectFactory;
import org.jboss.ejb3.proxy.impl.objectfactory.session.SessionProxyObjectFactory;
import org.jboss.ejb3.proxy.impl.remoting.IsLocalProxyFactoryInterceptor;
import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvokerLocator;


/**
 * A JNDI Object Factory responsible for parsing metadata obtained from 
 * Reference Address information and returning the appropriate clustered 
 * Session Proxy.
 * <p>
 * Extends the superclass by ensuring that any remote calls to a proxy factory
 * that are needed to create the Session Proxy are properly load balanced.
 * 
 * @author Brian Stansberry
 * @version $Revision: $
 *
 */
public class SessionClusteredProxyObjectFactory extends SessionProxyObjectFactory
{
   // --------------------------------------------------------------------------------||
   // Class Members  -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /** The serialVersionUID */
   private static final long serialVersionUID = 2550630984434261500L;
   
   private static final Logger log = Logger.getLogger(SessionClusteredProxyObjectFactory.class.getName());
   
   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Clustered Session Bean Object Factories always create a new Proxy in order
    * to get the latest target list for the bean from the server.
    * 
    * @param proxyFactory The ProxyFactory to use
    * @param name The JNDI name looked up
    * @param referenceAddresses
    */
   @Override
   protected Object getProxy(ProxyFactory proxyFactory, Name name, Map<String, List<String>> referenceAddresses)
   {
      // We don't want any caching, as the target list can get out of date
      return this.createProxy(proxyFactory, name, referenceAddresses);
   }

   /**
    * Here we replace the superclass implementation to create a cluster aware
    * proxy that will load balance requests to the server-side proxy factory.
    * 
    * Deprecated since https://jira.jboss.org/jira/browse/EJBTHREE-1884 - The
    * {@link ProxyObjectFactory} is no longer responsible for creating a proxy
    * to the {@link ProxyFactory}. Instead the {@link ProxyObjectFactory} will
    * lookup in the JNDI for the {@link ProxyFactory} using the
    * <code>proxyFactoryRegistryKey</code>. The responsibility of
    * binding the proxyfactory to jndi will rest with the {@link JndiSessionRegistrarBase}
    * 
    * @see The new {@link #getProxyFactoryFromJNDI(String, javax.naming.Context, java.util.Hashtable) 
    *  
    * {@inheritDoc}
    */
   @Deprecated
   @Override
   protected ProxyFactory createProxyFactoryProxy(Name name, Map<String, List<String>> refAddrs,
         String proxyFactoryRegistryKey) throws Exception
   {
      // Obtain the URL for invoking upon the Registry
      String url = this.getSingleRequiredReferenceAddressValue(name, refAddrs,
            ProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_INVOKER_LOCATOR_URL);

      // Create an InvokerLocator
      assert url != null && !url.trim().equals("") : InvokerLocator.class.getSimpleName()
            + " URL is required, but is not specified; improperly bound reference in JNDI";
      InvokerLocator locator = new InvokerLocator(url);
      
      String partitionName = this.getSingleRequiredReferenceAddressValue(name, refAddrs,
            ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_PARTITION_NAME);
      
      assert partitionName != null && !partitionName.trim().equals("") : " Partition name is required, but is not specified; improperly bound reference in JNDI";
      
      String lbpClass = this.getSingleRequiredReferenceAddressValue(name, refAddrs,
            ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_PROXY_FACTORY_LOAD_BALANCE_POLICY);
      
      assert lbpClass != null && !lbpClass.trim().equals("") : LoadBalancePolicy.class.getSimpleName() 
            + " class name is required, but is not specified; improperly bound reference in JNDI";
      
      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
      LoadBalancePolicy loadBalancePolicy = (LoadBalancePolicy) tccl.loadClass(lbpClass).newInstance();
      
      FamilyWrapper wrapper = getFamilyWrapper(name, refAddrs);
      
      Class<?>[] interfaces = {this.getProxyFactoryClass()};
      Interceptor[] interceptors = { IsLocalProxyFactoryInterceptor.singleton, 
                                     ClusterChooserInterceptor.singleton, 
                                     InvokeRemoteInterceptor.singleton };
      
      ClusteredPojiProxy proxy = new ClusteredPojiProxy(proxyFactoryRegistryKey, locator, interceptors, wrapper, 
                                                        loadBalancePolicy, partitionName, null);
      
      return  (ProxyFactory) Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, proxy);
   }

   // --------------------------------------------------------------------------------||
   // Private ------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   private FamilyWrapper getFamilyWrapper(Name name, Map<String, List<String>> refAddrs) throws MalformedURLException
   {
      String familyName = this.getSingleRequiredReferenceAddressValue(name, refAddrs,
            ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_FAMILY_NAME);
      
      assert familyName != null && !familyName.trim().equals("") : " Cluster family name is required, but is not specified; improperly bound reference in JNDI";
      
      List<InvokerLocator> targets = new ArrayList<InvokerLocator>();
      List<String> targetRefAddrs = refAddrs.get(ClusteredProxyFactoryReferenceAddressTypes.REF_ADDR_TYPE_CLUSTER_TARGET_INVOKER_LOCATOR_URL);
      if (targetRefAddrs != null)
      {
         for (String url : targetRefAddrs)
         {
            targets.add(new InvokerLocator(url));
         }
      }
      
      log.debug("Creating " + FamilyWrapper.class.getSimpleName() + " for family " + 
                familyName + " using targets " + targets);
      return new FamilyWrapper(familyName, targets);
   }
   
}
