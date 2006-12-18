/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.injection.test.annotated;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.jboss.injection.test.common.Counter;

/**
 * Do injection based on annotation.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class InjectedBean
{
   @Resource
   private String value;
   
   /*
    * To prove extendibility of annotated injection. 
    * This should create a SimpleValueInjector.
    */
   @Simple(value="Hello world")
   private String simpleValue;
   
   public InjectedBean()
   {
      if(value != null)
         throw new IllegalStateException("value must be null");
      
      if(simpleValue != null)
         throw new IllegalStateException("simpleValue must be null");
   }
   
   public void check()
   {
      if(value == null)
         throw new IllegalStateException("value must not be null");
      
      if(simpleValue == null)
         throw new IllegalStateException("simpleValue must no be null");
   }
   
   @PostConstruct
   public void postConstruct()
   {
      if(value == null)
         throw new IllegalStateException("value must not be null");
      
      if(simpleValue == null)
         throw new IllegalStateException("simpleValue must no be null");
      
      Counter.postConstructs++;
   }
   
   @PreDestroy
   public void preDestroy()
   {
      Counter.preDestroys++;
   }
}
