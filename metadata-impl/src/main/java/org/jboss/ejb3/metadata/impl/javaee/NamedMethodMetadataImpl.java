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

import org.jboss.ejb3.metadata.spi.javaee.MethodParamsMetaData;
import org.jboss.ejb3.metadata.spi.javaee.NamedMethodMetaData;
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;

/**
 * NamedMethodMetadataImpl
 *
 * Represents the metadata for a named-method
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class NamedMethodMetadataImpl extends IdMetadataImpl implements NamedMethodMetaData
{
   /**
    * {@link org.jboss.metadata.ejb.spec.NamedMethodMetaData} from which
    * this {@link NamedMethodMetadataImpl} was constructed
    */
   private org.jboss.metadata.ejb.spec.NamedMethodMetaData delegate;

   /**
    * Name of the method
    */
   private String methodName;

   /**
    * Method parameters
    */
   private MethodParamsMetaData methodParams;

   /**
    * Constructs a {@link NamedMethodMetadataImpl} from the {@link org.jboss.metadata.ejb.spec.NamedMethodMetaData}
    * 
    * @param namedMethod
    */
   public NamedMethodMetadataImpl(org.jboss.metadata.ejb.spec.NamedMethodMetaData namedMethod)
   {
      super(namedMethod.getId());
      initialize(namedMethod);
   }

   /**
    * Initializes this {@link NamedMethodMetadataImpl} from the state present in the
    * <code>namedMethod</code>
    * 
    * @param namedMethod
    */
   protected void initialize(org.jboss.metadata.ejb.spec.NamedMethodMetaData namedMethod)
   {
      // set the delegate
      this.delegate = namedMethod;

      this.methodName = this.delegate.getMethodName();

      // method params
      MethodParametersMetaData delegateMethodParams = this.delegate.getMethodParams();
      this.methodParams = delegateMethodParams == null ? null : new MethodParamsMetadataImpl(delegateMethodParams);
   }

   /**
    * @see NamedMethodMetaData#getMethodName()
    */
   public String getMethodName()
   {
      return this.methodName;
   }

   /**
    * @see NamedMethodMetaData#getMethodParams()
    */
   public MethodParamsMetaData getMethodParams()
   {
      return this.methodParams;
   }

   /**
    * @see NamedMethodMetaData#setMethodName(String)
    */
   public void setMethodName(String methodName)
   {
      this.methodName = methodName;

   }

   /**
    * @see NamedMethodMetaData#setMethodParams(MethodParamsMetaData)
    */
   public void setMethodParams(MethodParamsMetaData methodParams)
   {
      this.methodParams = methodParams;

   }

}
