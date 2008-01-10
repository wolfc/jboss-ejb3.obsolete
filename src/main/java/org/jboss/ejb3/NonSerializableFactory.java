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

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import org.jboss.naming.Util;

/**
 * A utility class that allows one to bind a non-serializable object into a
 * local JNDI context. The binding will only be valid for the lifetime of the
 * VM in which the JNDI InitialContext lives. An example usage code snippet is:
 * <p/>
 * Internally, there is a static map that is keyed based on Context identityMap and the atom name of the target
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>.
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @version $Revision$
 * @see javax.naming.spi.ObjectFactory
 * @see #rebind(javax.naming.Context, String, Object)
 */
public class NonSerializableFactory implements ObjectFactory
{
   private static Map wrapperMap = Collections.synchronizedMap(new HashMap());

   public static void unbind(Context ctx, String strName) throws NamingException
   {
      Name name = ctx.getNameParser("").parse(strName);
      int size = name.size();
      String atom = name.get(size - 1);
      Context parentCtx = Util.createSubcontext(ctx, name.getPrefix(size - 1));
      String key = parentCtx.getNameInNamespace() + "/" + atom;
      wrapperMap.remove(key);
      Util.unbind(ctx, strName);

   }

   public static void rebind(Context ctx, String strName, Object value) throws javax.naming.NamingException
   {
      Name name = ctx.getNameParser("").parse(strName);
      int size = name.size();
      String atom = name.get(size - 1);
      Context parentCtx = Util.createSubcontext(ctx, name.getPrefix(size - 1));
      String key = parentCtx.getNameInNamespace() + "/" + atom;
      wrapperMap.put(key, value);
      String className = value.getClass().getName();
      String factory = NonSerializableFactory.class.getName();
      StringRefAddr addr = new StringRefAddr("nns", key);
      Reference memoryRef = new Reference(className, addr, factory, null);
      parentCtx.rebind(atom, memoryRef);
   }

   public static void bind(Context ctx, String strName, Object value) throws javax.naming.NamingException
   {
      Name name = ctx.getNameParser("").parse(strName);
      int size = name.size();
      String atom = name.get(size - 1);
      Context parentCtx = Util.createSubcontext(ctx, name.getPrefix(size - 1));
      String key = parentCtx.getNameInNamespace() + "/" + atom;
      wrapperMap.put(key, value);
      String className = value.getClass().getName();
      String factory = NonSerializableFactory.class.getName();
      StringRefAddr addr = new StringRefAddr("nns", key);
      Reference memoryRef = new Reference(className, addr, factory, null);
      
      parentCtx.bind(atom, memoryRef);
   }

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env)
           throws Exception
   {	// Get the nns value from the Reference obj and use it as the map key
      Reference ref = (Reference) obj;
      RefAddr addr = ref.get("nns");
      String key = (String) addr.getContent();
      return wrapperMap.get(key);
   }
// --- End ObjectFactory interface methods
}
