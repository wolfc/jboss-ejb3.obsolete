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
package org.jboss.ejb3.test.ejbthree1889.b;

import javax.ejb.EJB;

import org.jboss.ejb3.annotation.Service;
import org.jboss.ejb3.test.ejbthree1889.a.EJBTestARemote;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Service(objectName="ejbthree1889:service=EJBTestB")
public class EJBTestBService implements EJBTestBRemote
{
   private static final Logger log = Logger.getLogger(EJBTestBService.class);
   
   // since we're injecting something outside of our scope we need to specify mappedName
   @EJB(mappedName="EJBTestAService/remote")
   private EJBTestARemote testA;
   
   public String sayHello()
   {
      return testA.sayHello();
   }
   
   public void start()
   {
      log.info("EJBTestB starting...");
   }
   
   public void stop()
   {
      log.info("EJBTestB stopping...");      
   }
}
