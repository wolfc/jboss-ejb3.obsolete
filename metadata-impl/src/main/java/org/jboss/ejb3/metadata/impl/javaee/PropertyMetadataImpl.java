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

import org.jboss.ejb3.metadata.spi.javaee.PropertyMetaData;

/**
 * PropertiesMetadataImpl
 * 
 * Represents the metadata for the &lt;property&gt; element
 *   
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class PropertyMetadataImpl extends IdMetadataImpl implements PropertyMetaData
{

   /**
    * {@link org.jboss.metadata.javaee.spec.PropertyMetaData} from which this 
    * {@link PropertyMetadataImpl} was constructed
    */
   private org.jboss.metadata.javaee.spec.PropertyMetaData delegate;

   /**
    * Property name
    */
   private String name;

   /**
    * Property value
    */
   private String value;

   /**
    * Constructs a {@link PropertyMetadataImpl} out of a {@link org.jboss.metadata.javaee.spec.PropertyMetaData}
    * 
    * @param property
    */
   public PropertyMetadataImpl(org.jboss.metadata.javaee.spec.PropertyMetaData property)
   {
      super(property.getId());
      this.initialize(property);

   }

   /**
    * Initializes this {@link PropertyMetadataImpl} from the state in <code>property</code>
    * 
    * @param property
    */
   private void initialize(org.jboss.metadata.javaee.spec.PropertyMetaData property)
   {
      // set the delegate
      this.delegate = property;

      this.name = this.delegate.getName();
      this.value = this.delegate.getValue();

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.PropertyMetaData#getName()
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.PropertyMetaData#getValue()
    */
   public String getValue()
   {
      return this.value;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.PropertyMetaData#setName(java.lang.String)
    */
   public void setName(String name)
   {
      this.name = name;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.PropertyMetaData#setValue(java.lang.String)
    */
   public void setValue(String value)
   {
      this.value = value;

   }

}
