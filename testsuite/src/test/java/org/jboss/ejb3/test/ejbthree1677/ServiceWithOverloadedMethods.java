/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.ejbthree1677;

import javax.ejb.Remote;

import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.Service;
import org.jboss.ejb3.test.ejbthree1677.unit.ServiceTestCase;

/**
 * ServiceWithOverloadedMethods
 *
 * Meant for test EJBTHREE-1677. Used in
 * {@link ServiceTestCase}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Service(objectName = ServiceWithOverloadedMethods.OBJECT_NAME)
@Management(ServiceManagement.class)
@Remote(ServiceManagement.class)
@RemoteBinding(jndiBinding = ServiceWithOverloadedMethods.JNDI_NAME)
public class ServiceWithOverloadedMethods implements ServiceManagement
{
   public static final String OBJECT_NAME = "ejbthree1677:service=ServiceWithOverloadedMethods";

   public static final String JNDI_NAME = "ejbthree1677/ServiceWithOverloadedMethods";

   public String echoMessage(String message)
   {
      return message;
   }

   public String sayHi(String name)
   {
      return "Hi " + name;
   }

   public String sayHi()
   {
      return "Hi";
   }

}
