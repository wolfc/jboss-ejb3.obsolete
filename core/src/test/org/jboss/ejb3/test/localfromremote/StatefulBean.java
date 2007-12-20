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
package org.jboss.ejb3.test.localfromremote;

import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateful;

import javax.naming.InitialContext;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateful
@Local(StatefulLocal.class)
@LocalHome(StatefulLocalHome.class)
@Remote(StatefulRemote.class)
@RemoteHome(StatefulRemoteHome.class)
public class StatefulBean 
{
   private static final Logger log = Logger.getLogger(StatefulBean.class);
   
   private static int methodCount = 0;
   private static int homeMethodCount = 0;
   
   public void localCall() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      StatelessLocal stateless = (StatelessLocal)jndiContext.lookup("StatelessBean/local");
      log.info("*** calling Local remotely ...  " + jndiContext.getEnvironment());
      stateless.method();   
   }
   
   public void localHomeCall() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      StatelessLocalHome statelessHome = (StatelessLocalHome)jndiContext.lookup("StatelessBean/localHome");
      StatelessLocal stateless = statelessHome.create();
      log.info("*** calling LocalHome remotely ...  " + jndiContext.getEnvironment());
      stateless.homeMethod();   
   }
   
   public int remoteCall() throws Exception
   {
      ++methodCount;
      InitialContext jndiContext = new InitialContext();
      StatefulRemote stateful = (StatefulRemote)jndiContext.lookup("StatefulBean/remote");
      log.info("*** calling Remote ... " + jndiContext.getEnvironment());
      return stateful.method();
   }
   
   public int remoteHomeCall() throws Exception
   {
      ++homeMethodCount;
      InitialContext jndiContext = new InitialContext();
      StatefulRemoteHome statefulHome = (StatefulRemoteHome)jndiContext.lookup("StatefulBean/home");
      StatefulRemote stateful = statefulHome.create();
      log.info("*** calling RemoteHome ... " + jndiContext.getEnvironment());
      return stateful.homeMethod();
   }
   
   public int method() throws Exception
   {
      ++methodCount;
      log.info("*** method called " + methodCount);
      return methodCount;
   }
   
   public int homeMethod() throws Exception
   {
      ++homeMethodCount;
      log.info("*** homeMethod called " + homeMethodCount);
      return homeMethodCount;
   }
}
