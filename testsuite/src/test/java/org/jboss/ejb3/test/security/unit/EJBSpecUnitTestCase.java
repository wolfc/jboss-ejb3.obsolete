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
package org.jboss.ejb3.test.security.unit;

import java.util.HashSet;

import javax.ejb.EJBException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import junit.framework.Test;

import org.jboss.ejb3.test.security.AppCallbackHandler;
import org.jboss.ejb3.test.security.CalledSession;
import org.jboss.ejb3.test.security.SecurityContext;
import org.jboss.ejb3.test.security.SessionFacade;
import org.jboss.ejb3.test.security.StatefulSession;
import org.jboss.ejb3.test.security.StatelessSession;
import org.jboss.logging.Logger;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase;


/** Test of EJB spec conformace using the security-spec.jar
 deployment unit. These test the basic role based access model.
 
 @author Scott.Stark@jboss.org
 @version $Revision$
 */
public class EJBSpecUnitTestCase
extends JBossTestCase
{
private static final Logger log = Logger.getLogger(EJBSpecUnitTestCase.class);

static String username = "scott";
static char[] password = "echoman".toCharArray();
static String QUEUE_FACTORY = "ConnectionFactory";

LoginContext lc;
boolean loggedIn;

public EJBSpecUnitTestCase(String name)
{
   super(name);
}

protected void tearDown() throws Exception
{
   logout();
}

/** Test that:
 1. SecureBean returns a non-null principal when getCallerPrincipal
 is called with a security context and that this is propagated
 to its Entity bean ref.
 
 2. UnsecureBean throws an IllegalStateException when getCallerPrincipal
 is called without a security context.
 */
public void testGetCallerPrincipal() throws Exception
{   
   log.debug("+++ testGetCallerPrincipal()");
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.UnsecureStatelessSession2");
   log.debug("Created spec.UnsecureStatelessSession2");
   
   try
   {
      // This should fail because echo calls getCallerPrincipal()
      bean.echo("Hello from nobody?");
      fail("Was able to call StatelessSession.echo");
   }
   catch(Exception e)
   {
      log.debug("echo failed as expected");
   }
   
   login();
   bean = (StatelessSession)getInitialContext().lookup("spec.StatelessSession2");
   log.debug("Created spec.StatelessSession2");
   // Test that the Entity bean sees username as its principal
   String echo = bean.echo(username);
   log.debug("bean.echo(username) = "+echo);
   assertTrue("username == echo", echo.equals(username));
   
   logout();
}

/** Test the use of getCallerPrincipal from within the ejbCreate
 * in a stateful session bean
 */
public void testStatefulCreateCaller() throws Exception
{
   log.debug("+++ testStatefulCreateCaller");
   login();
   InitialContext jndiContext = new InitialContext();
  
   StatefulSession bean = (StatefulSession)jndiContext.lookup("spec.StatefulSession");
   // Need to invoke a method to ensure an ejbCreate call
   bean.echo("testStatefulCreateCaller");
   log.debug("Bean.echo(), ok");

   logout();
}

/**
 * Test that a call interacting with different security domains does not
 * change the 
 * @throws Exception
 */ 
public void testDomainInteraction() throws Exception
{
   logout();
   login("testDomainInteraction", "testDomainInteraction".toCharArray());
   log.debug("+++ testDomainInteraction()");
   SecurityContext bean = (SecurityContext)getInitialContext().lookup("spec.UserInRoleContextSession");
   log.debug("Created spec.UserInRoleContextSession");
   HashSet roles = new HashSet();
   roles.add("Role1");
   roles.add("Role2");
   try
   {
      bean.testDomainInteraction(roles);
   }
   catch(EJBException e)
   {
      Throwable cause = e.getCause();
      if(cause != null && cause instanceof SecurityException)
      {
         cause.printStackTrace();
         fail(cause.getMessage());
      }
      throw e;
   }
}

/** Test that the calling principal is propagated across bean calls.
 */
public void testPrincipalPropagation() throws Exception
{
   log.debug("+++ testPrincipalPropagation");
   logout();
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.UnsecureStatelessSession2");
   log.debug("Created spec.UnsecureStatelessSession2");
   log.debug("Bean.forward('testPrincipalPropagation') -> "+bean.forward("testPrincipalPropagation"));
}

public void testMethodAccess() throws Exception
{
   log.debug("+++ testMethodAccess");
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.StatelessSession");
   log.debug("Created spec.StatelessSession");
   log.debug("Bean.echo('Hello') -> "+bean.echo("Hello"));

   // This should be allowed in ejb3
   bean.noop();
}

/** Test that the echo method is accessible by an Echo
 role. Since the excluded() method of the StatelessSession
 bean has been placed into the excluded set it should not
 accessible by any user. This uses the security domain of the
 JaasSecurityDomain service to test its use as an authentication mgr.
 */
public void testDomainMethodAccess() throws Exception
{
   log.debug("+++ testDomainMethodAccess");
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.StatelessSessionInDomain");
   log.debug("Created spec.StatelessSessionInDomain");
   log.debug("Bean.echo('testDomainMethodAccess') -> "+bean.echo("testDomainMethodAccess"));

   try
   {
      // This should not be allowed
      bean.excluded();
      fail("Was able to call StatelessSession.excluded");
   }
   catch(Exception e)
   {
      log.debug("StatelessSession.excluded failed as expected");
   }
}

/** Test that the permissions assigned to the stateless session bean:
 with ejb-name=org/jboss/test/security/ejb/StatelessSession_test
 are read correctly.
 */
public void testMethodAccess2() throws Exception
{
   log.debug("+++ testMethodAccess2");
   login();
   InitialContext jndiContext = new InitialContext();
   StatelessSession bean = (StatelessSession)jndiContext.lookup("spec.StatelessSession_test");
   log.debug("Created spec.StatelessSession_test");
   log.debug("Bean.echo('testMethodAccess2') -> "+bean.echo("testMethodAccess2"));
}

/** Test a user with Echo and EchoLocal roles can access the CalleeBean
 through its local interface by calling the CallerBean and that a user
 with only a EchoLocal cannot call the CallerBean.
 */
public void a1testLocalMethodAccess() throws Exception
{
   log.debug("+++ testLocalMethodAccess");
   login();
   InitialContext jndiContext = new InitialContext();
   CalledSession bean = (CalledSession)jndiContext.lookup("spec.CallerBean");
   log.debug("Created spec.CallerBean");
   log.debug("Bean.invokeEcho('testLocalMethodAccess') -> "+bean.invokeEcho("testLocalMethodAccess"));
}

/** Test access to a bean with a mix of remote interface permissions and
 * unchecked permissions with the unchecked permissions declared first.
 * @throws Exception
 */ 
public void testUncheckedRemote() throws Exception
{
   log.debug("+++ testUncheckedRemote");
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.UncheckedSessionRemoteLast");
   log.debug("Created spec.UncheckedSessionRemoteLast");
   log.debug("Bean.echo('testUncheckedRemote') -> "+bean.echo("testUncheckedRemote"));
   try
   {
      bean.excluded();
      fail("Was able to call UncheckedSessionRemoteLast.excluded");
   }
   catch(Exception e)
   {
      log.debug("UncheckedSessionRemoteLast.excluded failed as expected");         
   }
   logout();
}

/** Test access to a bean with a mix of remote interface permissions and
 * unchecked permissions with the unchecked permissions declared last.
 * @throws Exception
 */ 
public void testRemoteUnchecked() throws Exception
{
   log.debug("+++ testRemoteUnchecked");
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.UncheckedSessionRemoteFirst");
   log.debug("Created spec.UncheckedSessionRemoteFirst");
   log.debug("Bean.echo('testRemoteUnchecked') -> "+bean.echo("testRemoteUnchecked"));
   try
   {
      bean.excluded();
      fail("Was able to call UncheckedSessionRemoteFirst.excluded");
   }
   catch(Exception e)
   {
      log.debug("UncheckedSessionRemoteFirst.excluded failed as expected");         
   }
   logout();
}

/** Test that a user with a role that has not been assigned any
 method permissions in the ejb-jar descriptor is able to access a
 method that has been marked as unchecked.
 */
public void testUnchecked() throws Exception
{
   log.debug("+++ testUnchecked");
   // Login as scott to create the bean
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.StatelessSession");
   log.debug("Created spec.StatelessSession");
   // Logout and login back in as stark to test access to the unchecked method
   logout();
   login("stark", "javaman".toCharArray());
   bean.unchecked();
   log.debug("Called Bean.unchecked()");
   logout();
}

/** Test that a user with a valid role is able to access a
 bean for which all methods have been marked as unchecked.
 */
public void testUncheckedWithLogin() throws Exception
{
   log.debug("+++ testUncheckedWithLogin");
   // Login as scott to see that a user with roles is allowed access
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.UncheckedSession");
   log.debug("Created spec.StatelessSession");
   bean.unchecked();
   log.debug("Called Bean.unchecked()");
   logout();
}

/** Test that user scott who has the Echo role is not able to
 access the StatelessSession2.excluded method even though
 the Echo role has been granted access to all methods of
 StatelessSession2 to test that the excluded-list takes
 precendence over the method-permissions.
 */
public void testExcluded() throws Exception
{
   log.debug("+++ testExcluded");
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.StatelessSession2");
   log.debug("Created spec.StatelessSession2");
   try
   {
      bean.excluded();
      fail("Was able to call Bean.excluded()");
   }
   catch(Exception e)
   {
      log.debug("Bean.excluded() failed as expected");
      // This is what we expect
   }
   logout();
}

/** This method tests the following call chains:
 1. RunAsStatelessSession.echo() -> PrivateEntity.echo()
 2. RunAsStatelessSession.noop() -> RunAsStatelessSession.excluded()
 3. RunAsStatelessSession.forward() -> StatelessSession.echo()
 1. Should succeed because the run-as identity of RunAsStatelessSession
 is valid for accessing PrivateEntity.
 2. Should succeed because the run-as identity of RunAsStatelessSession
 is valid for accessing RunAsStatelessSession.excluded().
 3. Should fail because the run-as identity of RunAsStatelessSession
 is not Echo.
 */
public void testRunAs() throws Exception
{
   log.debug("+++ testRunAs");
   login();
   StatelessSession bean = (StatelessSession)getInitialContext().lookup("spec.RunAsStatelessSession");
   log.debug("Created spec.RunAsStatelessSession");
   log.debug("Bean.echo('testRunAs') -> "+bean.echo("testRunAs"));
   bean.noop();
   log.debug("Bean.noop(), ok");
   
   try
   {
      // This should not be allowed
      bean.forward("Hello");
      fail("Was able to call RunAsStatelessSession.forward");
   }
   catch(Exception e)
   {
      log.debug("StatelessSession.forward failed as expected");
   }
   
   logout();
}

/** This method tests the following call chain:
 Level1CallerBean.callEcho() -> Level2CallerBean.invokeEcho()
   -> Level3CalleeBean.echo()
 The Level1CallerBean uses a run-as of InternalRole and the Level2CallerBean
 and Level3CalleeBean are only accessible by InternalRole.
 */   
public void testDeepRunAs() throws Exception
{
   log.debug("+++ testDeepRunAs");
   login();
   CalledSession bean = (CalledSession)getInitialContext().lookup("spec.Level1CallerBean");
   log.debug("Created spec.Level1CallerBean");
   String principal = bean.callEcho();
   assertEquals("scott", principal);
   log.debug("Bean.callEcho() ok");
}

public void testRunAsSFSB() throws Exception
{
   log.info("+++ testRunAsSFSB");
   login();
   log.debug("Found CallerFacadeBean-testRunAsSFSB Home");
   CalledSession bean = (CalledSession)getInitialContext().lookup("spec.CallerFacadeBean-testRunAsSFSB");
   log.debug("Created spec.CallerFacadeBean-testRunAsSFSB");
   bean.invokeEcho("testRunAsSFSB");
   log.debug("Bean.invokeEcho() ok");
}

/**
 * Test the run-as side-effects raised in 
 * http://jira.jboss.com/jira/browse/JBAS-1852
 * 
 * @throws Exception
 */ 
public void testJBAS1852() throws Exception
{
   log.info("+++ testJBAS1852");
   login();
   SessionFacade bean = (SessionFacade)getInitialContext().lookup("spec.PublicSessionFacade");
   log.debug("Created PublicSessionFacade");
   log.debug("Bean.callEcho('testJBAS1852') -> " + bean.callEcho("testJBAS1852"));
}

/** Test that an MDB with a run-as identity is able to access secure EJBs
 that require the identity.
 */
public void a1testMDBRunAs() throws Exception
{
   log.debug("+++ testMDBRunAs");
   logout();
   QueueConnectionFactory queueFactory = (QueueConnectionFactory) getInitialContext().lookup(QUEUE_FACTORY);
   Queue queA = (Queue) getInitialContext().lookup("queue/A");
   Queue queB = (Queue) getInitialContext().lookup("queue/B");
   QueueConnection queueConn = queueFactory.createQueueConnection();
   QueueSession session = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
   Message msg = session.createMessage();
   msg.setStringProperty("arg", "testMDBRunAs");
   msg.setJMSReplyTo(queB);
   QueueSender sender = session.createSender(queA);
   sender.send(msg);
   sender.close();
   log.debug("Sent msg to queue/A");
   queueConn.start();
   QueueReceiver recv = session.createReceiver(queB);
   msg = recv.receive(5000);
   log.debug("Recv msg: "+msg);
   String info = msg.getStringProperty("reply");
   recv.close();
   session.close();
   queueConn.close();

   if( info == null || info.startsWith("Failed") )
   {
      fail("Recevied exception reply, info="+info);
   }
}

/** Test that an MDB with a run-as identity is able to access secure EJBs
 that require the identity. DeepRunAsMDB -> Level1MDBCallerBean.callEcho() ->
   Level2CallerBean.invokeEcho() -> Level3CalleeBean.echo()
 The MDB uses a run-as of InternalRole and the Level2CallerBean
 and Level3CalleeBean are only accessible by InternalRole.
 */
public void a1testMDBDeepRunAs() throws Exception
{
   log.debug("+++ testMDBDeepRunAs");
   logout();
   QueueConnectionFactory queueFactory = (QueueConnectionFactory) getInitialContext().lookup(QUEUE_FACTORY);
   Queue queD = (Queue) getInitialContext().lookup("queue/D");
   Queue queB = (Queue) getInitialContext().lookup("queue/B");
   QueueConnection queueConn = queueFactory.createQueueConnection();
   QueueSession session = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
   Message msg = session.createMessage();
   msg.setStringProperty("arg", "testMDBDeepRunAs");
   msg.setJMSReplyTo(queB);
   QueueSender sender = session.createSender(queD);
   sender.send(msg);
   sender.close();
   log.debug("Sent msg to "+queD);
   queueConn.start();
   QueueReceiver recv = session.createReceiver(queB);
   msg = recv.receive(5000);
   log.debug("Recv msg: "+msg);
   String info = msg.getStringProperty("reply");
   recv.close();
   session.close();
   queueConn.close();

   if( info == null || info.startsWith("Failed") )
   {
      fail("Recevied exception reply, info="+info);
   }
}

/** This method tests that the RunAsWithRolesMDB is assigned multiple roles
 * within its onMessage so that it can call into the ProjRepository session
 * bean's methods that required ProjectAdmin, CreateFolder and DeleteFolder
 * roles.
 */
public void a1testRunAsWithRoles() throws Exception
{
   log.debug("+++ testRunAsWithRoles");
   logout();
   QueueConnectionFactory queueFactory = (QueueConnectionFactory) getInitialContext().lookup(QUEUE_FACTORY);
   Queue queC = (Queue) getInitialContext().lookup("queue/C");
   Queue queB = (Queue) getInitialContext().lookup("queue/B");
   QueueConnection queueConn = queueFactory.createQueueConnection();
   QueueSession session = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
   Message msg = session.createMessage();
   msg.setStringProperty("name", "testRunAsWithRoles");
   msg.setJMSReplyTo(queB);
   QueueSender sender = session.createSender(queC);
   sender.send(msg);
   sender.close();
   log.debug("Sent msg to queue/C");
   queueConn.start();
   QueueReceiver recv = session.createReceiver(queB);
   msg = recv.receive(5000);
   log.debug("Recv msg: "+msg);
   String info = msg.getStringProperty("reply");
   recv.close();
   session.close();
   queueConn.close();

   if( info == null || info.startsWith("Failed") )
   {
      fail("Recevied exception reply, info="+info);
   }
}

/** Login as user scott using the conf.name login config or
 'spec-test' if conf.name is not defined.
 */
private void login() throws Exception
{
   login(username, password);
}
private void login(String username, char[] password) throws Exception
{
   if( loggedIn )
      return;
   
   String confName = System.getProperty("conf.name", "spec-test");
   AppCallbackHandler handler = new AppCallbackHandler(username, password);
   log.debug("Creating LoginContext("+confName+")");
   lc = new LoginContext(confName, handler);
   lc.login();
   log.debug("Created LoginContext, subject="+lc.getSubject());
   loggedIn = true;
}
private void logout() throws Exception
{
   if( lc != null )
   {
      loggedIn = false;
      lc.logout();
      lc = null;
   }
}


/**
 * Setup the test suite.
 */
public static Test suite() throws Exception
{
   try {
      Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
      return getDeploySetup(EJBSpecUnitTestCase.class, "security-spec.sar,security.jar");
   }
   catch (Exception e)
   {
      e.printStackTrace();
      throw e;
   }
}
}
