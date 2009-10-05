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
package org.jboss.ejb3.metadata.impl.javaee;

import org.jboss.ejb3.metadata.spi.javaee.LifecycleCallbackMetaData;

/**
 * LifecycleCallbackMetadataImpl
 * 
 * Represents the metadata for lifecycle-callback
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class LifecycleCallbackMetadataImpl implements LifecycleCallbackMetaData
{

   /**
    * Delegate from which this {@link LifecycleCallbackMetadataImpl} instance
    * was constructed
    */
   private org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData delegate;

   /**
    * Fully qualified name of the lifecycle callback class
    */
   private String lifecycleCallbackClassname;

   /**
    * Name of the lifecycle callback method
    */
   private String lifecycleCallbackMethodName;

   /**
    * Constructs a {@link LifecycleCallbackMetadataImpl} from the {@link org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData}
    * instance
    * 
    * @param lifecycleCallback
    */
   public LifecycleCallbackMetadataImpl(org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData lifecycleCallback)
   {
      this.initialize(lifecycleCallback);
   }

   private void initialize(org.jboss.metadata.javaee.spec.LifecycleCallbackMetaData lifecycleCallback)
   {
      // set the delegate
      this.delegate = lifecycleCallback;

      this.lifecycleCallbackClassname = this.delegate.getClassName();
      this.lifecycleCallbackMethodName = this.delegate.getMethodName();

   }

   /**
    * @see LifecycleCallbackMetaData#getLifecycleCallbackClass()
    */
   public String getLifecycleCallbackClass()
   {
      return this.lifecycleCallbackClassname;
   }

   /**
    * @see LifecycleCallbackMetaData#getLifecycleCallbackMethod()
    */
   public String getLifecycleCallbackMethod()
   {
      return this.lifecycleCallbackMethodName;
   }

   /**
    * @see LifecycleCallbackMetaData#setLifecycleCallbackClass(String)
    */
   public void setLifecycleCallbackClass(String lifecycleCallbackClass)
   {
      this.lifecycleCallbackClassname = lifecycleCallbackClass;
   }

   /**
    * @see LifecycleCallbackMetaData#setLifecycleCallbackMethod(String)
    */
   public void setLifecycleCallbackMethod(String methodName)
   {
      this.lifecycleCallbackMethodName = methodName;
   }

}
