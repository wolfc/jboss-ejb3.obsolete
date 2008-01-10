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
package org.jboss.ejb3;

import java.util.Hashtable;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

import org.jboss.mx.util.MBeanServerLocator;

import org.jboss.system.server.ServerConfig;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class InitialContextFactory
{
private static final Logger log = Logger.getLogger(InitialContextFactory.class);
   
   private static Properties props = null;
   private static Properties securityProperties = null;
   private static InitialContext baseInitialContext = null;
   private static Context haContext = null;
   private static Integer haJndiPort = null;
   
   public static InitialContext getInitialContext() throws NamingException
   {
      InitialContext jndiContext;
      
      if (props == null)
      {
         if (baseInitialContext == null)
         {
            baseInitialContext = new InitialContext();
         }
         jndiContext = baseInitialContext;
         
         try
         {
            jndiContext.getEnvironment();
         }
         catch (NamingException e)
         {
            jndiContext = baseInitialContext = new InitialContext();
         }      
      }
      else
      {
         if (securityProperties != null)
         {
            Properties combinedProps = new Properties();
            combinedProps.putAll(props);
            combinedProps.putAll(securityProperties);
            jndiContext = new InitialContext(combinedProps);
         }
         else
            jndiContext = new InitialContext(props);
      }
       
      return jndiContext;
   }
   
   public static InitialContext getInitialContext(Hashtable props)
      throws NamingException
   {
      InitialContext ctx = null;
      if (props != null)
      {
         ctx = new InitialContext(props);
      }
      else
      {
         ctx = getInitialContext();
      }
      return ctx;
   }
   
   public static Context getHAContext(Context ctx)
      throws NamingException
   {
      if (haContext == null)
      {
         Hashtable env = ctx.getEnvironment();
         Object providerUrl = env.get(Context.PROVIDER_URL);
         if (providerUrl == null)
         {          
            try
            {
               String bindAddress = System.getProperty(ServerConfig.SERVER_BIND_ADDRESS);
               env.put(Context.PROVIDER_URL, bindAddress + ":" + getHaJndiPort());
               InitialContext haCtx = new InitialContext(env);
               haCtx.bind("HA_TEST", null);
               haCtx.unbind("HA_TEST");
               haContext = haCtx;
            }
            catch (Exception e)
            {
               log.debug("Unable to access HA JNDI: " + e);
            }
         }
      }
      return haContext;
   }
   
   private static Integer getHaJndiPort() throws Exception
   {
      if (haJndiPort == null)
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName objectName = new ObjectName("jboss:service=HAJNDI");
         haJndiPort = (Integer)server.getAttribute(objectName, "Port");
      }
      return haJndiPort;
   }
   
   public static void setProperties(Properties properties)
   {
      props = properties;
   }
   
   public static void setSecurity(String user, String password)
   {
      if (props != null)
      {
         securityProperties = new Properties();
         securityProperties.put(Context.SECURITY_PRINCIPAL, user);
         securityProperties.put(Context.SECURITY_CREDENTIALS, password);
         securityProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
      }
   }
}

