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
package org.jboss.ejb3.test.asynchronous;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.common.proxy.plugins.async.AsyncUtils;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Stateless
@SecurityDomain("other")
@Remote(SecuredStatelessRemote.class)
@Local(SecuredStatelessLocal.class)
public class SecuredStatelessBean implements SecuredStatelessRemote, SecuredStatelessLocal
{
   @PermitAll
   public int uncheckedMethod(int i)
   {
      return i;
   }

   @DenyAll
   public int excludedMethod(int i)
   {
      return i;
   }

   @RolesAllowed("allowed")
   public int method(int i)
   {
      
      SecuredStatelessLocal local = null;
      try
      {
         Context context = new InitialContext();
         local = (SecuredStatelessLocal) context.lookup(SecuredStatelessBean.class.getSimpleName() + "/local");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      SecuredStatelessLocal asynchLocal = AsyncUtils.mixinAsync(local);

      asynchLocal.excludedMethod(i);
      Object ret = getReturnOrException(asynchLocal);
      if (!(ret instanceof EJBAccessException))
      {
         throw new RuntimeException("Local excluded method call did not cause a SecurityException");
      }

      asynchLocal.localSecured(i);
      ret = getReturnOrException(asynchLocal);
      return (Integer)ret;
   }

   @RolesAllowed("allowed")
   public int localSecured(int i)
   {
      return i;
   }

   private Object getReturnOrException(Object proxy)
   {
      try
      {
         Future<?> future = AsyncUtils.getFutureResult(proxy);

         while (!future.isDone())
         {
            Thread.sleep(100);
         }
         return future.get();
      }
      catch(ExecutionException e)
      {
         return e.getCause();
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException("Bummer");
      }
   }

}
