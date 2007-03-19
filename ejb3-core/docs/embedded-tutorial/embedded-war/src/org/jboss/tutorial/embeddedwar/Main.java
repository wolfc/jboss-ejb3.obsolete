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
package org.jboss.tutorial.embeddedwar;

import java.net.URL;
import java.util.Hashtable;
import javax.naming.InitialContext;
import org.jboss.ejb3.embedded.EJB3StandaloneDeployer;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;

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

      EJB3StandaloneDeployer deployer = new EJB3StandaloneDeployer();
      URL archive = getArchiveURL();
      deployer.getArchives().add(archive);

      // need to set the InitialContext properties that deployer will use
      // to initial EJB containers
      deployer.setJndiProperties(getInitialContextProperties());

      deployer.create();
      deployer.start();

      InitialContext ctx = getInitialContext();
      CustomerDAOLocal local = (CustomerDAOLocal)ctx.lookup("CustomerDAOBean/local");
      CustomerDAORemote remote = (CustomerDAORemote)ctx.lookup("CustomerDAOBean/remote");

      System.out.println("----------------------------------------------------------");
      System.out.println("This is the archive deployed from: ");
      System.out.print("    ");
      System.out.println(archive);

      int id = local.createCustomer("Gavin");
      Customer cust = local.findCustomer(id);
      System.out.println("Successfully created and found Gavin from @Local interface");

      id = remote.createCustomer("Emmanuel");
      cust = remote.findCustomer(id);
      System.out.println("Successfully created and found Emmanuel from @Remote interface");
      System.out.println("----------------------------------------------------------");

      deployer.stop();
      deployer.destroy();
   }

   public static URL getArchiveURL() throws Exception
   {
      // Usually you would hardcode your URL.  This is just a way to make it easier for the tutorial
      // code to configure where the resource is.
      URL res = Thread.currentThread().getContextClassLoader().getResource("META-INF/persistence.xml");
      return EJB3StandaloneDeployer.getContainingUrlFromResource(res, "META-INF/persistence.xml");
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
