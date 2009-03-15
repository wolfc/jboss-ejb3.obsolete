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
package org.jboss.ejb3.test.proxy.impl.session.unit;

import org.jboss.ejb3.proxy.spi.intf.SessionProxy;
import org.jboss.ejb3.test.proxy.impl.common.SessionTestCaseBase;
import org.jboss.logging.Logger;
import org.junit.Test;

/**
 * SessionProxyContractTestCase
 *
 * Tests to ensure that the SessionProxy contract remains
 * backwards-compatible.  Note that we don't need to test anything 
 * (which is implementation), just compile-time.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class SessionProxyContractTestCase extends SessionTestCaseBase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(SessionProxyContractTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Ensures that the compile-time contracts are in place, performs no real runtime
    * tests/checks
    */
   @Test
   @SuppressWarnings("null")
   public void testSessionProxyContract() throws Throwable
   {
      SessionProxy proxy = null;

      try
      {
         proxy.getTarget();
      }
      catch (NullPointerException npe)
      {
         // Swallow
      }
      try
      {
         proxy.setTarget(null);
      }
      catch (NullPointerException npe)
      {
         // Swallow
      }
      try
      {
         proxy.removeTarget();
      }
      catch (NullPointerException npe)
      {
         // Swallow
      }

      log.info(SessionProxy.class.getName() + " contracts OK");
   }
}
