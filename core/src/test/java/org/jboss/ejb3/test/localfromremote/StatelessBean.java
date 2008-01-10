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
import javax.ejb.Stateless;

import javax.naming.InitialContext;

import javax.ejb.Remote;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless
@Local(StatelessLocal.class)
@LocalHome(StatelessLocalHome.class)
@Remote(StatelessRemote.class)
@RemoteHome(StatelessRemoteHome.class)
public class StatelessBean 
{
   private static final Logger log = Logger.getLogger(StatelessBean.class);
  
   private static int methodCount = 0;
   private static int homeMethodCount = 0;
   
   public void localCall() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      StatefulLocal stateful = (StatefulLocal)jndiContext.lookup("StatefulBean/local");
      log.info("*** calling Local remotely ... " + jndiContext.getEnvironment());
      stateful.method();
   }
   
   public void localHomeCall() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      StatefulLocalHome statefulHome = (StatefulLocalHome)jndiContext.lookup("StatefulBean/localHome");
      StatefulLocal stateful = statefulHome.create();
      log.info("*** calling LocalHome remotely ... " + jndiContext.getEnvironment());
      stateful.homeMethod();
   }
   
   public int remoteCall() throws Exception
   {
      ++methodCount;
      InitialContext jndiContext = new InitialContext();
      StatelessRemote stateless = (StatelessRemote)jndiContext.lookup("StatelessBean/remote");
      log.info("*** calling Remote ... " + jndiContext.getEnvironment());
      return stateless.method();
   }
   
   public int remoteHomeCall() throws Exception
   {
      ++homeMethodCount;
      InitialContext jndiContext = new InitialContext();
      StatelessRemoteHome statelessHome = (StatelessRemoteHome)jndiContext.lookup("StatelessBean/home");
      StatelessRemote stateless = statelessHome.create();
      return stateless.homeMethod();
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
