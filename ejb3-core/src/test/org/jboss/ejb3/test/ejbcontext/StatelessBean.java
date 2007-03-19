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
package org.jboss.ejb3.test.ejbcontext;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version $Revision$
 */
@Stateless(name="Stateless")
@Local(StatelessLocal.class)
@Remote(org.jboss.ejb3.test.ejbcontext.Stateless.class)
@LocalBinding(jndiBinding = "StatelessLocal")
@RemoteBinding(jndiBinding = "Stateless")
public class StatelessBean
   implements org.jboss.ejb3.test.ejbcontext.Stateless, StatelessLocal
{
   private static final Logger log = Logger.getLogger(StatelessBean.class);
   
   @Resource
   SessionContext sessionContext;

   public void noop()
   {
      
   }
   
   public void testEjbContextLookup() throws Exception
   {
      Stateful stateful = (Stateful)sessionContext.lookup("Stateful");
      stateful.test();
   }
   
   public Class testInvokedBusinessInterface() throws Exception
   {
      return sessionContext.getInvokedBusinessInterface();
   }
   
   public Object testBusinessObject(Class businessInterface) throws Exception
   {
      return sessionContext.getBusinessObject(businessInterface);
   }
   
   public void testEjbObject() throws Exception
   {
      javax.ejb.EJBObject ejbObject = sessionContext.getEJBObject();
      ejbObject.getHandle();
   }
   
   public void testEjbLocalObject() throws Exception
   {
      javax.ejb.EJBLocalObject ejbObject = sessionContext.getEJBLocalObject();
      ejbObject.getClass();
   }
}
