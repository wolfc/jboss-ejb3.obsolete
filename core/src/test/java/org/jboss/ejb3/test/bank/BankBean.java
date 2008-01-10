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
package org.jboss.ejb3.test.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Init;
import javax.ejb.Remove;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.ejb3.Container;

/**
 * @see <related>
 * @author $Author$
 * @version $Revision$
 */
public class BankBean implements Bank, Serializable, javax.ejb.SessionSynchronization
{
   private static final Logger log = Logger.getLogger(BankBean.class);

   transient public DataSource customerDb;

   static final String ID = Container.ENC_CTX_NAME + "/env/org.jboss.ejb3.test.bank/id";

   @Resource String id;

   String customerId = "defaultId";

   static long nextAccountId = System.currentTimeMillis();

   static long nextCustomerId = System.currentTimeMillis();

   String initialized = "";

   private String activated = "";
   
   private String transactionState = "failed";
   private String rollbackState;
   private boolean beforeCalled = false;

   public String getId()
   {
      return id;
   }

   public String getEnvEntryId() throws RemoteException, NamingException
   {
      InitialContext jndiContext = new InitialContext();
      String value = (String)jndiContext.lookup(ID);
      return value;
   }

   public String createAccountId(Customer customer) throws RemoteException
   {
      return getId() + "." + customer.getName() + "." + (nextAccountId++);
   }

   public String createCustomerId()
   {
      return getId() + "." + (nextCustomerId++);
   }

   public void storeCustomerId(String customerId)
   {
      this.customerId = customerId;
   }

   public String retrieveCustomerId()
   {
      return customerId;
   }

   public String interceptCustomerId(String customerId)
   {
      return customerId;
   }

   public void testResource() throws Exception
   {
      if (customerDb == null) throw new Exception("customerDb resource not set");
      Connection connection = customerDb.getConnection();
      connection.close();
   }

   public void remove()
   {
   }

   @Init
   public void annotatedInit()
   {
      initialized += "YES";
   }

   public void init()
   {
      initialized += "YES";
   }

   public String isInitialized()
   {
      return initialized;
   }

   public String isActivated()
   {
      return activated;
   }
   
   public String getTransactionState()
   {
      return transactionState;
   }
   
   public void testTransactionTimeout()
   {
      try
      {
         Thread.sleep(2000);
         transactionState = "ok";
      } 
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void afterBegin() throws EJBException, RemoteException
   {
      rollbackState = transactionState;
   }

   public void beforeCompletion() throws EJBException, RemoteException
   {
      beforeCalled = true;
   }

   public void afterCompletion(boolean committed) throws EJBException, RemoteException
   {
      if (!committed)
         transactionState = rollbackState;  
   }
}

/*
 * $Id$ Currently locked
 * by:$Locker$ Revision: $Log$
 * by:$Locker:  $ Revision: Revision 1.17  2006/03/29 19:19:29  bdecoste
 * by:$Locker:  $ Revision: removed logging
 * by:$Locker:  $ Revision:
 * by:$Locker$ Revision: Revision 1.16  2006/03/29 02:03:35  bdecoste
 * by:$Locker$ Revision: injection for all bean types
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.15  2005/10/30 00:06:46  starksm
 * by:$Locker$ Revision: Update the jboss LGPL headers
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.14  2005/10/16 16:45:22  bdecoste
 * by:$Locker$ Revision: added SessionSynchronization
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.13  2005/10/13 19:14:42  bdecoste
 * by:$Locker$ Revision: added transaction timeouts via annotation or jboss.xml
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.12  2005/09/24 02:28:31  bill
 * by:$Locker$ Revision: injection compliance for ejb-refs and resource refs and env-entry
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.11  2005/09/14 02:24:04  bill
 * by:$Locker$ Revision: stateful bean should be serializable
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.10  2005/08/24 20:30:43  bdecoste
 * by:$Locker$ Revision: support for <env-entry>
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.9  2005/06/02 23:25:14  bdecoste
 * by:$Locker$ Revision: ejb3 jboss.xml support
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.8  2005/05/26 02:24:09  bdecoste
 * by:$Locker$ Revision: support for @Init
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.7  2005/05/25 18:57:19  bdecoste
 * by:$Locker$ Revision: added support for @Remove and @Exclude
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.6  2005/05/17 22:37:42  bdecoste
 * by:$Locker$ Revision: remove ejb2.1 rules
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.5  2005/05/14 20:54:02  bdecoste
 * by:$Locker$ Revision: added ejb3 dd support for Resource
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.4  2005/05/12 20:31:46  bdecoste
 * by:$Locker$ Revision: added ejb3 dd support for injection, callbacks, EJB
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.3  2005/05/12 01:46:06  bdecoste
 * by:$Locker$ Revision: added ejb3 dd support for RunAs and Inject
 * by:$Locker$ Revision:
 * by:$Locker$ Revision: Revision 1.2  2005/05/03 23:51:01  bdecoste
 * by:$Locker$ Revision: fixed formatting
 * by:$Locker$ Revision: Revision 1.1 2005/05/03
 * 20:35:11 bdecoste test for ejb3 deployment descriptors Revision 1.5
 * 2003/08/27 04:32:49 patriot1burke 4.0 rollback to 3.2 Revision 1.3 2002/02/15
 * 06:15:50 user57 o replaced most System.out usage with Log4j. should really
 * introduce some base classes to make this mess more maintainable... Revision
 * 1.2 2001/01/07 23:14:34 peter Trying to get JAAS to work within test suite.
 * Revision 1.1.1.1 2000/06/21 15:52:37 oberg Initial import of jBoss test. This
 * module contains CTS tests, some simple examples, and small bean suites.
 */
