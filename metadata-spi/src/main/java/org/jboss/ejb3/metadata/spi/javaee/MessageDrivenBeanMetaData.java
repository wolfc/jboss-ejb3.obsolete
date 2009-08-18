//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;

/**
 *
 *
 * 	The message-driven element declares a message-driven
 * 	bean. The declaration consists of:
 *
 * 	    - an optional description
 * 	    - an optional display name
 * 	    - an optional icon element that contains a small and a large
 * 	      icon file name.
 * 	    - a name assigned to the enterprise bean in
 * 	      the deployment descriptor
 *             - an optional mapped-name element that can be used to provide
 *               vendor-specific deployment information such as the physical
 *               jndi-name of destination from which this message-driven bean
 *               should consume.  This element is not required to be supported
 *               by all implementations.  Any use of this element is non-portable.
 * 	    - the message-driven bean's implementation class
 * 	    - an optional declaration of the bean's messaging
 * 	      type
 *             - an optional declaration of the bean's timeout method.
 * 	    - the optional message-driven bean's transaction management
 *               type. If it is not defined, it is defaulted to Container.
 * 	    - an optional declaration of the bean's
 * 	      message-destination-type
 * 	    - an optional declaration of the bean's
 * 	      message-destination-link
 * 	    - an optional declaration of the message-driven bean's
 * 	      activation configuration properties
 *             - an optional list of the message-driven bean class and/or
 *               superclass around-invoke methods.
 * 	    - an optional declaration of the bean's environment
 * 	      entries
 * 	    - an optional declaration of the bean's EJB references
 * 	    - an optional declaration of the bean's local EJB
 * 	      references
 * 	    - an optional declaration of the bean's web service
 * 	      references
 * 	    - an optional declaration of the security
 * 	      identity to be used for the execution of the bean's
 * 	      methods
 * 	    - an optional declaration of the bean's
 * 	      resource manager connection factory
 * 	      references
 * 	    - an optional declaration of the bean's resource
 * 	      environment references.
 * 	    - an optional declaration of the bean's message
 * 	      destination references
 *
 *
 *
 * <p>Java class for message-driven-beanType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="message-driven-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType" minOccurs="0"/>
 *         &lt;element name="messaging-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="timeout-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/>
 *         &lt;element name="transaction-type" type="{http://java.sun.com/xml/ns/javaee}transaction-typeType" minOccurs="0"/>
 *         &lt;element name="message-destination-type" type="{http://java.sun.com/xml/ns/javaee}message-destination-typeType" minOccurs="0"/>
 *         &lt;element name="message-destination-link" type="{http://java.sun.com/xml/ns/javaee}message-destination-linkType" minOccurs="0"/>
 *         &lt;element name="activation-config" type="{http://java.sun.com/xml/ns/javaee}activation-configType" minOccurs="0"/>
 *         &lt;element name="around-invoke" type="{http://java.sun.com/xml/ns/javaee}around-invokeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="security-identity" type="{http://java.sun.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface MessageDrivenBeanMetaData extends EnterpriseBeanMetaData, IdMetaData
{

   /**
    *
    * @return Returns the fully qualified classname of the message
    * listener interface of this message-driven bean.
    *
    */
   String getMessagingType();

   /**
    * Sets the fully qualified classname of the message
    * listener interface of this message-driven bean.
    *
    * @param messageListenerType Fully qualified classname of the message listener
    * interface of this bean
    *
    */
   void setMessagingType(String messageListenerType);

   /**
    *
    * @return  Returns the timeout method of this bean.
    *
    * Returns null if there is no such method
    *
    */
   NamedMethodMetaData getTimeoutMethod();

   /**
    * Sets the timeout-method of this bean
    *
    * @param timeoutMethod The timeout method metadata
    *
    */
   void setTimeoutMethod(NamedMethodMetaData timeoutMethod);

   /**
    * Returns the transaction type of this bean
    *
    */
   TransactionType getTransactionType();

   /**
    * Sets the transaction type of this bean
    *
    * @param transactionType The transaction type of this bean
    *
    */
   void setTransactionType(TransactionType transactionType);

   /**
    *
    * @return Returns the destination type associated with this
    * message driven bean. The destination type is the fully qualified
    * classname of the interface expected to be implemented by the destination.
    *
    *  Ex: javax.jms.Queue
    *
    */
   String getMessageDestinationType();

   /**
    * Sets the destination type associated with this
    * message driven bean. The destination type is the fully qualified
    * classname of the interface expected to be implemented by the destination.
    *
    * @param destinationType Fully qualified classname of the interface implemented
    * by the destination
    *
    */
   void setMessageDestinationType(String destinationType);

   /**
    *
    * @return Returns the message destination link. The message-destination-link
    * is used to link a message destination reference or message-driven bean to a message
    * destination.
    */
   String getMessageDestinationLink();

   /**
    * Sets the message destination link.
    * The message-destination-link is used to link a message destination reference
    * or message-driven bean to a message
    *
    * @param destinationLink
    *
    */
   void setMessageDestinationLink(String destinationLink);

   /**
    *
    * @return Returns the activation config associated with this message driven bean
    *
    */
   ActivationConfigMetaData getActivationConfig();

   /**
    * Sets the activation configuration for this message driven bean
    *
    * @param activationConfig
    *
    */
   void setActivationConfig(ActivationConfigMetaData activationConfig);

   /**
    * Returns a list of around-invoke metadata of this bean.
    * Returns an empty list if there is no around-invoke for this bean
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    */
   List<AroundInvokeMetaData> getAroundInvokes();

   /**
    * Sets the list of around-invoke metadata of the bean.
    *
    * @param aroundInvokes
    */
   void setAroundInvokes(List<AroundInvokeMetaData> aroundInvokes);

   /**
    * Gets the value of the messageDestinationRef property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the messageDestinationRef property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getMessageDestinationRef().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link MessageDestinationRefType }
    *
    *
    */
   // TODO: We need this. Revisit this
   //List<MessageDestinationRefType> getMessageDestinationRef();

}