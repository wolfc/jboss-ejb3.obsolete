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
package org.jboss.ejb3.core.test.ejbthree670;

import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;

/**
 * Test how many times pre destroy is called.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Stateful
@Remote(MyStateful.class)
public class MyStatefulBean implements MyStateful
{
   private static final Logger log = Logger.getLogger(MyStatefulBean.class);
   
   private String name;
   public static int preDestroyCalls = 0;
   
   @PreDestroy
   public void preDestroy()
   {
      preDestroyCalls++;
      log.info("pre destroy");
      if(preDestroyCalls > 1)
         throw new IllegalStateException("pre destroy called multiple times");
   }
   
   @Remove
   public void remove()
   {
      log.info("remove");
   }
   
   public String sayHello()
   {
      return "Hi " + name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

}
