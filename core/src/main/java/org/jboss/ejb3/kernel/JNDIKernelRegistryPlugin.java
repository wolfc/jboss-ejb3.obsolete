/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.kernel;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.ejb3.InitialContextFactory;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryPlugin;
import org.jboss.logging.Logger;

/**
 * A kernel registry plugin which checks for JNDI names.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author <a href="mailto:ajustin@redhat.com">Ales Justin</a>
 * @version $Revision$
 */
public class JNDIKernelRegistryPlugin implements KernelRegistryPlugin
{
   private static final Logger log = Logger.getLogger(JNDIKernelRegistryPlugin.class);

   public static final String JNDI_DEPENDENCY_PREFIX = "jndi:";

   private Context context;

   private Hashtable<?, ?> environment;

   public JNDIKernelRegistryPlugin()
   {
   }

   public JNDIKernelRegistryPlugin(Hashtable environment)
   {
      this.environment = environment;
   }

   public void create() throws NamingException
   {
      log.debug("Creating JNDIKernelRegistryPlugin");
      this.context = InitialContextFactory.getInitialContext(environment);
   }

   public void destroy() throws NamingException
   {
      log.debug("Destroying JNDIKernelRegistryPlugin");
      if (context != null)
         context.close();
      context = null;
   }

   /**
    * @see KernelRegistryPlugin#getEntry(Object)
    */
   public KernelRegistryEntry getEntry(Object name)
   {
      if (name == null)
      {
         // as per the KernelRegistryPlugin interface, we should throw
         // IllegalArgumentException when name is null
         throw new IllegalArgumentException("Name cannot be null");
      }

      String s = String.valueOf(name);
      if (!s.startsWith(JNDI_DEPENDENCY_PREFIX))
         return null;

      if (log.isTraceEnabled())
         log.trace("get entry for " + name);

      String jndiName = s.substring(JNDI_DEPENDENCY_PREFIX.length());
      return new LazyJNDIKernelRegistryEntry(this.context, jndiName);
   }

   public void setEnvironment(Hashtable<?, ?> env)
   {
      if (context != null)
         throw new IllegalStateException("context already initialized");
      this.environment = env;
   }
}
