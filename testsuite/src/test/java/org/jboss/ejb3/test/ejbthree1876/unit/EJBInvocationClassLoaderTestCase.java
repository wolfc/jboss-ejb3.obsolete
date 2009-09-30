/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.ejb3.test.ejbthree1876.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1876.ResultTracker;
import org.jboss.ejb3.test.ejbthree1876.SimpleSLSBean;
import org.jboss.ejb3.test.ejbthree1876.StatelessRemote;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * EJBInvocationClassLoaderTestCase
 *
 * Test case to test the fix for EJBTHREE-1876 issue (also JBAS-6314)
 *
 * The issue involved a Quartz integrated MDB, in a isolated deployment, which when
 * using a injected bean would throw CNFE for the bean/bean-interfaces.
 * The real issue was because the Quartz scheduler thread did not have the
 * correct classloader while working with the EJB containers and other
 * components. More details here http://www.jboss.org/index.html?module=bb&op=viewtopic&t=159011
 *
 *
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJBInvocationClassLoaderTestCase extends JBossTestCase
{

   private static Logger logger = Logger.getLogger(EJBInvocationClassLoaderTestCase.class);

   /**
    * Constructor
    * @param name
    */
   public EJBInvocationClassLoaderTestCase(String name)
   {
      super(name);

   }

   /**
    *
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(EJBInvocationClassLoaderTestCase.class, "ejbthree1876.ear");
   }

   /**
    * Ensure that the MDB is able to invoke a method on an injected bean. The MDB
    * is configured to receive timed invocations from a Quartz scheduler. On each
    * invocation, the MDB uses an injected SLSB to invoke a method on that bean.
    * The result (success/failure) is stored on a server side {@link ResultTracker}
    * which is then accessed through the same SLSB in this testcase.
    *
    * @throws Exception
    */
   public void testBeanInvocationThroughMDB() throws Exception
   {
      // ensure that the deployment went off fine
      serverFound();
      logger.debug("Successfully deployed the ejbthree1876.ear");
      
      // upon deployment, the MDB will be invoked by the quartz scheduler
      // every 2 seconds. So let's atleast wait for 3 seconds before testing
      // whether the MDB has been invoked. If the MDB was invoked, it internally
      // we call a quick SLSB method and then set a state for success/failure.
      // We just have to check the state here
      Thread.sleep(3000);
      Context ctx = new InitialContext();
      StatelessRemote bean = (StatelessRemote) ctx.lookup(SimpleSLSBean.JNDI_NAME);
      assertNull("Exception occured while invoking injected bean's method from MDB", bean.getFailureCause());
      assertEquals("MDB could not invoke the injected bean's method", ResultTracker.Result.SUCCESS, bean.getResult());
   }

}
