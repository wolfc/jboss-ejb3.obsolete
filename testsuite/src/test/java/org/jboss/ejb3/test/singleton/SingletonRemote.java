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

/**
 * A SingletonRemote.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public interface SingletonRemote
{
   /**
    * @return current number of created instances (must always be 1)
    */
   int getInstanceCount();

   /**
    * This method demonstrates that once one thread is entered an instance method
    * no other thread can enter any method of the same instance in case of write concurrency.
    */
   void writeLock(long pause);
   
   /**
    * This method demonstrates that two threads can be active in the same session bean instance in case of read concurrency.
    */
   int getValue(int valueThreshold, long timeout);
   
   /**
    * Sets the new value and returns the previous one.
    * 
    * @param i  new value
    * @return  previous value
    */
   int setValue(int i);
   
   /**
    * Returns the name of the last executed method
    * 
    * @return
    */
   String getLastReturnedValueMethod();
}
