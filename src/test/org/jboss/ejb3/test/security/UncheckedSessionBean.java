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
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;

/** A simple session bean for testing unchecked declarative security.

 @author Scott.Stark@jboss.org
 @version $Revision: 67628 $
 */
@Stateless(name="UncheckedSessionRemote")
@Remote(org.jboss.ejb3.test.security.StatelessSession.class)
@RemoteBinding(jndiBinding = "spec.UncheckedSession")
@SecurityDomain("spec-test")
@PermitAll
public class UncheckedSessionBean
{
   Logger log = Logger.getLogger(getClass());
   
   @Resource SessionContext sessionContext;

   @RolesAllowed({"Echo"})
   public String echo(String arg)
   {
      log.debug("echo, arg=" + arg);
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal=" + p);
      boolean isCaller = sessionContext.isCallerInRole("EchoCaller");
      log.debug("echo, isCallerInRole('EchoCaller')=" + isCaller);
      return arg;
   }

   public String forward(String echoArg)
   {
      log.debug("forward, echoArg=" + echoArg);
      return echo(echoArg);
   }

   public void noop()
   {
      log.debug("noop");
   }

   public void npeError()
   {
      log.debug("npeError");
      Object obj = null;
      obj.toString();
   }

   public void unchecked()
   {
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("unchecked, callerPrincipal=" + p);
   }

   @RolesAllowed({"InternalRole"})
   public void excluded()
   {
      throw new EJBException("excluded, no access should be allowed");
   }

}
