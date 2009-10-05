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

import org.jboss.ejb3.metadata.spi.javaee.NamedMethodMetaData;
import org.jboss.ejb3.metadata.spi.javaee.RemoveMethodMetaData;

/**
 * RemoveMethodMetadataImpl
 *
 * Represents the metadata for remove-method
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class RemoveMethodMetadataImpl extends IdMetadataImpl implements RemoveMethodMetaData
{

   /** 
    * The {@link org.jboss.metadata.ejb.spec.RemoveMethodMetaData} from which
    * this {@link RemoveMethodMetadataImpl} was constructed
    */
   private org.jboss.metadata.ejb.spec.RemoveMethodMetaData delegate;

   /**
    * The remove method of the bean
    */
   private NamedMethodMetaData beanMethod;

   /**
    * A flag which decided whether the bean should 
    * be retained on exception
    */
   private boolean retainIfException;

   /**
    * Constructs a {@link RemoveMethodMetadataImpl} from a {@link org.jboss.metadata.ejb.spec.RemoveMethodMetaData}
    * 
    * @param removeMethod
    * @throws NullPointerException If the passed <code>removeMethod</code> is null
    */
   public RemoveMethodMetadataImpl(org.jboss.metadata.ejb.spec.RemoveMethodMetaData removeMethod)
   {
      super(removeMethod.getId());
      this.initialize(removeMethod);
   }

   /**
    * Initializes this {@link RemoveMethodMetadataImpl} from the state in <code>removeMethod</code>
    * 
    * @param removeMethod
    * @throws NullPointerException If the passed <code>removeMethod</code> is null
    */
   private void initialize(org.jboss.metadata.ejb.spec.RemoveMethodMetaData removeMethod)
   {
      // set the delegate
      this.delegate = removeMethod;

      // bean method
      org.jboss.metadata.ejb.spec.NamedMethodMetaData delegateBeanMethod = this.delegate.getBeanMethod();
      if (delegateBeanMethod != null)
      {
         this.beanMethod = new NamedMethodMetadataImpl(delegateBeanMethod);
      }

      // retain-if-exception
      this.retainIfException = this.delegate.isRetainIfException();
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.RemoveMethodMetaData#getBeanMethod()
    */
   public NamedMethodMetaData getBeanMethod()
   {
      return this.beanMethod;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.RemoveMethodMetaData#isRetainIfException()
    */
   public boolean isRetainIfException()
   {
      return this.retainIfException;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.RemoveMethodMetaData#setBeanMethod(org.jboss.ejb3.metadata.spi.javaee.NamedMethodMetaData)
    */
   public void setBeanMethod(NamedMethodMetaData removeMethod)
   {
      this.beanMethod = removeMethod;

   }

   /** 
    * @see org.jboss.ejb3.metadata.spi.javaee.RemoveMethodMetaData#setRetainIfException(boolean)
    */
   public void setRetainIfException(boolean retainIfException)
   {
      this.retainIfException = retainIfException;

   }

}
