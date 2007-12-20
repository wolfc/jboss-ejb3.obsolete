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
package org.jboss.ejb3.embedded;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.util.JBossStringBuilder;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 61136 $
 */
public class KernelErrors
{
   private static final Logger log = Logger.getLogger(KernelErrors.class);

   public static boolean validate(Kernel kernel)
   {
      Controller controller = kernel.getController();
      Set notInstalled = new HashSet(controller.getNotInstalled());
      if (notInstalled.isEmpty()) return true;

      for (Iterator i = notInstalled.iterator(); i.hasNext();)
      {
         KernelControllerContext context = (KernelControllerContext) i.next();
         if (context.getState().equals(context.getRequiredState()))
            i.remove();
      }

      if (notInstalled.isEmpty()) return true;

      HashSet errors = new HashSet();
      HashSet incomplete = new HashSet();
      for (Iterator i = notInstalled.iterator(); i.hasNext();)
      {
         KernelControllerContext ctx = (KernelControllerContext) i.next();
         if (ctx.getState().equals(ControllerState.ERROR))
            errors.add(ctx);
         else
            incomplete.add(ctx);
      }
      if (errors.size() != 0)
      {
         log.error("*** DEPLOYMENTS IN ERROR:\n");
         for (Iterator i = errors.iterator(); i.hasNext();)
         {
            KernelControllerContext ctx = (KernelControllerContext) i.next();
            log.error("Failed deployment: " + ctx.getName(), ctx.getError());
         }
      }
      if (incomplete.size() != 0)
      {
         JBossStringBuilder buffer = new JBossStringBuilder();
         buffer.append("\n*** DEPLOYMENTS MISSING DEPENDENCIES:\n");
         for (Iterator i = incomplete.iterator(); i.hasNext();)
         {
            KernelControllerContext ctx = (KernelControllerContext) i.next();
            buffer.append(ctx.getName()).append(" depends on: \n");
            DependencyInfo dependsInfo = ctx.getDependencyInfo();
            Set depends = dependsInfo.getIDependOn(null);
            for (Iterator j = depends.iterator(); j.hasNext();)
            {
               DependencyItem item = (DependencyItem) j.next();
               buffer.append("                     ").append(item.getIDependOn()).append("'{").append(item.getWhenRequired().getStateString());
               buffer.append(':');
               ControllerContext other = controller.getContext(item.getIDependOn(), null);
               if (other == null)
                  buffer.append("NOT FOUND");
               else
                  buffer.append(other.getState().getStateString());
               buffer.append('}');
               if (j.hasNext())
                  buffer.append("\n");
            }
            buffer.append('\n');
         }
         log.error(buffer.toString());
      }
      if (errors.size() > 0 || incomplete.size() > 0)
      {
         return false;
      }
      return true;
   }
}
