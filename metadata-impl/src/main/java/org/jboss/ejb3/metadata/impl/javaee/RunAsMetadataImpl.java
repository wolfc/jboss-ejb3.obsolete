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

import java.util.List;

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.RunAsMetaData;

/**
 * RunAsMetadataImpl
 *
 * Represents the metadata for run-as
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class RunAsMetadataImpl extends IdMetadataImpl implements RunAsMetaData
{

   /**
    * The {@link org.jboss.metadata.javaee.spec.RunAsMetaData} from which this
    * {@link RunAsMetadataImpl} was constructed
    */
   private org.jboss.metadata.javaee.spec.RunAsMetaData delegate;

   /**
    * Role name
    */
   private String roleName;

   /**
    * Constructs a {@link RunAsMetadataImpl} from a {@link org.jboss.metadata.javaee.spec.RunAsMetaData}
    * 
    * @param runAs
    * @throws NullPointerException If the passed <code>runAs</code> is null
    */
   public RunAsMetadataImpl(org.jboss.metadata.javaee.spec.RunAsMetaData runAs)
   {
      super(runAs.getId());
      this.initialize(runAs);
   }

   /**
    * Initializes this {@link RunAsMetadataImpl} from the state in <code>runAs</code>
    * 
    * @param runAs
    * @throws NullPointerException If the passed <code>runAs</code> is null
    */
   private void initialize(org.jboss.metadata.javaee.spec.RunAsMetaData runAs)
   {
      // set the delegate
      this.delegate = runAs;

      this.roleName = this.delegate.getRoleName();

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.RunAsMetaData#getDescription()
    */
   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.RunAsMetaData#getRoleName()
    */
   public String getRoleName()
   {
      return this.roleName;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.RunAsMetaData#setRoleName(java.lang.String)
    */
   public void setRoleName(String roleName)
   {
      this.roleName = roleName;

   }

}
