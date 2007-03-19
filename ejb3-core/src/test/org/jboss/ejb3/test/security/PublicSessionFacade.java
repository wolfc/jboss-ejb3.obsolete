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
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.Container;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@Stateless(name="PublicSessionFacade")
@Remote(org.jboss.ejb3.test.security.SessionFacade.class)
@RemoteBinding(jndiBinding = "spec.PublicSessionFacade")
@SecurityDomain("spec-test")
@RolesAllowed({"Echo"})
@EJBs({@EJB(name="TargetEJB", beanInterface=org.jboss.ejb3.test.security.Session.class, beanName="PublicSession")})
public class PublicSessionFacade
{
   @Resource SessionContext sessionContext;
   
   public String callEcho(String arg)
      throws RemoteException
   {
      Principal user = sessionContext.getCallerPrincipal();
      String echoMsg = null;
      try
      {
         InitialContext ctx = new InitialContext();
         Session bean = (Session) ctx.lookup(Container.ENC_CTX_NAME + "/env/TargetEJB");
         echoMsg = bean.echo("Hello, arg="+arg);
         echoMsg = bean.echo("Hello 2, arg="+arg);
      }
      catch (NamingException e)
      {
         throw new RemoteException("callEcho failed", e);
      }
    
      return echoMsg;
   }
}
