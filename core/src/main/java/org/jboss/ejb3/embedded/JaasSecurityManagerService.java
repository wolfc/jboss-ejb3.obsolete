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
package org.jboss.ejb3.embedded;


import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.naming.BrainlessContext;
import org.jboss.logging.Logger;
import org.jboss.security.auth.callback.SecurityAssociationHandler;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.plugins.JBossAuthorizationManager;
import org.jboss.security.plugins.JaasSecurityManager;
import org.jboss.security.plugins.SecurityDomainContext;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author carlo
 */
public class JaasSecurityManagerService
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(JaasSecurityManager.class);

   private static final String SECURITY_MGR_PATH = "java:/jaas";

   private static Map<String, JaasSecurityManager> cache = new HashMap<String, JaasSecurityManager>();

   private Hashtable initialContextProperties;

   public JaasSecurityManagerService()
   {

   }

   public void setInitialContextProperties(Hashtable initialContextProperties)
   {
      this.initialContextProperties = initialContextProperties;
   }

   private InitialContext getInitialContext() throws NamingException
   {
      return InitialContextFactory.getInitialContext(initialContextProperties);
   }

   public void start() throws Exception
   {
      XMLLoginConfigImpl configuration = new XMLLoginConfigImpl();
      configuration.setConfigResource("login-config.xml");
      configuration.loadConfig();

      Configuration.setConfiguration(configuration);

      Context ctx = getInitialContext();

      String factoryName = SecurityDomainObjectFactory.class.getName();
      Reference ref = new Reference("nl.wolfc.embedded.security.plugins.JaasSecurityManager", factoryName, null);
      ctx.rebind(SECURITY_MGR_PATH, ref);
   }

   private static JaasSecurityManager getSecurityManager(String name)
   {
      JaasSecurityManager manager = cache.get(name);
      if (manager != null)
      {
         //log.info("cache hit");
         return manager;
      }
      synchronized (cache)
      {
         if (manager != null)
            return manager;

         manager = new JaasSecurityManager(name, new SecurityAssociationHandler());
         cache.put(name, manager);
      }
      return manager;
   }

   public static class SecurityDomainObjectFactory implements ObjectFactory
   {
      @SuppressWarnings("unused")
      private static final Logger log = Logger.getLogger(SecurityDomainObjectFactory.class);
      
      public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
      {
         /**
          * The lookup will be of the form java:/jaas/other
          * So check for name.get(1)
          */
         String securityDomainName = name.get(1);
         final SecurityDomainContext ctx = new SecurityDomainContext(getSecurityManager(securityDomainName), null);
         ctx.setAuthorizationManager(new JBossAuthorizationManager(securityDomainName, new SecurityAssociationHandler()));
         return new BrainlessContext()
         {
            public Object lookup(Name name) throws NamingException
            {
               log.debug("lookup " + name);
               if(name.size() < 2)
                  return lookup(name.get(0));
               else
                  return ctx.lookup(name.get(1));
            }

            public Object lookup(String name) throws NamingException
            {
               log.debug("lookup " + name);
               return getSecurityManager(name);
            }
         };
      }
   }
}
