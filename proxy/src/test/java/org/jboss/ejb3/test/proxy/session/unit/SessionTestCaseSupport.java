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
package org.jboss.ejb3.test.proxy.session.unit;

import static org.junit.Assert.fail;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;

import org.jboss.ejb3.test.proxy.common.SessionTestCaseBase;

/**
 * SessionTestCaseSupport
 * 
 * Extends SessionTestCaseBase to add support for
 * similarly-executed tests
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class SessionTestCaseSupport extends SessionTestCaseBase
{

   // --------------------------------------------------------------------------------||
   // Contracts ----------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Obtains the Context to be used for JNDI Operations 
    */
   protected abstract Context getNamingContext();

   // --------------------------------------------------------------------------------||
   // Test Support -------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Checks that a default business interface is not bound for the EJB w/ the
    * specified implementation class
    * 
    * EJBTHREE-1668
    * 
    * @param beanImplClass
    * @param isLocal
    * @throws Exception
    * @author Jaikiran Pai
    * @author ALR (Revised Jaikiran's patch)
    */
   protected void checkNoDefaultBusinessInterfaceBound(Class<?> beanImplClass, boolean isLocal) throws Exception
   {
      // A simple context.lookup("beanName/[jndiSuffix]"), should have been enough
      // to illustrate the issue. But a context.lookup throws a NullPointerException
      //
      // javax.naming.NamingException: Could not dereference object [Root exception is java.lang.NullPointerException]
      // at org.jnp.interfaces.NamingContext.getObjectInstanceWrapFailure(NamingContext.java:1337)
      //
      // and "errors" out the testcase. Not exactly what i wanted. So instead to get the
      // test case failing, let's just check whether the jndi-name is present
      NamingEnumeration<Binding> namingEnumeration = getNamingContext().listBindings(beanImplClass.getSimpleName());
      while (namingEnumeration.hasMore())
      {
         Binding binding = namingEnumeration.next();
         String suffix = isLocal ? "local" : "remote";
         if (binding.getName().equals(suffix))
         {
            // This jndi-name should not have been present, since we do not have a default local business interface
            // and assuming that no one intentionally bound something to this jndi-name
            fail("JNDI name " + beanImplClass.getSimpleName() + "/" + suffix
                  + " exists. Expected nothing to be bound with this jndi-name");
         }
      }
   }
}
