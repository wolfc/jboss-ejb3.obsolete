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
package org.jboss.ejb3.test.bank.unit;

import javax.ejb.EJBAccessException;
import javax.ejb.NoSuchEJBException;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.ClientKernelAbstraction;
import org.jboss.ejb3.KernelAbstractionFactory;
import org.jboss.ejb3.test.bank.Bank;
import org.jboss.ejb3.test.bank.Bank21;
import org.jboss.ejb3.test.bank.BankHome;
import org.jboss.ejb3.test.bank.Teller;
import org.jboss.ejb3.test.bank.TellerInterceptor;
import org.jboss.ejb3.test.bank.TestStatus;
import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * Test for EJB3 deployment of EJB2.0 Bank EJBs
 * 
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class BankDeploymentDescriptorTestCase
    extends JBossTestCase {
 //   extends TestCase {

   private static final Logger log = Logger
         .getLogger(BankDeploymentDescriptorTestCase.class);

   public BankDeploymentDescriptorTestCase(String name)
   {
      super(name);
   }
   
   public void testEnvEntry() throws Exception
   {
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());
      */
      InitialContext jndiContext = new InitialContext();
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      String id = bank.getEnvEntryId();
      assertEquals(id, "5678");
      sc.logout();
   }

   public void testStatelessTeller() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("rolefail"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());*/
 
      String greeting;
      Teller teller = (Teller) jndiContext.lookup(Teller.JNDI_NAME);
      assertNotNull(teller);
      
      greeting = teller.greetWithRequiredTransaction("Hello");
      assertNotNull(greeting);
      assertEquals("Hello", greeting);
      greeting = teller.greetWithNotSupportedTransaction("Hello");
      assertNotNull(greeting);
      assertEquals("Hello", greeting);
      greeting = teller.greetUnchecked("Hello");
      assertNotNull(greeting);
      assertEquals("Hello", greeting);
      
      try {
         greeting = teller.greetChecked("Hello");
         assertTrue(false);
      } catch (Exception e){
         assertTrue(e instanceof EJBAccessException);
      }
      
      sc.setSimple(new SimplePrincipal("customer"), "password".toCharArray());
      sc.login();
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("customer"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      try{
         greeting = teller.greetChecked("Hello");
         assertNotNull(greeting);
         assertEquals("Hello", greeting);
      } catch (Exception e){
         e.printStackTrace();
      }
   }

   public void testInjectionAnnotations() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Teller teller = (Teller) jndiContext.lookup(Teller.JNDI_NAME);
      assertNotNull(teller);
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("customer"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("customer"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      String greeting = teller.greetChecked("Hello");
      assertNotNull(greeting);
      assertEquals("Hello", greeting);
      assertTrue(teller.isConstructed());

   }

   public void testFieldInject() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
 
      Teller teller = (Teller) jndiContext.lookup(Teller.JNDI_NAME);
      assertNotNull(teller);
      
      String greeting = teller.greetWithServiceTimer("Hello");
      assertEquals("Hello", greeting);
   }
   
   public void testRunAs() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("rolefail"));
      SecurityAssociation.setCredential("password".toCharArray());*/
 
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("rolefail"), "password".toCharArray());
      sc.login();
      
      Teller teller = (Teller) jndiContext.lookup(Teller.JNDI_NAME);
      assertNotNull(teller);
      
      String tmpId = teller.retrieveCustomerId();
      assertEquals("defaultId", tmpId);
   }

   public void testStatefulBank() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      String customerId = "CustomerId";
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("customer"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("customer"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      try {
         bank.storeCustomerId(customerId);
         assertTrue(false);
      } catch (Exception e){
         assertTrue(e instanceof EJBAccessException);
      }
      
      sc.logout();
      
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      bank.storeCustomerId(customerId);
      String tmpId = bank.retrieveCustomerId();
      assertEquals(customerId, tmpId);
      sc.logout();
   }
   
   public void testStatefulState() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      String customerId = "CustomerId";
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      bank.storeCustomerId(customerId);
      String tmpId = bank.retrieveCustomerId();
      assertEquals(customerId, tmpId);
      
      bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      tmpId = bank.retrieveCustomerId();
      assertEquals("defaultId", tmpId);
      sc.logout();
   }
 
   public void testStatefulBank21() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      BankHome home = (BankHome) jndiContext.lookup(Bank.JNDI_NAME + "21");
      assertNotNull(home);
      Bank21 bank = home.create();
      assertNotNull(bank);
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      String activated = bank.isActivated();
      assertEquals(activated, "_CREATED");
   }
 
   public void testCallbackListenersAndInteceptors() throws Exception
   {
      TestStatus status = (TestStatus) getInitialContext().lookup("TestStatusBean/remote");
      status.clear();
      
      InitialContext jndiContext = new InitialContext();
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      String id = bank.interceptCustomerId("CustomerId");
      log.debug("id=" + id);
      assertEquals("CustomerId_SecondInterceptor_FirstInterceptor", id);
      assertTrue(status.postConstruct());
   }

   public void testResource() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      bank.testResource();
   }
   
   public void testRemove() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      bank.remove();
      
      try {
         bank.testResource();
         assertTrue(false);
      }
      catch (NoSuchEJBException e)
      {
         // correct exception
      }
   }
   
   public void testInit() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      String initialized = bank.isInitialized();
      assertEquals("YESYES", initialized);
   }
   
   public void testTeller() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Teller teller = (Teller) jndiContext.lookup(Teller.JNDI_NAME);
      assertNotNull(teller);
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("customer"), "password".toCharArray());
      sc.login(); 
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("customer"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      try {
         teller.excludedMethod();
         assertTrue(false);
      } catch (Exception e){
         assertTrue(e instanceof EJBAccessException);
      }
   }
   
   public void testRemoteBindingInterceptorStack() throws Exception
   {
      Teller teller = (Teller)getInitialContext().lookup(Teller.JNDI_NAME);
      assertNotNull(teller);
      assertTrue(TellerInterceptor.accessed);
   }
   
   public void testTransactionTimeout() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("customer"), "password".toCharArray());
      sc.login(); 
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("customer"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      Teller teller = (Teller) jndiContext.lookup(Teller.JNDI_NAME);
      assertNotNull(teller);

      boolean exceptionThrown = false;
      try
      {
         teller.testTransactionTimeout();
      }
      catch (Exception e)
      {
         exceptionThrown = true;
      }
      assertTrue(exceptionThrown);

   }
   
   public void testStatefulTransactionTimeout() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      
      SecurityClient sc = SecurityClientFactory.getSecurityClient();
      sc.setSimple(new SimplePrincipal("teller"), "password".toCharArray());
      sc.login();
      
      /*SecurityAssociation.setPrincipal(new SimplePrincipal("teller"));
      SecurityAssociation.setCredential("password".toCharArray());*/
      
      Bank bank = (Bank) jndiContext.lookup(Bank.JNDI_NAME);
      assertNotNull(bank);
      
      try{
         bank.testTransactionTimeout();
         String state = bank.getTransactionState();
         assertEquals("failed", state);
      } catch (Exception e){
      }
   }

   public void testSessionContextForEjb21() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      BankHome home = (BankHome) jndiContext.lookup(Bank.JNDI_NAME + "21");
      assertNotNull(home);
      Bank21 bank = home.create();
      assertNotNull(bank);
      assertTrue("setSessionContext(ctx) should have been invoked", bank.hasSessionContext());
   }

   public static Test suite() throws Exception
   {
      ClientKernelAbstraction kernel = KernelAbstractionFactory.getClientInstance();
      ObjectName propertiesServiceON = new ObjectName("jboss:type=Service,name=SystemProperties");
      kernel.invoke(
            propertiesServiceON,
            "set",
            new Object[]{"test.datasource.jndi","java:/DefaultDS"},
            new String[]{"java.lang.String", "java.lang.String"}
      );
      
      kernel.invoke(
            propertiesServiceON,
            "set",
            new Object[]{"test.transactionmanager.jndi","java:/TransactionManager"},
            new String[]{"java.lang.String", "java.lang.String"}
      );
      
      return getDeploySetup(BankDeploymentDescriptorTestCase.class, "bank.jar");
   }

}
 