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
import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.Container;

/** An implmentation of the Session interface that delegates its
echo method call to the PrivateSession bean to test run-as.

@author Scott.Stark@jboss.org
@version $Revision$ 
*/
@Stateless(name="PublicSession")
@Remote(org.jboss.ejb3.test.security.Session.class)
@RemoteBinding(jndiBinding = "spec.PublicSession")
@SecurityDomain("spec-test")
@RolesAllowed({"Echo"})
@EJBs({@EJB(name="PrivateSession", beanInterface=org.jboss.ejb3.test.security.Session.class, beanName="PrivateSession")})
public class PublicSessionBean
{
   @Resource SessionContext sessionContext;
   
    public String echo(String arg)
    {
        System.out.println("PublicSessionBean.echo, arg="+arg);
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PublicSessionBean.echo, callerPrincipal="+p);
        System.out.println("PublicSessionBean.echo, isCallerInRole('EchoUser')="+sessionContext.isCallerInRole("EchoUser"));
        try
        {
            InitialContext ctx = new InitialContext();
			Session bean = (Session) ctx.lookup(Container.ENC_CTX_NAME + "/env/PrivateSession");
            System.out.println("PublicSessionBean.echo, created PrivateSession");
            arg = bean.echo(arg);
        }
        catch(Exception e)
        {
        }
        return arg;
    }
    public void noop()
    {
        System.out.println("PublicSessionBean.noop");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PublicSessionBean.noop, callerPrincipal="+p);
    }
    public void restricted() 
    {
        System.out.println("PublicSessionBean.restricted");
        Principal p = sessionContext.getCallerPrincipal();
        System.out.println("PublicSessionBean.restricted, callerPrincipal="+p);
    }
}
