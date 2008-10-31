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
package org.jboss.ejb3.test.ejbthree1504;

import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.jboss.logging.Logger;

/**
 * SystemTimeService that returns the current time in milliseconds. It also 
 * implements MBeanRegistration to set the object name with which the MBean is 
 * registered dynamically. 
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
@Service(objectName=SystemTimeService.ORIGINAL_NAME)
@Management(SystemTime.class)
public class SystemTimeService implements SystemTime
{
   public static final String ORIGINAL_NAME = "org.jboss.ejb3:service=SystemTime";
   public static final String DYNAMIC_NAME = "org.jboss.ejb3:service=SystemTime,type=dynamic";
   
   private static final Logger log = Logger.getLogger(SystemTimeService.class);
   
   public long getCurrentTimeMillis()
   {
      long currentTimeMillis = System.currentTimeMillis();
      
      log.info("Current time millis is: " + currentTimeMillis);

      return currentTimeMillis;      
   }
}