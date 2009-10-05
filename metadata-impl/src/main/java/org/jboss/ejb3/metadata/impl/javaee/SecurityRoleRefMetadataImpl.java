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
import org.jboss.ejb3.metadata.spi.javaee.SecurityRoleRefMetaData;

/**
 * SecurityRoleRefMetadataImpl
 *
 * Represents the metadata for security-role-ref
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SecurityRoleRefMetadataImpl extends IdMetadataImpl implements SecurityRoleRefMetaData
{

   /**
    * {@link org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData} from which this
    * {@link SecurityRoleRefMetadataImpl} was constructed
    */
   private org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData delegate;

   /**
    * role link
    */
   private String roleLink;

   /**
    * role name
    */
   private String roleName;

   /**
    * Constructs a {@link SecurityRoleRefMetadataImpl} from a {@link SecurityRoleRefMetaData}
    * 
    * @param securityRoleRef
    * @throws NullPointerException If the passed <code>securityRoleRef</code> is null
    */
   public SecurityRoleRefMetadataImpl(org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData securityRoleRef)
   {
      super(securityRoleRef.getId());
      this.initialize(securityRoleRef);
   }

   /**
    * Initializes this {@link SecurityRoleRefMetadataImpl} from the state in <code>securityRoleRef</code>
    * 
    * @param securityRoleRef
    * @throws NullPointerException If the passed <code>securityRoleRef</code> is null
    */
   private void initialize(org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData securityRoleRef)
   {
      // set the delegate
      this.delegate = securityRoleRef;
      
      this.roleLink = this.delegate.getRoleLink();
      this.roleName = this.delegate.getRoleName();

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityRoleRefMetaData#getDescription()
    */
   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityRoleRefMetaData#getRoleLink()
    */
   public String getRoleLink()
   {
      return this.roleLink;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityRoleRefMetaData#getRoleName()
    */
   public String getRoleName()
   {
      return this.roleName;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityRoleRefMetaData#setRoleLink(java.lang.String)
    */
   public void setRoleLink(String value)
   {
      this.roleLink = value;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityRoleRefMetaData#setRoleName(java.lang.String)
    */
   public void setRoleName(String value)
   {
      this.roleName = value;

   }

}
