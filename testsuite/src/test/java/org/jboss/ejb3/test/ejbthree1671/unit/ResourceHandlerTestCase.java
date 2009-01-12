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
package org.jboss.ejb3.test.ejbthree1671.unit;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree1671.StatelessOne;
import org.jboss.ejb3.test.ejbthree1671.StatelessOneImpl;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * ResourceHandlerTestCase
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ResourceHandlerTestCase extends JBossTestCase
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(ResourceHandlerTestCase.class);

   /**
    * Constructor
    * @param name
    */
   public ResourceHandlerTestCase(String name)
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
      return getDeploySetup(ResourceHandlerTestCase.class, "ejbthree1671.ear");
   }

   /**
    * A lookup and bean method invocation should be enough to
    * ensure that the bean was deployed successfully
    * 
    * @throws Throwable
    */
   public void testResourceInjectionOfApplicationSpecificType() throws Throwable
   {
      Context ctx = new InitialContext();
      logger.debug("Looking up the bean with jndi-name " + StatelessOneImpl.JNDI_NAME);

      StatelessOne statelessOne = (StatelessOne) ctx.lookup(StatelessOneImpl.JNDI_NAME);
      logger
            .info("Bean successfully returned from JNDI, which effectively means the application was successfully deployed");

      logger.debug("Now invoking a method on the returned bean");
      statelessOne.doNothing();
      logger.info("Successfully invoked a method on the bean");
   }

}
