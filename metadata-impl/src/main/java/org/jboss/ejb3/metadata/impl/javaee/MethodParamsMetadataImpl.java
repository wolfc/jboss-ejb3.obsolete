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

import java.util.ArrayList;
import java.util.List;

import org.jboss.ejb3.metadata.spi.javaee.MethodParamsMetaData;
import org.jboss.metadata.ejb.spec.MethodParametersMetaData;

/**
 * MethodParamsMetadataImpl
 *
 * Represents the metadata for method-params
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MethodParamsMetadataImpl implements MethodParamsMetaData
{

   /**
    * Delegate from which this {@link MethodParamsMetadataImpl} was constructed
    */
   private MethodParametersMetaData delegate;

   /**
    * Id
    */
   private String id;

   /**
    * method-params
    */
   private List<String> methodParams;

   /**
    * Constructs a {@link MethodParamsMetadataImpl} out of a {@link MethodParametersMetaData}
    * 
    * @param methodParams
    */
   public MethodParamsMetadataImpl(MethodParametersMetaData methodParams)
   {
      this.initialize(methodParams);
   }

   /**
    * Initializes this {@link MethodParamsMetadataImpl} from the state present in 
    * <code>methodParams</code>
    * 
    * @param methodParams
    */
   public void initialize(MethodParametersMetaData methodParams)
   {
      // set the delegate
      this.delegate = methodParams;

      // delegate itself is a List<String> 
      this.methodParams = new ArrayList<String>(this.delegate);
   }

   /**
    * @see MethodParamsMetaData#getMethodParams()
    */
   public List<String> getMethodParams()
   {
      return this.methodParams;
   }

   /**
    * @see MethodParamsMetaData#setMethodParams(List)
    */
   public void setMethodParams(List<String> methodParams)
   {
      this.methodParams = methodParams;

   }

   /**
    * @see MethodParamsMetaData#getId()
    */
   public String getId()
   {
      return this.id;
   }

   /**
    * @see MethodParamsMetaData#setId(String)
    */
   public void setId(String id)
   {
      this.id = id;
   }

}
