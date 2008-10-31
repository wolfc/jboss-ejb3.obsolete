/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.servicedependency;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * A Customer.
 * 
 * @author <a href="galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision$
 */
public class Customer
{

   /**
    * FIXME Comment this
    * 
    * @param args
    * @throws Exception 
    */
   public static void main(String[] args) throws Exception
   {
      Properties props = new Properties();
      props.setProperty("java.naming.factory.initial",
               "org.jnp.interfaces.NamingContextFactory");
      props.setProperty("java.naming.factory.url.pkgs",
               "org.jboss.naming:org.jnp.interfaces");
      props.setProperty("java.naming.provider.url", "localhost:1099");
      
      InitialContext ctx = new InitialContext(props);
      
      Object ejb= ctx.lookup("AccountBean/remote");
      Account account = (Account)PortableRemoteObject.narrow(ejb, Account.class);
      
      account.debit("galder", 10);
   }

}
