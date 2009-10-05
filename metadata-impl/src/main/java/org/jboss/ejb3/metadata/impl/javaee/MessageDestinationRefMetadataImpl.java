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
import java.util.Set;

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.MessageDestinationUsageType;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * MessageDestinationRefMetadataImpl
 * 
 * Represents the metadata for a message-destination-ref
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class MessageDestinationRefMetadataImpl extends IdMetadataImpl implements MessageDestinationRefMetaData
{

   /** 
    * The {@link MessageDestinationReferenceMetaData} from which this {@link MessageDestinationRefMetadataImpl}
    * was constructed
    */
   private MessageDestinationReferenceMetaData delegate;

   /**
    * Injection targets
    */
   private List<InjectionTargetMetaData> injectionTargets;

   /**
    * mapped-name
    */
   private String mappedName;

   /**
    * Message destination link
    */
   private String messageDestinationLink;

   /**
    * Name of the message destination reference
    */
   private String messageDestinationRefName;

   /**
    * Fully qualified classname of the interface of 
    * the message destination type
    */
   private String messageDestinationType;

   /**
    * message destination usage
    */
   private MessageDestinationUsageType messageDestUsage;

   /**
    * Constructs a {@link MessageDestinationRefMetadataImpl} from a {@link MessageDestinationReferenceMetaData}
    * 
    * @param messageDestRef
    * @throws NullPointerException If the passed <code>messageDestRef</code> is null
    */
   public MessageDestinationRefMetadataImpl(MessageDestinationReferenceMetaData messageDestRef)
   {
      super(messageDestRef.getId());
      this.initialize(messageDestRef);
   }

   /**
    * Initializes this {@link MessageDestinationRefMetadataImpl} from the state in
    * <code>messageDestRef</code>
    * 
    * @param messageDestRef
    * @throws NullPointerException If the passed <code>messageDestRef</code> is null
    */
   private void initialize(MessageDestinationReferenceMetaData messageDestRef)
   {
      // set the delegate
      this.delegate = messageDestRef;

      this.mappedName = this.delegate.getMappedName();
      this.messageDestinationLink = this.delegate.getLink();
      this.messageDestinationRefName = this.delegate.getMessageDestinationRefName();
      this.messageDestinationType = this.delegate.getType();

      // message destination usage
      org.jboss.metadata.javaee.spec.MessageDestinationUsageType delegateMessageDestUsage = this.delegate
            .getMessageDestinationUsage();
      if (delegateMessageDestUsage != null)
      {
         if (delegateMessageDestUsage == org.jboss.metadata.javaee.spec.MessageDestinationUsageType.Consumes)
         {
            this.messageDestUsage = MessageDestinationUsageType.CONSUMES;
         }
         else if (delegateMessageDestUsage == org.jboss.metadata.javaee.spec.MessageDestinationUsageType.Produces)
         {
            this.messageDestUsage = MessageDestinationUsageType.PRODUCES;
         }
         else if (delegateMessageDestUsage == org.jboss.metadata.javaee.spec.MessageDestinationUsageType.ConsumesProduces)
         {
            this.messageDestUsage = MessageDestinationUsageType.CONSUMES_PRODUCES;
         }
      }

      // injection targets
      Set<ResourceInjectionTargetMetaData> injectionTargetsMD = this.delegate.getInjectionTargets();
      if (injectionTargetsMD != null)
      {
         this.injectionTargets = new ArrayList<InjectionTargetMetaData>(injectionTargetsMD.size());
         for (ResourceInjectionTargetMetaData injectionTarget : injectionTargetsMD)
         {
            this.injectionTargets.add(new InjectionTargetMetadataImpl(injectionTarget));
         }
      }

   }

   /** 
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#getDescription()
    */
   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#getInjectionTargets()
    */
   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      return this.injectionTargets;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#getMappedName()
    */
   public String getMappedName()
   {
      return this.mappedName;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#getMessageDestinationLink()
    */
   public String getMessageDestinationLink()
   {
      return this.messageDestinationLink;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#getMessageDestinationRefName()
    */
   public String getMessageDestinationRefName()
   {
      return this.messageDestinationRefName;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#getMessageDestinationType()
    */
   public String getMessageDestinationType()
   {
      return this.messageDestinationType;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#getMessageDestinationUsage()
    */
   public MessageDestinationUsageType getMessageDestinationUsage()
   {
      return this.messageDestUsage;
   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#setInjectionTargets(java.util.List)
    */
   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      this.injectionTargets = injectionTargets;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#setMappedName(java.lang.String)
    */
   public void setMappedName(String mappedName)
   {
      this.mappedName = mappedName;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#setMessageDestinationLink(java.lang.String)
    */
   public void setMessageDestinationLink(String messageDestinationLink)
   {
      this.messageDestinationLink = messageDestinationLink;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#setMessageDestinationRefName(java.lang.String)
    */
   public void setMessageDestinationRefName(String messageDestinationRefName)
   {
      this.messageDestinationRefName = messageDestinationRefName;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#setMessageDestinationType(java.lang.String)
    */
   public void setMessageDestinationType(String messageDestinationType)
   {
      this.messageDestinationType = messageDestinationType;

   }

   /**
    * @see org.jboss.ejb3.metadata.spi.javaee.MessageDestinationRefMetaData#setMessageDestinationUsage(org.jboss.ejb3.metadata.spi.javaee.MessageDestinationUsageType)
    */
   public void setMessageDestinationUsage(MessageDestinationUsageType messageDestinationUsage)
   {
      this.messageDestUsage = messageDestinationUsage;

   }

}
