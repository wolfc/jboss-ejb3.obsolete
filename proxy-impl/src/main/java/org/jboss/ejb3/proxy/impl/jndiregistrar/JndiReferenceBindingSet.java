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

package org.jboss.ejb3.proxy.impl.jndiregistrar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Encapsulates the {@link JndiReferenceBinding}s associated with a particular
 * container.
 * 
 * @author Brian Stansberry
 */
public class JndiReferenceBindingSet
{
   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   /** The naming context to use if there is a need to rebind */
   private Context context;
   /** Naming properties associated with context; used if context is not viable */
   private final Hashtable<?, ?> namingEnvironment;
   
   /** All bindings for remote homes */
   private Set<JndiReferenceBinding> homeRemoteBindings = new HashSet<JndiReferenceBinding>();     
   /** All bindings for default remotes */
   private Set<JndiReferenceBinding> defaultRemoteBindings = new HashSet<JndiReferenceBinding>();    
   /** All bindings for business remotes, grouped by business interface */
   private Map<String, Set<JndiReferenceBinding>> businessRemoteBindings = new HashMap<String, Set<JndiReferenceBinding>>();
   /** All bindings for local homes */
   private Set<JndiReferenceBinding> homeLocalBindings = new HashSet<JndiReferenceBinding>();     
   /** All bindings for default locals */
   private Set<JndiReferenceBinding> defaultLocalBindings = new HashSet<JndiReferenceBinding>();    
   /** All bindings for business locals, grouped by business interface */
   private Map<String, Set<JndiReferenceBinding>> businessLocalBindings = new HashMap<String, Set<JndiReferenceBinding>>();

   // --------------------------------------------------------------------------------||
   // Constructors -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   public JndiReferenceBindingSet(Context context)
   {
      assert context != null : "context is null";
      this.context = context;
      try
      {
         this.namingEnvironment = context.getEnvironment();
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Cannot retrieve naming environment from " + context);
      }
   }

   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||
   
   public Context getContext()
   {
      // FIXME pass the env properties through JndiSessionRegistrarBase.bindEjb
      // instead of passing the context. Then we cache those and recreate
      // the context via InitialContextFactory
      try
      {
         // A test of the viability of our cached context
         context.getEnvironment();
      }
      catch (NamingException ne)
      {
         try
         {
            context = new InitialContext(namingEnvironment);
         }
         catch (NamingException e)
         {
            throw new RuntimeException("Cannot create InitialContext from " + namingEnvironment);
         }
      }
      return context;
   }

   public Set<JndiReferenceBinding> getHomeRemoteBindings()
   {
      return new HashSet<JndiReferenceBinding>(homeRemoteBindings);
   }

   public void addHomeRemoteBinding(JndiReferenceBinding binding)
   {
      assert binding != null : "binding is null";
      this.homeRemoteBindings.add(binding);
   }

   public Set<JndiReferenceBinding> getDefaultRemoteBindings()
   {
      return new HashSet<JndiReferenceBinding>(defaultRemoteBindings);
   }

   public void addDefaultRemoteBinding(JndiReferenceBinding binding)
   {
      assert binding != null : "binding is null";
      this.defaultRemoteBindings.add(binding);
   }
   
   public Map<String, Set<JndiReferenceBinding>> getBusinessRemoteBindings()
   {
      return new HashMap<String, Set<JndiReferenceBinding>>(businessRemoteBindings);
   }
   
   public void addBusinessRemoteBinding(String businessInterfaceName, JndiReferenceBinding binding)
   {
      assert businessInterfaceName != null : "businessInterfaceName is null";
      assert binding != null : "binding is null";

      Set<JndiReferenceBinding> bindings = businessRemoteBindings.get(businessInterfaceName);
      if (bindings == null)
      {
         bindings = new HashSet<JndiReferenceBinding>();
         businessRemoteBindings.put(businessInterfaceName, bindings);
      }
      bindings.add(binding);
   }

   public Set<JndiReferenceBinding> getHomeLocalBindings()
   {
      return new HashSet<JndiReferenceBinding>(homeLocalBindings);
   }

   public void addHomeLocalBinding(JndiReferenceBinding binding)
   {
      assert binding != null : "binding is null";
      this.homeLocalBindings.add(binding);
   }

   public Set<JndiReferenceBinding> getDefaultLocalBindings()
   {
      return new HashSet<JndiReferenceBinding>(defaultLocalBindings);
   }

   public void addDefaultLocalBinding(JndiReferenceBinding binding)
   {
      assert binding != null : "binding is null";
      this.defaultLocalBindings.add(binding);
   }

   public Map<String, Set<JndiReferenceBinding>> getBusinessLocalBindings()
   {
      return new HashMap<String, Set<JndiReferenceBinding>>(businessLocalBindings);
   }

   public void addBusinessLocalBinding(String businessInterfaceName, JndiReferenceBinding binding)
   {
      assert businessInterfaceName != null : "businessInterfaceName is null";
      assert binding != null : "binding is null";

      Set<JndiReferenceBinding> bindings = businessLocalBindings.get(businessInterfaceName);
      if (bindings == null)
      {
         bindings = new HashSet<JndiReferenceBinding>();
         businessLocalBindings.put(businessInterfaceName, bindings);
      }
      bindings.add(binding);
   }
   
}
