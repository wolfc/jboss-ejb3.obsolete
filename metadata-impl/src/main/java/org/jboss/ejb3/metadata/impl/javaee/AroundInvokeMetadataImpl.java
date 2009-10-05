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

import org.jboss.ejb3.metadata.spi.javaee.AroundInvokeMetaData;

/**
 * AroundInvokeMetadataImpl
 *
 * Represents the metadata for around-invoke 
 * 
 * @see AroundInvokeMetaData
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class AroundInvokeMetadataImpl implements AroundInvokeMetaData
{

   /**
    * Delegate from which this {@link AroundInvokeMetadataImpl} was constructed
    */
   private org.jboss.metadata.ejb.spec.AroundInvokeMetaData delegate;
   
   /**
    * The classname specified in this around-invoke
    */
   private String classname;
   
   /**
    * The around-invoke method name
    */
   private String methodName;
   
   /**
    * Constructor
    * Creates a {@link AroundInvokeMetadataImpl} out of a {@link org.jboss.metadata.ejb.spec.AroundInvokeMetaData}
    * 
    * @param aroundInvoke
    */
   public AroundInvokeMetadataImpl(org.jboss.metadata.ejb.spec.AroundInvokeMetaData aroundInvoke)
   {
      this.initialize(aroundInvoke);
   }
   
   /**
    * Initializes this {@link AroundInvokeMetadataImpl} from the state in {@link org.jboss.metadata.ejb.spec.AroundInvokeMetaData}
    * 
    * @param aroundInvoke
    */
   private void initialize(org.jboss.metadata.ejb.spec.AroundInvokeMetaData aroundInvoke)
   {
      // set the delegate
      this.delegate = aroundInvoke;
      this.classname = this.delegate.getClassName();
      this.methodName = this.delegate.getMethodName();
      
   }
   /**
    * @see AroundInvokeMetaData#getClassname()
    */
   public String getClassname()
   {
      return this.classname;
   }

   /**
    * @see AroundInvokeMetaData#getMethodName()
    */
   public String getMethodName()
   {
      return this.methodName;
   }

   /**
    * @see AroundInvokeMetaData#setClassname(String)
    */
   public void setClassname(String classname)
   {
      this.classname = classname;
      
   }

   /**
    * {@inheritDoc}
    */
   public void setMethodName(String methodName)
   {
      this.methodName = methodName;
      
   }

}
