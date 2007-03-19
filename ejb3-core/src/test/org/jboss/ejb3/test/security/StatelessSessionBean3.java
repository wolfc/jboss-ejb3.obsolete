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

import java.rmi.RemoteException;
import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.test.security.StatelessSession;
import org.jboss.logging.Logger;


/** A SessionBean that accesses an Entity bean in its echo() method to test runAs
 identity propagation. It also access its own excluded() method to test that the runAs
 identity is also see on methods of this bean that are invoked through the
 remote interface.
 
 @author Scott.Stark@jboss.org
 @version $Revision$
 */
@Stateless
@Remote(org.jboss.ejb3.test.security.StatelessSession.class)
@RemoteBinding(jndiBinding = "spec.RunAsStatelessSession")
@SecurityDomain("spec-test")
@RunAs("InternalRole")
public class StatelessSessionBean3
{
   private static final Logger log = Logger
   .getLogger(StatelessSessionBean3.class);
   
   @Resource SessionContext sessionContext;
   
   public void testGetBusinessObject()
   {
      StatelessSession ss = (StatelessSession)sessionContext.getBusinessObject(org.jboss.ejb3.test.security.StatelessSession.class);
      ss.noop();
   }

   /** This method creates an instance of the entity bean bound under
    java:comp/env/ejb/Entity and then invokes its echo method. This
    method should be accessible by user's with a role of Echo, while
    the Entity bean should only be accessible by the runAs role.
    */
   public String echo(String arg)
   {
      log.debug("echo, arg="+arg);
      // This call should fail if the bean is not secured
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("echo, callerPrincipal="+p);
      return p.getName();
   }
   
   public String forward(String echoArg)
   {
      log.debug("forward, echoArg="+echoArg);
      String echo = null;
      try
      {
         InitialContext ctx = new InitialContext();
         StatelessSession bean = (StatelessSession) ctx.lookup("java:comp/env/ejb/Session");
         echo = bean.echo(echoArg);
      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new EJBException(e);
      }
      return echo;
   }
   
   /** This method gets this bean's remote interface and invokes the
    excluded() method to test that the method is accessed as the
    runAs role.
    */
   public void noop()
   {
      log.debug("noop calling excluded...");
      excluded();
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
      log.debug("StatelessSessionBean.unchecked, callerPrincipal="+p);
   }
   
   /** This method should be assigned access to the runAs role and no user
    should have this role.
    */
   public void excluded()
   {
      log.debug("excluded, accessed");
      // This call should fail if the bean is not secured
      Principal p = sessionContext.getCallerPrincipal();
      log.debug("excluded, callerPrincipal="+p);
   }
}
