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
import org.jboss.ejb3.metadata.spi.javaee.SecurityIdentityMetaData;

/**
 * SecurityIdentityMetadataImpl
 * 
 * Represents the metadata for security-identity
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SecurityIdentityMetadataImpl extends IdMetadataImpl implements SecurityIdentityMetaData
{

   /**
    * The {@link org.jboss.metadata.ejb.spec.SecurityIdentityMetaData} from which this
    * {@link SecurityIdentityMetadataImpl} was constructed
    * 
    */
   private org.jboss.metadata.ejb.spec.SecurityIdentityMetaData delegate;

   /**
    * run-as 
    */
   private RunAsMetaData runAs;

   /**
    * Flag which decides whether the caller identity should be used
    */
   private boolean useCallerIdentity;

   /**
    * Constructs a {@link SecurityIdentityMetadataImpl} from a {@link org.jboss.metadata.ejb.spec.SecurityIdentityMetaData}
    * 
    * @param secIdentity
    * @throws NullPointerException If the passed <code>secIdentity</code> is null
    */
   public SecurityIdentityMetadataImpl(org.jboss.metadata.ejb.spec.SecurityIdentityMetaData secIdentity)
   {
      super(secIdentity.getId());
      this.initialize(secIdentity);
   }

   /**
    * Initializes this {@link SecurityIdentityMetadataImpl} from the state in <code>secIdentity</code>
    * 
    * @param secIdentity
    * @throws NullPointerException If the passed <code>secIdentity</code> is null
    */
   private void initialize(org.jboss.metadata.ejb.spec.SecurityIdentityMetaData secIdentity)
   {
      // set the delegate
      this.delegate = secIdentity;

      // run-as
      org.jboss.metadata.javaee.spec.RunAsMetaData delegateRunAs = this.delegate.getRunAs();
      if (delegateRunAs != null)
      {
         this.runAs = new RunAsMetadataImpl(delegateRunAs);
      }

      // use-caller-identity
      if (this.delegate.getUseCallerIdentity() != null)
      {
         this.useCallerIdentity = true;
      }

   }

   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityIdentityMetaData#getRunAs()
    */
   public RunAsMetaData getRunAs()
   {
      return this.runAs;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityIdentityMetaData#isUseCallerIdentity()
    */
   public boolean isUseCallerIdentity()
   {
      return this.useCallerIdentity;
   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityIdentityMetaData#setRunAs(org.jboss.ejb3.metadata.spi.javaee.RunAsMetaData)
    */
   public void setRunAs(RunAsMetaData runAs)
   {
      this.runAs = runAs;

   }

   /**
    * 
    * @see org.jboss.ejb3.metadata.spi.javaee.SecurityIdentityMetaData#setUseCallerIdentity(boolean)
    */
   public void setUseCallerIdentity(boolean useCallerIdentity)
   {
      this.useCallerIdentity = useCallerIdentity;

   }

}
