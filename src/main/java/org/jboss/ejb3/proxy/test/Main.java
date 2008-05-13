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

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.naming.Util;


/**
 * Main
 *
 * Test Class for Proof of Concept
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Main
{

   private Context ctx;

   private static final String BIND_LOCATION_OBJECT_FACTORY = "/remote";

   private static final String BIND_LOCATION_REFERENCE = "AllBusinessInterfaces";

   private static final String BIND_LOCATION_SECOND_REF = "OneInterface";

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      Main main = new Main();
      try
      {
         main.bindObjectFactory();
         main.bindLinkRefs();
         main.lookupReference();
         //main.unbindAll();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }

   public Main()
   {

      Properties properties = new Properties();
      properties.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      properties.put("java.naming.provider.url", "jnp://localhost:1099");
      properties.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      try
      {
         this.ctx = new InitialContext(properties);
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   private void bindObjectFactory() throws Exception
   {
      // Bind an object factory as a reference
      RefAddr refAddr1 = new StringRefAddr("Business Interface Proxy", "org.jboss.ejb3.BusinessInterface1");
      RefAddr refAddr2 = new StringRefAddr("Business Interface Proxy", "org.jboss.ejb3.BusinessInterface2");
      Reference objRef = new Reference(
            "Proxy for org.jboss.ejb3.BusinessInterface1, org.jboss.ejb3.BusinessInterface2", refAddr1,
            ProxyObjectFactory.class.getName(), null);
      objRef.add(refAddr2);
      Util.rebind(this.ctx, Main.BIND_LOCATION_OBJECT_FACTORY, objRef);
   }

   private void bindLinkRefs() throws Exception
   {
      RefAddr refAddr1 = new StringRefAddr("org.jboss.ejb3.BusinessInterface1", "org.jboss.ejb3.BusinessInterface1");
      RefAddr refAddr2 = new StringRefAddr("org.jboss.ejb3.BusinessInterface2", "org.jboss.ejb3.BusinessInterface2");
      LinkRef ref = new LinkRef(Main.BIND_LOCATION_OBJECT_FACTORY);
      ref.add(refAddr1);
      ref.add(refAddr2);
      Util.rebind(this.ctx, Main.BIND_LOCATION_REFERENCE, ref);

      Reference ref2 = new LinkRef(Main.BIND_LOCATION_OBJECT_FACTORY);
      RefAddr refAddr3 = new StringRefAddr("org.jboss.ejb3.BusinessInterface2", "org.jboss.ejb3.BusinessInterface2");
      ref2.add(refAddr3);
      Util.rebind(this.ctx, Main.BIND_LOCATION_SECOND_REF, ref2);
   }

//   private void unbindAll() throws Exception
//   {
//      Util.unbind(this.ctx, Main.BIND_LOCATION_REFERENCE);
//      Util.unbind(this.ctx, Main.BIND_LOCATION_OBJECT_FACTORY);
//   }

   private void lookupReference() throws Exception
   {
      ctx.lookup(Main.BIND_LOCATION_SECOND_REF);
      ctx.lookup(Main.BIND_LOCATION_SECOND_REF);
      ctx.lookup(Main.BIND_LOCATION_SECOND_REF);
      String s = (String) ctx.lookup(Main.BIND_LOCATION_SECOND_REF);
      if (!s.equals(ProxyObjectFactory.EXPECTED_OBJ))
      {
         throw new RuntimeException("Found \"" + s + "\" where \"" + ProxyObjectFactory.EXPECTED_OBJ + "\" expected.");
      }
      System.out.println("Found expected result \"" + s + "\" from JNDI at " + Main.BIND_LOCATION_SECOND_REF);
   }

}
