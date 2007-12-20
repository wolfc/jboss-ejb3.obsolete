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
package org.jboss.ejb3.test.security;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;



/**
 * A simple session bean that calls the CalleeBean
 * @author Scott.Stark@jboss.org
 * @version $Revision: 67628 $
 */
@Stateless(name="CallerBean")
@Remote(CalledSession.class)
@RemoteBinding(jndiBinding = "spec.CallerBean")
@RunAs("InternalRole")
@RolesAllowed({"Echo"})
@SecurityDomain("spec-test")
public class CallerBean implements CalledSession
{
   private static Logger log = Logger.getLogger(CallerBean.class);
   @Resource  SessionContext sessionContext;

   /**
    * This method calls echo on a StatelessSessionLocal and asserts that the
    * caller is in the EchoCaller role.
    */
   public String invokeEcho(String arg)
   {
      log.info("echo, arg=" + arg);
      Principal p = sessionContext.getCallerPrincipal();
      log.info("echo, callerPrincipal=" + p);
      boolean isEchoCaller = sessionContext.isCallerInRole("EchoCaller");
      log.info("echo, isCallerInRole('EchoCaller')=" + isEchoCaller);
      boolean isInternalRole = sessionContext.isCallerInRole("InternalRole");
      log.info("echo, isCallerInRole('InternalRole')=" + isInternalRole);
      
      if (isEchoCaller == false && isInternalRole == false)
         throw new SecurityException("isEchoCaller == false && isInternalRole == false");
    
      try
      {
         InitialContext ic = new InitialContext();
     //    StatelessSessionLocal localBean = (StatelessSessionLocal)ic.lookup(Container.ENC_CTX_NAME + "/env/ejb/Callee");
         
         StatelessSessionLocal localBean = (StatelessSessionLocal)ic.lookup("spec.CalleeBeanRemote");
         
         String echo2 = localBean.echo(arg);
         log.info("echo#1, callee.echo=" + echo2);
     //    echo2 = localBean.echo(arg);
     //    log.info("echo#2, callee.echo=" + echo2);
      }
      catch (Exception e)
      {
         log.error("Failed to invoke Callee.echo", e);
         throw new EJBException("Failed to invoke Callee.echo", e);
      }

      isEchoCaller = sessionContext.isCallerInRole("EchoCaller");
      log.info("echo, isCallerInRole#2('EchoCaller')=" + isEchoCaller);
      isInternalRole = sessionContext.isCallerInRole("InternalRole");
      log.info("echo, isCallerInRole#2('InternalRole')=" + isInternalRole);

      if (isEchoCaller == false && isInternalRole == false)
         throw new SecurityException("isEchoCaller == false && isInternalRole == false post calls");
      
      return arg;
   }

   /**
    * This method should call invokeEcho on another CalledSession
    */
   public String callEcho()
   {
      try
      {
         InitialContext ic = new InitialContext();
       
         CalledSession bean = (CalledSession)ic.lookup(Container.ENC_CTX_NAME + "/env/ejb/Caller2");
         String echo = bean.invokeEcho("Level1");
         log.info("echo, callee.invokeEcho=" + echo);
         
         String principal = sessionContext.getCallerPrincipal().getName();
         return principal;
      }
      catch (Exception e)
      {
         log.error("Failed to invoke Callee.invokeEcho", e);
         throw new EJBException("Failed to invoke Callee.invokeEcho", e);
      }

   }

   /**
    * This method should call invokeEcho on a CalledSession
    */
   public String callLocalEcho(String arg)
   {
      try
      {
         InitialContext ic = new InitialContext();
         Context enc = (Context) ic.lookup("java:comp/env");
         CalledSessionLocal bean = (CalledSessionLocal)enc.lookup("ejb/Caller");
         String echo2 = bean.invokeEcho(arg + "Level1");
         log.info("echo, callee.invokeEcho=" + echo2);
         return echo2;
      }
      catch (Exception e)
      {
         log.error("Failed to invoke Callee.invokeEcho", e);
         throw new EJBException("Failed to invoke Callee.invokeEcho", e);
      }
   }

   public void noop()
   {
      log.info("noop");
   }

}
