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

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

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
    * Returns a lazy entry {@link LazyJNDIKernelRegistryEntry} corresponding
    * to the passed name, if:
    * <ol>
    * <li>the name starts with {@link #JNDI_DEPENDENCY_PREFIX}</li>
    * <li>AND the name is bound in JNDI</li>
    * </ol>
    * Note that to check whether the name is bound in JNDI, "lookup"
    * is NOT done.
    * 
    * If the name is not bound then returns null (MC "implies" this contract)
    * 
    * @see KernelRegistryPlugin#getEntry(Object)
    * 
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
      try
      {
         if (isBoundInJNDI(jndiName))
         {
            if (log.isTraceEnabled())
            {
               log.trace("Found in jndi " + jndiName + "for MC name " + name);
            }
            // bound in jndi, so return a Lazy entry
            return new LazyJNDIKernelRegistryEntry(this.context,jndiName);
         }
         if (log.isTraceEnabled())
         {
            log.trace("Not available in jndi " + jndiName + " for MC name " + name);
         }
         // not bound in JNDI, return null
         return null;
      }
      catch (NamingException ne)
      {

         log.error("Can't resolve JNDI name " + jndiName + " for MC name " + name);
         throw new RuntimeException("Can't resolve JNDI name " + jndiName + " for MC name " + name, ne);
      }

      
   }

   public void setEnvironment(Hashtable<?, ?> env)
   {
      if (context != null)
         throw new IllegalStateException("context already initialized");
      this.environment = env;
   }

   /**
    * Checks whether the passed <code>jndiName</code> is available
    * in JNDI
    * 
    * @param jndiName The name to check
    * @return Returns true if the name is available in JNDI. Else returns false
    * @throws NamingException
    */
   private boolean isBoundInJNDI(String jndiName) throws NamingException
   {
      NameParser nameParser = this.context.getNameParser(jndiName);
      Name nameInJNDI = nameParser.parse(jndiName);
      if (nameInJNDI.isEmpty())
      {
         return false;
      }
      // Number of components in the Name
      int numberOfComponents = nameInJNDI.size();
      
      // If it's a just a simple name without any context/subcontexts 
      // (ex: SimpleBean), then we need to just travese the bindings
      // and look for a match
      if (numberOfComponents == 1)
      {
         NamingEnumeration<Binding> bindings = this.context.listBindings("");
         while (bindings.hasMoreElements())
         {
            Binding binding = bindings.nextElement();
            // compare the binding with the name
            if (binding.getName().equals(nameInJNDI.get(0)))
            {
               // match found in JNDI
               return true;
            }
         }
         return false;
      }
      if (numberOfComponents > 1)
      {
         // if the Name consists of context/subcontexts
         // (ex: MyApp/SimpleBean/remote) then we need list the 
         // bindings for the parent context (in this example,
         // MyApp/SimpleBean) and then compare the bindings in that
         // context with the "atom" of this Name
         Name parentCtx = nameInJNDI.getPrefix(numberOfComponents - 1);
         String atom = nameInJNDI.get(numberOfComponents - 1);
         try
         {
            // list the bindings of the parent context
            NamingEnumeration<Binding> bindings = this.context.listBindings(parentCtx);
            while (bindings.hasMoreElements())
            {
               // compare the "atom" with the binding
               Binding binding = bindings.nextElement();
               if (binding.getName().equals(atom))
               {
                  // match, found in JNDI
                  return true;
               }
            }

         }
         catch (NotContextException nce)
         {
            if (log.isTraceEnabled())
            {
               log.trace("Not found in JNDI " + jndiName + " since " + parentCtx + " is not bound");
            }
            // this means, that the parent context (MyApp/SimpleBean) is not
            // present. So effectively there's no such name bound to JNDI.
            return false;
         } 
         catch (NameNotFoundException nnfe)
         {
            if (log.isTraceEnabled())
            {
               log.trace("Not found in JNDI " + jndiName + " since sub-context(s) within parentCtx " + parentCtx + " is not bound");
            }
            // this means, some sub context wasn't available, effectively
            // the jndiName isn't bound in JNDI
            return false;
         }
      }
      // no match, so return false
      return false;
   }
}
