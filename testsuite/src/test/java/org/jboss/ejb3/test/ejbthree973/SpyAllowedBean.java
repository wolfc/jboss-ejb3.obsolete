/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree973;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * Only Spy is allowed to call me.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
@Stateless
@Remote(SpyMe.class)
@SecurityDomain(value="", unauthenticatedPrincipal="anonymous")
@RolesAllowed("Spy")
public class SpyAllowedBean implements SpyMe
{
   @Resource
   private SessionContext ctx;
   
   @EJB(beanName="WhoAmIBean")
   private WhoAmI whoAmIBean;
   
   public String getCallerPrincipal()
   {
      return whoAmIBean.getCallerPrincipal();
   }

   @RolesAllowed("nobody")
   public void notAllowed()
   {
      String me;
      try
      {
         me = ctx.getCallerPrincipal().getName();
      }
      catch(Exception e)
      {
         me = "<error: " + e.getMessage() + ">";
      }
      throw new RuntimeException(me + " should not come here");
   }

}
