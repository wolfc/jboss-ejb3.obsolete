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
package org.jboss.injection;

import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.LinkRef;
import javax.naming.NamingException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class EjbEncInjector implements EncInjector
{
   private static final Logger log = Logger.getLogger(EjbEncInjector.class);
   
   private String ejbLink;
   private Class refClass;
   private String jndiName;
   private String error;
   private String encName;

   public EjbEncInjector(String name, String mappedName, String error)
   {
      this.jndiName = mappedName;
      this.error = error;
      this.encName = name;
   }

   public EjbEncInjector(String name, Class refClass, String ejbLink, String error)
   {
      this.refClass = refClass;
      this.ejbLink = ejbLink;
      this.error = error;
      this.encName = name;
      if (refClass == null && ejbLink == null)
         throw new RuntimeException("cannot have null refClass and ejbLink for encName: " + name);
   }


   public void inject(InjectionContainer container)
   {
      if (jndiName == null || jndiName.equals(""))
      {
         if (ejbLink != null && !"".equals(ejbLink))
         {
            jndiName = container.getEjbJndiName(ejbLink, refClass);
         }
         else
         {
            try
            {
               if (refClass != null)
               {
                  jndiName = container.getEjbJndiName(refClass);
               }
               else
               {
                  throw new RuntimeException("searching for @EJB" + encName + " has null refClass and null ejbLink.");
               }
            }
            catch (NameNotFoundException e)
            {
               throw new RuntimeException("could not resolve global JNDI name for " + error + " for container " + container.getIdentifier() + ": reference class: " + refClass.getName() + " ejbLink: " + ejbLink + " " + e.getMessage());
            }
         }
      }
      try
      {
         if (jndiName == null)
            throw new RuntimeException("Failed to populate ENC: " + encName + " global jndi name was null");
         log.debug(" " + encName + " --> " + jndiName);
         Context enc = container.getEnc();
         Util.rebind(enc, encName, new LinkRef(jndiName));
      }
      catch (NamingException e)
      {
         throw new RuntimeException("could not bind enc name '" +  encName + "' for " + error + " for container " + container.getIdentifier() + ": reference class: " + refClass.getName() + " ejbLink: " + ejbLink + " " + e.getMessage());
      }
   }
}
