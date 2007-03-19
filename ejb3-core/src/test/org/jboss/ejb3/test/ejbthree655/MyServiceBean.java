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
package org.jboss.ejb3.test.ejbthree655;

import javax.ejb.Remote;

import org.jboss.annotation.ejb.Service;
import org.jboss.logging.Logger;

/**
 * This service bean tests the magic lifecycle methods. (EJBTHREE-655)
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Service
@Remote(MyService.class)
public class MyServiceBean extends AbstractStateChecker
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(MyServiceBean.class);
   
//   public void create()
//   {
//      log.info("create called");
//   }
//   
//   public void destroy()
//   {
//      log.info("destroy called");
//   }
   
   public String sayHelloTo(String name)
   {
      return "Hi " + name;
   }
   
//   public void start()
//   {
//      log.info("start called");
//   }
//   
//   public void stop()
//   {
//      log.info("stop called");
//   }
}
