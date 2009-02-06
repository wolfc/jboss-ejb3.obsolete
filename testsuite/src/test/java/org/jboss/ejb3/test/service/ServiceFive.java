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
package org.jboss.ejb3.test.service;

import javax.management.ObjectName;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
@Service
@Management(ServiceFiveManagement.class)
public class ServiceFive implements ServiceFiveManagement
{
   @Depends("jboss.ejb3:is=serviceThree,management=interface,with=customName")
   public ServiceThreeManagement serviceThreeA;

   @Depends("jboss.j2ee:jar=service-test.jar,name=ServiceFour,service=EJB3,type=ManagementInterface")
   public ObjectName serviceFourA;

   private ServiceThreeManagement serviceThreeB;
   private ObjectName serviceFourB;

   @Depends("jboss.ejb3:is=serviceThree,management=interface,with=customName")
   public void setServiceThree(ServiceThreeManagement proxy)
   {
      serviceThreeB = proxy;
   }

   @Depends("jboss.j2ee:jar=service-test.jar,name=ServiceFour,service=EJB3,type=ManagementInterface")
   public void setServiceFour(ObjectName on)
   {
      serviceFourB = on;
   }

   public void create() throws Exception
   {
      System.out.println("ServiceFive - CREATE");
      new Exception().printStackTrace();
      Tester.creates.add("5");
   }

   public void start() throws Exception
   {
      System.out.println("ServiceFive - START");
      Tester.starts.add("5");
   }

   public boolean getInjectedProxyField()
   {
      return serviceThreeA != null;
   }

   public boolean getInjectedObjectNameField()
   {
      return serviceFourA != null;
   }

   public boolean getInjectedProxyMethod()
   {
      return serviceThreeB != null;
   }

   public boolean getInjectedObjectNameMethod()
   {
      return serviceFourB != null;
   }
}
