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

import java.util.Date;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.logging.Logger;

/**
 * TimeTellerBean.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
@Stateless
@Remote(TimeTeller.class)
public class TimeTellerBean implements TimeTeller
{
   private static final Logger log = Logger.getLogger(TimeTellerBean.class);
         
   @Depends(SystemTimeService.ORIGINAL_NAME) private SystemTime systemTimeService;

   public Date whatsTheTime(String origin) throws Exception
   {
      log.debug("Call via SystemTime EJB3 service implementation");
      long currentTimeMillis = systemTimeService.getCurrentTimeMillis();
      Date date = new Date(currentTimeMillis);
      log.info("current time is: "+ date + " and origin: " + origin);      
      return date;
   }

}
