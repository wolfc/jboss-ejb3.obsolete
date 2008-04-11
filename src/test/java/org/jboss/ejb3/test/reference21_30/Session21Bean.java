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

import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class Session21Bean implements javax.ejb.SessionBean
{
   private static final Logger log = Logger.getLogger(Session21Bean.class);
   
   public String access()
   {
      return "Session21";
   }
   
   public String access30()
   {
      try {
         InitialContext jndiContext = new InitialContext();
         Session30 session = (Session30)jndiContext.lookup(Container.ENC_CTX_NAME + "/env/Session30");
         return session.access();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   public String globalAccess30()
   {
      try {
         InitialContext jndiContext = new InitialContext();
         Session30RemoteBusiness session = (Session30RemoteBusiness)jndiContext.lookup("GlobalSession30Remote");
         return session.access();
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public void ejbCreate()
   {
    
   }
   
   public void ejbActivate()
   {
   
   }
   
   public void ejbPassivate()
   {
      
   }
   
   public void ejbRemove()
   {
      
   }
   
   public void setSessionContext(SessionContext context)
   {
      
   }
   
}
