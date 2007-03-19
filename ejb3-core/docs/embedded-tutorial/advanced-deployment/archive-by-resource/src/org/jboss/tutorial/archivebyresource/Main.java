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
package org.jboss.tutorial.archivebyresource;

import java.util.Hashtable;
import javax.naming.InitialContext;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.embedded.EJB3StandaloneDeployer;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Main
{
   public static void main(String[] args) throws Exception
   {
      EJB3StandaloneBootstrap.boot(null);

      EJB3StandaloneDeployer deployer = EJB3StandaloneBootstrap.createDeployer();
      deployer.getArchivesByResource().add("META-INF/persistence.xml");

      deployer.create();
      deployer.start();

      InitialContext ctx = getInitialContext();
      CustomerDAOLocal local = (CustomerDAOLocal)ctx.lookup("CustomerDAOBean/local");
      CustomerDAORemote remote = (CustomerDAORemote)ctx.lookup("CustomerDAOBean/remote");

      System.out.println("----------------------------------------------------------");
      System.out.println("We will search for all archives with META-INF/persistence.xml to deploy them.");
      System.out.println("    ");

      int id = local.createCustomer("Gavin");
      Customer cust = local.findCustomer(id);
      System.out.println("Successfully created and found Gavin from @Local interface");

      id = remote.createCustomer("Emmanuel");
      cust = remote.findCustomer(id);
      System.out.println("Successfully created and found Emmanuel from @Remote interface");
      System.out.println("----------------------------------------------------------");

      deployer.stop();
      deployer.destroy();
      EJB3StandaloneBootstrap.shutdown();
   }

   public static InitialContext getInitialContext() throws Exception
   {
      Hashtable props = getInitialContextProperties();
      return new InitialContext(props);
   }

   private static Hashtable getInitialContextProperties()
   {
      Hashtable props = new Hashtable();
      props.put("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      return props;
   }
}
