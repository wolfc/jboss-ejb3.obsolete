/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.test.security5;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

//$Id$

/**
 *  EJB3 Session Bean
 *  @author Anil.Saldhana@redhat.com
 *  @since  Aug 16, 2007 
 *  @version $Revision$
 */
@Stateless(name = "SimpleStatelessBean")
@Remote(SimpleSessionInterfaceRemote.class)
@RunAs("InternalRole")
public class SimpleSessionBean implements SimpleSessionInterfaceRemote
{
   @Resource
   SessionContext sessionContext;

   @RolesAllowed(
   {"Echo"})
   public String echo(String arg)
   {
      SimpleSessionInterface ssi = null;
      try
      {
         InitialContext context = new InitialContext();
         String jndiName = "FirstBean/local";
         ssi = (SimpleSessionInterface) context.lookup(jndiName);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      String str = ssi.echo(arg);
      if (str.equals(arg) == false)
         throw new IllegalStateException("First Bean returned:" + str);
      return arg;
   }

   public Principal echoCallerPrincipal()
   {
      return sessionContext.getCallerPrincipal();
   }

   public boolean isCallerInRole(String roleName)
   {
      return sessionContext.isCallerInRole(roleName);
   }
}
