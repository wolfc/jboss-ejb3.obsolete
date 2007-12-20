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
package org.jboss.ejb3.test.exception;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision: 61136 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless
@Remote(Foo2.class)
public class FooBean2 implements Foo2
{
   private static final Logger log = Logger.getLogger(FooBean2.class);

   @Resource
   private SessionContext ctx;

   public void bar() throws FooException2
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         Foo3 foo3 = (Foo3) jndiContext.lookup("FooBean3/remote");
         foo3.bar();
      } catch (NamingException e)
      {
         throw new FooException2(e.getMessage());
      } catch (FooException3 e)
      {
         throw new FooException2(e.getMessage());
      } 
   }
}