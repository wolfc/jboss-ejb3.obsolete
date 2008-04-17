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
package org.jboss.ejb3.test.tx.common;



/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleContainer
{
   private String name;
   private String domainName;
   private Class<Object> beanClass;
   
   private StatefulContainer<Object> beanContainer;
   
   /**
    * @param name
    * @param domainName
    * @param beanClass
    */
   public SimpleContainer(String name, String domainName, Class<Object> beanClass)
   {
      this.name = name;
      this.domainName = domainName;
      this.beanClass = beanClass;
   }

   public <I> I constructProxy(Class<I> intf) throws Throwable
   {
      return beanContainer.constructProxy(intf);
   }
   
   public void start()
   {
      this.beanContainer = new StatefulContainer<Object>(name, domainName, beanClass);
   }
   
   public void stop()
   {
      this.beanContainer = null;
   }
}
