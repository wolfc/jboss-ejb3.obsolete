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
package org.jboss.ejb3.test.naming.bad;

import javax.ejb.Remove;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.annotation.Resource;

/** A bean that uses resource injection incorrectly to test the container
 * problem detection
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 61136 $
 */
@Stateless
public class BadFieldMethodBean implements BadInjector
{
   private @Resource String key;

   @PostConstruct
   public void init()
   {
      System.out.println("PostConstruct state, key="+key);
   }

   @Resource
   public void setKey(String key)
   {
      this.key = key;
      System.out.println("setKey, key="+key);
   }

   public String getKey()
   {
      System.out.println("ping, key="+key);
      return key;
   }

   @Remove
   public void remove()
   {
   }

}
