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

import java.rmi.*;
import java.sql.Connection;

import javax.naming.*;
import javax.ejb.Init;
import javax.ejb.SessionContext;
import javax.sql.DataSource;

import org.jboss.logging.Logger;

/**
 * @see <related>
 * @author $Author$
 * @version $Revision$
 */
public class BankBean21 implements javax.ejb.SessionBean
{
   private static final Logger log = Logger.getLogger(BankBean21.class);
   
   public DataSource customerDb;
   
   static final String ID = "java:comp/env/id";

   String id;

   String customerId = "defaultId";

   static long nextAccountId = System.currentTimeMillis();

   static long nextCustomerId = System.currentTimeMillis();
   
   String initialized = "";
   
   private String activated = "";

   public String getId()
   {
      return id;
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
   
   public void ejbCreate()
   {
      activated += "_CREATED";
   }
   
   public void ejbActivate()
   {
      activated += "_ACTIVATED";
   }
   
   public void ejbPassivate()
   {
      
   }
   
   public void ejbRemove()
   {
      
   }
   
   public void setSessionContext(SessionContext context)
   {
      
   }
}

/*
 * $Id$ Currently locked
 * by:$Locker$ Revision: $Log$
 * by:$Locker:  $ Revision: Revision 1.2  2005/10/30 00:06:46  starksm
 * by:$Locker:  $ Revision: Update the jboss LGPL headers
 * by:$Locker:  $ Revision:
 * by:$Locker$ Revision: Revision 1.1  2005/06/02 23:25:14  bdecoste
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
