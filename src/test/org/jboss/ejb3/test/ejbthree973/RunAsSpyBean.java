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

import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.SecurityDomain;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 67628 $
 */
@Stateless
@Remote(SpyMe.class)
@RunAs("Spy")
@SecurityDomain(value="", unauthenticatedPrincipal="anonymous")
public class RunAsSpyBean implements SpyMe
{
   @EJB(beanName="SpyAllowedBean")
   private SpyMe spyAllowedBean;
   
   public String getCallerPrincipal()
   {
      return spyAllowedBean.getCallerPrincipal();
   }

   public void notAllowed()
   {
      spyAllowedBean.notAllowed();
   }
}
