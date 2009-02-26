/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.core.test.txsync;

import java.rmi.RemoteException;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Stateful
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class TxSyncBean implements TxSync, SessionSynchronization
{
   private static final Logger log = Logger.getLogger(TxSyncBean.class);
   
   public static int afterBeginCalls = 0;
   public static int afterCompletionCalls = 0;
   public static int beforeCompletionCalls = 0;

   @Resource(name="test")
   private SessionContext ctx;
   
   private static boolean throwInAfterBegin = false;
   private boolean throwInAfterCompletion = false;
   private boolean throwInBeforeCompletion = false;

   public void afterBegin() throws EJBException, RemoteException
   {
      log.info("afterBegin");
      afterBeginCalls ++;
      checkSessionContext();
      if(throwInAfterBegin)
         throw new RuntimeException("afterBegin");
   }

   public void afterCompletion(boolean committed) throws EJBException, RemoteException
   {
      log.info("afterCompletion(" + committed + ")");
      afterCompletionCalls++;
      checkSessionContext();
      if(throwInAfterCompletion)
         throw new RuntimeException("afterCompletion");
   }

   public void beforeCompletion() throws EJBException, RemoteException
   {
      log.info("beforeCompletion");
      beforeCompletionCalls++;
      checkSessionContext();
      if(throwInBeforeCompletion)
         throw new RuntimeException("beforeCompletion");
   }

   private void checkSessionContext()
   {
      try
      {
         SessionContext other = (SessionContext) new InitialContext().lookup("java:comp/env/test");
         if(!other.equals(ctx))
            throw new RuntimeException("found wrong SessionContext at java:comp/env/test " + other + " != " + ctx);
      }
      catch(NamingException e)
      {
         log.error("failed to lookup java:comp/env/test", e);
         throw new RuntimeException(e);
      }
   }
   
   @Remove
   public void remove()
   {
      
   }
   
   public String sayHi(String name)
   {
      checkSessionContext();
      return "Hi " + name;
   }
   
   public static void setThrowInAfterBegin(boolean flag)
   {
      throwInAfterBegin  = flag;
   }
   
   public void setThrowInAfterCompletion(boolean flag)
   {
      this.throwInAfterCompletion  = flag;
   }
   
   public void setThrowInBeforeCompletion(boolean flag)
   {
      this.throwInBeforeCompletion  = flag;
   }
}
