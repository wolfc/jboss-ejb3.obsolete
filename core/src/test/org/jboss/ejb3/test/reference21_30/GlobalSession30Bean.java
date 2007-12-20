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
package org.jboss.ejb3.test.reference21_30;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision: 67628 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless(name="GlobalSession30")
@Remote(Session30RemoteBusiness.class)
@RemoteBinding(jndiBinding = "GlobalSession30Remote")
public class GlobalSession30Bean 
{
   private static final Logger log = Logger.getLogger(GlobalSession30Bean.class);
   
   public String access()
   {
      return "Session30";
   }
   
   public String access21()
   {
      return null;
   }
   
   public String accessLocalStateful()
   {
      return "not supported";
   }
   
   public String accessLocalStateful(String value)
   {
      return "not supported";
   }
   
   public String accessLocalStateful(String value, Integer suffix)
   {
      return "not supported";
   }
   
   public String globalAccess21()
   {
      try {
         InitialContext jndiContext = new InitialContext();
         Session21Home home = (Session21Home)jndiContext.lookup("Session21Remote");
         Session21 session = (Session21)home.create();
         return session.access();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
}
