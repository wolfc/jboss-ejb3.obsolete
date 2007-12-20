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
package org.jboss.ejb3.test.regression.ejbthree315.unit;

import javax.naming.InitialContext;
import org.jboss.ejb3.test.regression.ejbthree315.ServiceRemote;
import org.jboss.ejb3.test.regression.ejbthree315.StatefulRemote;
import org.jboss.ejb3.test.regression.ejbthree315.StatelessRemote;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 61136 $
 */
public class UnderscoreInMethodNameTestCase
extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   public UnderscoreInMethodNameTestCase(String name)
   {
      super(name);
   }

   public void testUnderscoreMethods() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatefulRemote stateful = (StatefulRemote)ctx.lookup("StatefulBean/remote");
      stateful._method();
      
      StatelessRemote stateless = (StatelessRemote)ctx.lookup("StatelessBean/remote");
      stateless._method();
      
      ServiceRemote service = (ServiceRemote)ctx.lookup("ServiceBean/remote");
      service._method();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(UnderscoreInMethodNameTestCase.class, "regression-ejbthree315.jar");
   }

}