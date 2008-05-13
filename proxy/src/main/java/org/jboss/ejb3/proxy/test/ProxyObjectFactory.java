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
package org.jboss.ejb3.proxy.test;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * ProxyObjectFactory
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ProxyObjectFactory implements ObjectFactory, Serializable
{
   private static final long serialVersionUID = 1L;
   public static final String EXPECTED_OBJ = "Expected";
   
   public ProxyObjectFactory()
   {
      
   }

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
         throws Exception
   {
      System.out.println(ProxyObjectFactory.class.getName() + " servicing request for " + name.toString());
      
//      Set<?> keys = environment.keySet();
//      for(Object key : keys)
//      {
//         System.out.println(key);
//      }
      
      System.out.println("Name: " + name.toString());
      System.out.println("Object: " + obj);
      Reference ref = (Reference)obj;
      
      Enumeration<RefAddr> refAddrs = ref.getAll();
      while(refAddrs.hasMoreElements())
      {
         RefAddr refAddr = refAddrs.nextElement();
         System.out.print("Type: "+refAddr.getType());
         System.out.println(", Content: " + refAddr.getContent());
      }
      
      System.out.println("");
      
      return ProxyObjectFactory.EXPECTED_OBJ;
   }

}
