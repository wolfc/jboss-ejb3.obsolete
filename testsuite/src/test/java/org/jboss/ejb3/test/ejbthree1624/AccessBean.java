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
package org.jboss.ejb3.test.ejbthree1624;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.common.registrar.spi.Ejb3Registrar;
import org.jboss.ejb3.common.registrar.spi.Ejb3RegistrarLocator;
import org.jboss.logging.Logger;

/**
 * AccessBean
 * 
 * An EJB to act as a proxy to the underlying MC Bean
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Remote(AccessRemoteBusiness.class)
public class AccessBean implements AccessRemoteBusiness
{
   // --------------------------------------------------------------------------------||
   // Functional Methods -------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(AccessBean.class);

   // --------------------------------------------------------------------------------||
   // Required Implementations -------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.test.ejbthreexxx.CalculatorBusiness#add(int[])
    */
   public int add(int... args)
   {
      // Initialize
      Ejb3Registrar registrar = Ejb3RegistrarLocator.locateRegistrar();
      String beanBindName = "testMcBean";

      // Make an POJO and install into MC
      McBean bean = new McBean();
      registrar.rebind(beanBindName, bean);

      // Get the POJO from MC
      McBean beanFromMc = registrar.lookup(beanBindName, McBean.class);

      // Invoke
      int result = beanFromMc.add(1, 2, 3);
      log.debug("Got result from MC Bean: " + result);

      // Unbind MC Bean
      registrar.unbind(beanBindName);

      // Return
      return result;
   }

}
