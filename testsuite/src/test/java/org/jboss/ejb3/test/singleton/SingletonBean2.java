/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.singleton;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb3.annotation.AspectDomain;
import org.jboss.ejb3.annotation.Pool;

/**
 * A SingletonBean2.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@Stateless(name="SingletonBean2")
@Remote(SingletonRemote2.class)
@Pool (value="SingletonPool")
@AspectDomain(value = "Singleton Stateless Bean")
public class SingletonBean2 extends AbstractSingletonBean implements SingletonRemote2
{
   // counter for created instances
   private static Integer instanceCount = 0;

   // instance initialization
   {
      synchronized(instanceCount)
      {
         ++instanceCount;
      }
   }
   
   public int getInstanceCount()
   {
      return instanceCount;
   }
   
   public int setValueToSingleton1Value(int singleton1ValueThreshold, long singleton1Timeout)
   {
      SingletonRemote singleton;
      try
      {
         singleton = (SingletonRemote) new InitialContext().lookup("SingletonBean/remote");
      }
      catch (NamingException e)
      {
         throw new IllegalStateException("Failed to lookup SingletonBean/remote: " + e.getMessage());
      }
     
      value = singleton.getValue(singleton1ValueThreshold, singleton1Timeout);
      
      return value;
   }
}
