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
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBs;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
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
@Stateless(name="CallerFacadeBean-testRunAsSFSB")
@Remote(org.jboss.ejb3.test.security.CalledSession.class)
@RemoteBinding(jndiBinding = "spec.CallerFacadeBean-testRunAsSFSB")
@SecurityDomain("spec-test")
@RolesAllowed({"Echo"})
@EJBs({@EJB(name="CalledSessionLocal", beanInterface=org.jboss.ejb3.test.security.CalledSessionLocal.class, beanName="CallerFacadeTargetSFSB")})
public class CallerFacadeBean 
{
   private static Logger log = Logger.getLogger(CallerFacadeBean.class);
   @Resource SessionContext sessionContext;

   /**
    * This method calls echo on a StatelessSessionLocal and asserts that the
    * caller is in the EchoCaller role.
    */
   public String invokeEcho(String arg)
   {
      log.debug("echo, arg=" + arg);
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal=" + p);
      boolean isEchoCaller = sessionContext.isCallerInRole("EchoCaller");
      log.debug("echo, isCallerInRole('EchoCaller')=" + isEchoCaller);
      boolean isInternalRole = sessionContext.isCallerInRole("InternalRole");
      log.debug("echo, isCallerInRole('InternalRole')=" + isInternalRole);

      try
      {
         InitialContext ic = new InitialContext();
         CalledSessionLocal localBean = (CalledSessionLocal)ic.lookup(Container.ENC_CTX_NAME + "/env/CalledSessionLocal");
         String echo2 = localBean.invokeEcho(arg);
         log.debug("echo#1, callee.invokeEcho=" + echo2);
         echo2 = localBean.invokeEcho(arg);
         log.debug("echo#2, callee.invokeEcho=" + echo2);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         log.error("Failed to invoke CalledSession.invokeEcho", e);
         throw new EJBException("Failed to invoke Callee.echo", e);
      }
      return arg;
   }

   /**
    * This method should call invokeEcho on another CalledSession
    */
   public void callEcho()
   {
      try
      {
         InitialContext ic = new InitialContext();
         CalledSessionLocal bean = (CalledSessionLocal)ic.lookup(Container.ENC_CTX_NAME + "/env/CalledSessionLocal");
         bean.callLocalEcho("callEcho#2");
         log.debug("echo, callee.callLocalEcho#1");
         bean.callLocalEcho("callEcho#2");
         log.debug("echo, callee.callLocalEcho#2");
      }
      catch (Exception e)
      {
         log.error("Failed to invoke Callee.invokeEcho", e);
         throw new EJBException("Failed to invoke Callee.invokeEcho", e);
      }

   }

   public void noop()
   {
      log.debug("noop");
   }

}
