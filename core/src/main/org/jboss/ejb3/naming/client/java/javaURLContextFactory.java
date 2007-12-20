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
package org.jboss.ejb3.naming.client.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.jboss.corba.ORBFactory;
import org.jboss.ejb3.naming.BrainlessContext;
import org.jboss.naming.client.java.HandleDelegateFactory;

/**
 * Creates objects for in the java:comp namespace.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class javaURLContextFactory implements ObjectFactory
{

   /* (non-Javadoc)
    * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
         throws Exception
   {
      if(nameCtx == null)
         nameCtx = new InitialContext(environment); 
      final Context lookupCtx = nameCtx;
      return new BrainlessContext() 
      {
         @Override
         public Object lookup(Name name) throws NamingException
         {
            if (name.size() > 0 && "java:comp".equals(name.get(0)))
            {
               if (name.size() == 2 && "ORB".equals(name.get(1)))
                  return ORBFactory.getORB();
               else if (name.size() == 2 && "HandleDelegate".equals(name.get(1)))
                  return HandleDelegateFactory.getHandleDelegateSingleton();
            }
            throw new NameNotFoundException(name.toString());
         }
         
         @Override
         public Object lookup(String name) throws NamingException
         {
            NameParser parser = lookupCtx.getNameParser("");
            return lookup(parser.parse(name));
         }
      };
   }

}
