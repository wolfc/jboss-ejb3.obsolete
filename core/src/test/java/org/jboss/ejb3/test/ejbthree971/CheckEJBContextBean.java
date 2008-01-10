/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree971;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Comment
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version: $Revision: $
 */
@Stateless
@Remote(CheckEJBContext.class)
public class CheckEJBContextBean implements CheckEJBContext
{
   @Resource(name="ejbContext")
   private SessionContext ctx;
   
   public void checkForEjbContextInEnv()
   {
      checkSessionContext(lookup("java:comp/EJBContext"));
   }

   public void checkForInjectedEjbContext()
   {
      checkSessionContext(ctx);
   }

   public void checkForInjectedEjbCtxInEnv()
   {
      checkSessionContext(lookup("java:comp/env/ejbContext"));
   }
   
   private void checkSessionContext(SessionContext context)
   {
      if(context == null)
         throw new IllegalStateException("context is null");
   }

   private InitialContext getInitialContext() throws NamingException
   {
      return new InitialContext();
   }
   
   private SessionContext lookup(String name)
   {
      try
      {
         return (SessionContext) getInitialContext().lookup(name);
      }
      catch (NamingException e)
      {
         throw new IllegalStateException("Can't lookup " + name, e);
      }
   }
}
