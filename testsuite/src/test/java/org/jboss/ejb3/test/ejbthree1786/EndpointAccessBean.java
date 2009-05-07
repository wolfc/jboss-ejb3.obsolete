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
package org.jboss.ejb3.test.ejbthree1786;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.common.deployers.spi.Ejb3DeployerUtils;
import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.endpoint.SessionFactory;
import org.jboss.ejb3.endpoint.deployers.EndpointResolver;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * EndpointAccessBean
 * 
 * A test SLSB which delegates to an {@link Endpoint}
 * and exposes a remote view of Endpoint operations upon a test SFSB
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(EndpointAccessRemoteBusiness.class)
@RemoteBinding(jndiBinding = EndpointAccessRemoteBusiness.JNDI_NAME)
public class EndpointAccessBean implements EndpointAccessRemoteBusiness, Endpoint
{

   //------------------------------------------------------------------------||
   // Class Members ---------------------------------------------------------||
   //------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EndpointAccessBean.class);

   /**
    * @deprecated EJBIdentifyier should be injected, but there's no hook to MC here
    */
   @Deprecated
   private static final String MC_BIND_NAME_ENDPOINT_RESOLVER = "EJB3EndpointResolver";

   private static final String EJB_NAME_TEST_SFSB = StatefulBean.class.getSimpleName();

   //------------------------------------------------------------------------||
   // Instance Members ------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * The underlying endpoint
    */
   private Endpoint endpoint;

   private EndpointResolver resolver;

   /**
    * For the dependency only
    */
   @EJB
   @SuppressWarnings("unused")
   private StatefulLocalBusiness dependency;

   //------------------------------------------------------------------------||
   // Lifecycle -------------------------------------------------------------||
   //------------------------------------------------------------------------||
   /**
    * Obtains the endpoint from MC
    */
   @PostConstruct
   public void initialize() throws Exception
   {
      // Get the registrar (hook to MC)
      @Deprecated
      Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();

      // Get the resolver
      //TODO Inject instead of lookup
      resolver = registrar.lookup(MC_BIND_NAME_ENDPOINT_RESOLVER, EndpointResolver.class);
      log.info("Got " + EndpointResolver.class.getSimpleName() + ": " + resolver);

      // Hack to get at the DU
      // Get the Container
      final Set<DeploymentUnit> allDeployments = Ejb3DeployerUtils.getAllEjb3DeploymentUnitsInMainDeployer();
      final String targetEjbName = EJB_NAME_TEST_SFSB;
      DeploymentUnit testSfsfDu = null;
      if (allDeployments != null)
      {
         for (DeploymentUnit du : allDeployments)
         {
            // Get out the EJB3 metadata
            final JBossMetaData metadata = du.getAttachment(JBossMetaData.class);
            if (metadata == null || !metadata.isEJB3x())
            {
               // Keep looking
               continue;
            }

            // Look for the EJB by name
            log.info("Querying " + DeploymentUnit.class.getSimpleName() + ": " + du + " for EJB of name "
                  + targetEjbName);
            if (metadata.getEnterpriseBean(targetEjbName) != null)
            {
               testSfsfDu = du;
               log.info("Got " + DeploymentUnit.class.getSimpleName() + ": " + testSfsfDu);
               break;
            }
         }
      }
      if (testSfsfDu == null)
      {
         throw new RuntimeException("Could not locate the test SFSB Deployment unit containing EJB with name: "
               + targetEjbName);
      }

      // Get the endpoint
      final String endpointMcBindName = resolver.resolve(testSfsfDu, EJB_NAME_TEST_SFSB);

      // Real usage will inject instead of lookup
      endpoint = registrar.lookup(endpointMcBindName, Endpoint.class);
      log.info("Got " + Endpoint.class.getSimpleName() + ": " + endpoint);

   }

   //------------------------------------------------------------------------||
   // Required Implementations ----------------------------------------------||
   //------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.endpoint.Endpoint#getSessionFactory()
    */
   public SessionFactory getSessionFactory() throws IllegalStateException
   {
      // TODO Auto-generated method stub
      return endpoint.getSessionFactory();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.endpoint.Endpoint#invoke(java.io.Serializable, java.lang.Class, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke(Serializable session, Class<?> invokedBusinessInterface, Method method, Object[] args)
         throws Throwable
   {
      log.info("invoking: " + invokedBusinessInterface + "");
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.endpoint.Endpoint#isSessionAware()
    */
   public boolean isSessionAware()
   {
      log.info("Invoked isSessionAware");
      return endpoint.isSessionAware();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1786.EndpointAccessRemoteBusiness#createSession()
    */
   public Serializable createSession()
   {
      log.info("Attempting to create session...");
      Serializable id = endpoint.getSessionFactory().createSession(null, null);
      log.info("Created session with ID: " + id);
      return id;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthree1786.EndpointAccessRemoteBusiness#destroySession(java.io.Serializable)
    */
   public void destroySession(Serializable id)
   {
      log.info("Attempting to destroy session with ID: " + id);
      endpoint.getSessionFactory().destroySession(id);
      log.info("Session with ID: " + id + " destroyed.");
   }

}
