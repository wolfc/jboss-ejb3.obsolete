/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.cachepassivation;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.Cache;
import org.jboss.ejb3.annotation.CacheConfig;
import org.jboss.ejb3.annotation.PersistenceManager;
import org.jboss.ejb3.annotation.Pool;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Cache("SimpleStatefulCache")
@PersistenceManager("MyStatefulSessionFilePersistenceManager")
@CacheConfig(idleTimeoutSeconds=1)
@Pool("ThreadlocalPool")
@Stateful
public class MockBean implements Mock
{
   public static Object notification = new Object();
   public static boolean passivated = false;
   
   @Resource
   public SessionContext ctx;
   
   @PostActivate
   public void postActivate()
   {
      System.out.println("postActivate");
   }
   
   @PostConstruct
   public void postConstruct()
   {
      System.out.println("postConstruct");
   }
   
   @PrePassivate
   public void prePassivate()
   {
      System.out.println("prePassivate");
      synchronized (notification)
      {
         passivated = true;
         notification.notify();
      }
   }
}
