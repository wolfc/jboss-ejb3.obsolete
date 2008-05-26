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
package org.jboss.ejb3.test.dd.web.ejb;

import javax.naming.InitialContext;
import javax.annotation.security.PermitAll;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;

import org.jboss.ejb3.test.dd.web.interfaces.ReferenceTest;
import org.jboss.ejb3.test.dd.web.interfaces.StatelessSession;
import org.jboss.ejb3.test.dd.web.interfaces.ReturnData;
import org.jboss.logging.Logger;

/** A stateless SessionBean 

 @author  Scott.Stark@jboss.org
 @version $Revision$
 */
public class StatelessSessionBean2
{
   static Logger log = Logger.getLogger(StatelessSessionBean2.class);

   private SessionContext sessionContext;

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
   }

   public String echo(String arg)
   {
      return arg;
   }

   public String forward(String echoArg)
   {
      String echo = null;
      try
      {
         InitialContext ctx = new InitialContext();
         StatelessSession bean = (StatelessSession) ctx.lookup("java:comp/env/ejb/Session");
         echo = bean.echo(echoArg);
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
      return echo;
   }

   public void noop(ReferenceTest test, boolean optimized)
   {
      boolean wasSerialized = test.getWasSerialized();
    
      if (optimized && wasSerialized == true)
         throw new EJBException("Optimized call had non-optimized(i.e. serialized) argument");
      if (optimized == false && wasSerialized == false)
         throw new EJBException("Non-optimized call had optimized (i.e. non-serialized) argument");
   }

   public ReturnData getData()
   {
      ReturnData data = new ReturnData();
      data.data = "TheReturnData2";
      return data;
   }

   /** A method deployed with no method permissions */
   public void unchecked()
   {
      log.debug("unchecked");
   }

   /** A method deployed with method permissions such that only a run-as
    * assignment will allow access. 
    */
   public void checkRunAs()
   {
      log.debug("checkRunAs");
   }
}
