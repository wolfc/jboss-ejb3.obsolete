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
package org.jboss.ejb3.stateless;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.ObjectStreamField;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

import org.jboss.ejb3.InitialContextFactory;
import org.jboss.naming.NamingContextFactory;

/**
 * An EJB stateless session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class StatelessHandleImpl
      implements Handle
{
   /** Serial Version Identifier. */
   static final long serialVersionUID = 3811452873535097661L;
   private static final ObjectStreamField[] serialPersistentFields =
      new ObjectStreamField[]
   {
      new ObjectStreamField("jndiName", String.class),
      new ObjectStreamField("jndiEnv", Hashtable.class)
   };

   /** The JNDI name of the home inteface binding */
   public String jndiName;
   /** The JNDI env in effect when the home handle was created */
   private Hashtable jndiEnv;

   // Constructors --------------------------------------------------

   public StatelessHandleImpl()
   {
      
   }
   
   /**
    * Construct a <tt>StatelessHandleImpl</tt>.
    *
    * @param handle    The initial context handle that will be used
    *                  to restore the naming context or null to use
    *                  a fresh InitialContext object.
    * @param name      JNDI name.
    */
   public StatelessHandleImpl(String jndiName)
   {
      this.jndiName = jndiName;
      this.jndiEnv = (Hashtable) NamingContextFactory.lastInitialContextEnv.get();
   }

   // Public --------------------------------------------------------

   public EJBObject getEJBObject() throws RemoteException
   {
      try
      {
         InitialContext ic = InitialContextFactory.getInitialContext(jndiEnv);
    
         Proxy proxy = (Proxy) ic.lookup(jndiName);

         return (EJBObject) proxy;
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         throw new RemoteException("Error during getEJBObject", t);
      }
   }

   /**
    * @return the jndi name
    */
   public String getJNDIName()
   {
      return jndiName;
   }

   private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException
   {
      ObjectInputStream.GetField getField = ois.readFields();
      jndiName = (String) getField.get("jndiName", null);
      jndiEnv = (Hashtable) getField.get("jndiEnv", null);
   }

   private void writeObject(ObjectOutputStream oos)
      throws IOException
   {
      ObjectOutputStream.PutField putField = oos.putFields();
      putField.put("jndiName", jndiName);
      putField.put("jndiEnv", jndiEnv);
      oos.writeFields();
   }
}
