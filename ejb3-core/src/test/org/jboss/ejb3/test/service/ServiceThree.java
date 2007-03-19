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

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import org.jboss.annotation.ejb.Depends;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;

/**
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
// note: this is a serialized name, not a canonical name
@Service (objectName = "jboss.ejb3:management=interface,with=customName,is=serviceThree")
@Management(ServiceThreeManagement.class)
@Depends ({"jboss.j2ee:jar=service-test.jar,name=ServiceTwo,service=EJB3","jboss.j2ee:jar=service-test.jar,name=ServiceOne,service=EJB3,type=ManagementInterface"})
public class ServiceThree implements ServiceThreeManagement
{
   @EJB
   private ServiceOneRemote serviceOne;

   int intercepted;

   public boolean getInjected()
   {
      return serviceOne != null;
   }

   public int getIntercepted()
   {
      return intercepted;
   }

   @AroundInvoke
   public Object intercept(InvocationContext ctx) throws Exception
   {
      System.out.println("Interceptor");
      intercepted++;
      return ctx.proceed();
   }

   public void create() throws Exception
   {
      System.out.println("ServiceThree - CREATE");
      Tester.creates.add("3");
   }

   public void start() throws Exception
   {
      System.out.println("ServiceThree - START");
      Tester.starts.add("3");      
   }

   public void stop()
   {
      System.out.println("ServiceThree - STOP");
   }

   public void destroy()
   {
      System.out.println("ServiceThree - DESTROY");
   }
}
