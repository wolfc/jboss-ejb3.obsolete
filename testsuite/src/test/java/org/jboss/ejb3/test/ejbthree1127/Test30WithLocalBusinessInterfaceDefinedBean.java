/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1127;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

/**
 * A Test EJB with a 3.0 View denoted only by a Local Business interface
 * 
 * @author <a href="mailto:andrew.rubinger@redhat.com">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Local(TestLocalBusiness.class)
public class Test30WithLocalBusinessInterfaceDefinedBean implements TestLocalBusiness
{
   // Class Members
   private static final Logger log = Logger.getLogger(Test30WithLocalBusinessInterfaceDefinedBean.class);

   // Required Implementations

   public int test()
   {
      log.info("Called");
      return TestLocalBusiness.RETURN_VALUE;
   }
}
