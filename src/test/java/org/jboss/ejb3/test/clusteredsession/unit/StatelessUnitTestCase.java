/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb3.test.clusteredsession.unit;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.ejb3.test.clusteredsession.NodeAnswer;
import org.jboss.ejb3.test.clusteredsession.ClusteredStatelessRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Properties;

import junit.framework.Test;

/**
 * Test slsb for load-balancing behaviour and others.
 *
 * @author  Ben.Wang@jboss.org
 * @version $Revision$
 */
public class StatelessUnitTestCase extends JBossClusteredTestCase
{
   static boolean deployed = false;
   public static int test = 0;
   static Date startDate = new Date();

   protected final String namingFactory =
   System.getProperty(Context.INITIAL_CONTEXT_FACTORY);

   protected final String providerURL =
   System.getProperty(Context.PROVIDER_URL);

   public StatelessUnitTestCase (String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      final String jarName = "clusteredsession-test.jar";
      return JBossClusteredTestCase.getDeploySetup(StatelessUnitTestCase.class,
              jarName);
   }

   protected InitialContext getInitialContext(int node) throws Exception {
      // Connect to the server0 JNDI
      String[] urls = getNamingURLs();
      Properties env1 = new Properties();
      env1.setProperty(Context.INITIAL_CONTEXT_FACTORY,
         "org.jnp.interfaces.NamingContextFactory");
      env1.setProperty(Context.PROVIDER_URL, urls[node]);
      return new InitialContext(env1);
   }

   public void testLoadbalance()
   throws Exception
   {
      getLog().debug(++StatelessUnitTestCase.test +"- "+"Trying the context...");

      InitialContext ctx = getInitialContext(0);

      getLog().debug("Test Stateless Bean loadbalancing");
      getLog().debug("==================================");
      getLog().debug(++StatelessUnitTestCase.test +"- "
              +"Looking up clusteredStateless/remote...");
      ClusteredStatelessRemote stateless = (ClusteredStatelessRemote) ctx.lookup("clusteredStateless/remote");

      NodeAnswer node1 = stateless.getNodeState ();
      getLog ().debug ("Node 1 ID: " +node1);

      stateless = (ClusteredStatelessRemote) ctx.lookup("clusteredStateless/remote");
      NodeAnswer node2 = stateless.getNodeState ();
      getLog ().debug ("Node 2 ID : " +node2);

      assertFalse(node1.nodeId.equals(node2.nodeId));
   }
}
