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

import org.jboss.ejb3.metadata.spi.javaee.InitMethodMetaData;
import org.jboss.ejb3.metadata.spi.javaee.NamedMethodMetaData;

/**
 * InitMethodMetadataImpl
 * 
 * Represents the metadata for init-method
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class InitMethodMetadataImpl extends IdMetadataImpl implements InitMethodMetaData
{

   /**
    * Delegate from which this {@link InitMethodMetadataImpl} was constructed
    */
   private org.jboss.metadata.ejb.spec.InitMethodMetaData delegate;

   /**
    * bean-method
    */
   private NamedMethodMetaData beanMethod;

   /**
    * create-method
    */
   private NamedMethodMetaData createMethod;

   /**
    * Constructs a {@link InitMethodMetadataImpl} out of a {@link org.jboss.metadata.ejb.spec.InitMethodMetaData}
    * 
    * @param initMethod
    */
   public InitMethodMetadataImpl(org.jboss.metadata.ejb.spec.InitMethodMetaData initMethod)
   {
      super(initMethod.getId());
      initialize(initMethod);
   }

   /**
    * Initializes this {@link InitMethodMetadataImpl} from the state available in 
    * the <code>initMethod</code>
    * 
    * @param initMethod
    */
   private void initialize(org.jboss.metadata.ejb.spec.InitMethodMetaData initMethod)
   {
      // set the delegate
      this.delegate = initMethod;

      org.jboss.metadata.ejb.spec.NamedMethodMetaData delegateBeanMethod = this.delegate.getBeanMethod();
      this.beanMethod = delegateBeanMethod == null ? null : new NamedMethodMetadataImpl(delegateBeanMethod);

      org.jboss.metadata.ejb.spec.NamedMethodMetaData delegateCreateMethod = this.delegate.getCreateMethod();
      this.createMethod = delegateCreateMethod == null ? null : new NamedMethodMetadataImpl(delegateCreateMethod);
   }

   /**
    * @see InitMethodMetaData#getBeanMethod()
    */
   public NamedMethodMetaData getBeanMethod()
   {
      return this.beanMethod;
   }

   /**
    * @see InitMethodMetaData#getCreateMethod()
    */
   public NamedMethodMetaData getCreateMethod()
   {
      return this.createMethod;
   }

   /**
    * @see InitMethodMetaData#setBeanMethod(NamedMethodMetaData)
    */
   public void setBeanMethod(NamedMethodMetaData beanMethod)
   {
      this.beanMethod = beanMethod;
   }

   /**
    * @see InitMethodMetaData#setCreateMethod(NamedMethodMetaData)
    */
   public void setCreateMethod(NamedMethodMetaData createMethod)
   {
      this.createMethod = createMethod;
   }

}
