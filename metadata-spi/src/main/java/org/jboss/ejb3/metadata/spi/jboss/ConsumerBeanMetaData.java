//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

import java.util.List;

import org.jboss.ejb3.metadata.spi.javaee.ActivationConfigMetaData;
import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.DisplayNameMetaData;
import org.jboss.ejb3.metadata.spi.javaee.IconType;
import org.jboss.ejb3.metadata.spi.javaee.IdMetaData;

/**
 *  The consumer element holds all of the information specific about a
 *             consumer bean which is a JBoss proprietary extension to EJB3 for sending JMS messages via
 *             standard Java interfaces. Used in: enterprise-beans 
 * 
 * <p>Java class for consumer-beanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="consumer-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType"/>
 *         &lt;element name="message-destination" type="{http://www.jboss.com/xml/ns/javaee}consumer-message-destinationType"/>
 *         &lt;element name="message-destination-type" type="{http://java.sun.com/xml/ns/javaee}message-destination-typeType"/>
 *         &lt;element name="producer" type="{http://www.jboss.com/xml/ns/javaee}producerType" maxOccurs="unbounded"/>
 *         &lt;element name="local-producer" type="{http://www.jboss.com/xml/ns/javaee}producerType" maxOccurs="unbounded"/>
 *         &lt;element name="current-message" type="{http://www.jboss.com/xml/ns/javaee}method-attributesType"/>
 *         &lt;element name="message-properties" type="{http://www.jboss.com/xml/ns/javaee}message-propertiesType" maxOccurs="unbounded"/>
 *         &lt;element name="ejb-ref" type="{http://www.jboss.com/xml/ns/javaee}ejb-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-local-ref" type="{http://www.jboss.com/xml/ns/javaee}ejb-local-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-identity" type="{http://www.jboss.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *         &lt;element name="resource-ref" type="{http://www.jboss.com/xml/ns/javaee}resource-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="resource-env-ref" type="{http://www.jboss.com/xml/ns/javaee}resource-env-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="message-destination-ref" type="{http://www.jboss.com/xml/ns/javaee}message-destination-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-domain" type="{http://www.jboss.com/xml/ns/javaee}security-domainType" minOccurs="0"/>
 *         &lt;element name="method-attributes" type="{http://www.jboss.com/xml/ns/javaee}method-attributesType" minOccurs="0"/>
 *         &lt;element name="depends" type="{http://www.jboss.com/xml/ns/javaee}dependsType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="annotation" type="{http://www.jboss.com/xml/ns/javaee}annotationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ignore-dependency" type="{http://www.jboss.com/xml/ns/javaee}ignore-dependencyType" minOccurs="0"/>
 *         &lt;element name="aop-domain-name" type="{http://www.jboss.com/xml/ns/javaee}aop-domain-nameType" minOccurs="0"/>
 *         &lt;element name="pool-config" type="{http://www.jboss.com/xml/ns/javaee}pool-configType" minOccurs="0"/>
 *         &lt;element name="jndi-ref" type="{http://www.jboss.com/xml/ns/javaee}jndi-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="activation-config" type="{http://www.jboss.com/xml/ns/javaee}activation-configType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface ConsumerBeanMetaData extends IdMetaData
{

   /**
    * Gets the value of the description property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the description property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getDescription().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link DescriptionMetaData }
    * 
    * 
    */
   List<DescriptionMetaData> getDescription();

   /**
    * Gets the value of the displayName property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the displayName property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getDisplayName().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link DisplayNameMetaData }
    * 
    * 
    */
   List<DisplayNameMetaData> getDisplayName();

   /**
    * Gets the value of the icon property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the icon property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getIcon().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link IconType }
    * 
    * 
    */
   List<IconType> getIcon();

   /**
    * @return Returns the EJB name
    *     
    */
   String getEjbName();

   /**
    * Sets the bean name
    * @param ejbName Name of the EJB
    *     
    */
   void setEjbName(String ejbName);

   /**
    * @return Returns the fully qualified class name of the bean
    *     
    */
   String getEjbClass();

   /**
    * Sets the fully qualified class name of the bean
    * 
    * @param ejbClass
    *     
    */
   void setEjbClass(String ejbClass);

   /**
    * @return Returns the jndi-name of the message destination
    * corresponding to this consumer bean
    *      
    */
   String getMessageDestination();

   /**
    * Sets the jndi-name of the message destination
    * correpsonding to this consumer bean
    * 
    * @param messageDestination The jndi-name of the message destination
    *     
    */
   void setMessageDestination(String messageDestination);

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
    * @return Returns the list of producers associated with
    * this consumer bean
    * 
    * 
    */
   List<JBossProducerMetaData> getProducers();

   /**
    * Sets the producers associated with this consumer bean
    * 
    * @param producers
    */
   void setProducers(List<JBossProducerMetaData> producers);

   /**
    * @return Returns the list of local producers associated with
    * this consumer bean
    * 
    * 
    */
   List<JBossProducerMetaData> getLocalProducers();

   /**
    * Sets the local producers associated with this consumer bean
    * 
    * @param localProducers
    */
   void setLocalProducers(List<JBossProducerMetaData> localProducers);

   /**
    * 
    * @return
    *     
    */
   MethodAttributesMetaData getCurrentMessage();

   /**
    * Sets the value of the currentMessage property.
    * 
    * @param value
    *     
    */
   void setCurrentMessage(MethodAttributesMetaData value);

   /**
    * @return Returns the message properties
    * 
    * 
    */
   List<MessagePropertiesMetaData> getMessageProperties();

   /**
    * Sets the message properties
    * 
    * @param messageProperties
    */
   void setMessageProperties(List<MessagePropertiesMetaData> messageProperties);

   /**
    * Returns the list of EJB references of this bean
    * Returns an empty list if there is no EJB reference for this bean.
    * 
    */
   List<JBossEjbRefMetaData> getEjbRefs();

   /**
    * Sets the list of EJB references for this bean
    * 
    * @param ejbRefs
    */
   void setEjbRefs(List<JBossEjbRefMetaData> ejbRefs);

   /**
    * @return Returns the list of EJB local references of this bean
    * Returns an empty list if there is no EJB local reference for this bean.
    * 
    */
   List<JBossEjbLocalRefMetaData> getEjbLocalRefs();

   /**
    * Sets the list of EJB local references for this bean
    *
    * @param ejbLocalRefs The list of EJB local references
    */
   void setEjbLocalRefs(List<JBossEjbLocalRefMetaData> ejbLocalRefs);

   /**
    * @return Returns the security identity associated with this bean
    *     
    */
   JBossSecurityIdentityMetaData getSecurityIdentity();

   /**
    * Sets the security identity associated with this bean
    * 
    * @param securityIdentity 
    *     
    */
   void setSecurityIdentity(JBossSecurityIdentityMetaData securityIdentity);

   /**
    * @return Returns the list of resource references of this bean.
    * Returns an empty list if there are no such references.
    * 
    * 
    */
   List<JBossResourceRefMetaData> getResourceRefs();

   /**
    * Sets the resource references of this bean
    * 
    * @param resourceRefs
    */
   void setResourceRefs(List<JBossResourceRefMetaData> resourceRefs);

   /**
    * @return Returns the list of resource environment references of this bean.
    * Returns an empty list if there are no such references.
    * 
    * 
    */
   List<JBossResourceEnvRefMetaData> getResourceEnvRefs();

   /**
    * Sets the resource env references of this bean
    * 
    * @param resourceEnvRefs
    */
   void setResourceEnvRefs(List<JBossResourceEnvRefMetaData> resourceEnvRefs);

   /**
    * @return Returns the list of message destination references of this bean.
    * Returns an empty list if there are no such references.
    * 
    * 
    */
   List<JBossMessageDestinationRefMetaData> getMessageDestinationRefs();

   /**
    * Sets the message destination references of this bean
    * 
    * @param messageDestinationRefs
    */
   void setMessageDestinationRefs(List<JBossMessageDestinationRefMetaData> messageDestinationRefs);

   /**
    * 
    * @return Returns the security domain associated with this bean
    *     
    */
   String getSecurityDomain();

   /**
    * Sets the security domain associated with this bean
    * 
    * @param securityDomain The security domain for this bean
    *     
    */
   void setSecurityDomain(String securityDomain);

   /**
    * Returns the method attributes configured for this bean
    * 
    */
   MethodAttributesMetaData getMethodAttributes();

   /**
    * Sets the method attributes for this bean 
    * 
    * @param methodAttributes
    *     
    */
   void setMethodAttributes(MethodAttributesMetaData methodAttributes);

   /**
    * @return Returns a list of dependencies of this bean
    * 
    */
   List<String> getDepends();

   /**
    * Sets the dependencies for this bean
    * 
    * @param dependencies
    */
   void setDepends(List<String> dependencies);

   /**
    * @return Returns the list of annotation added to this  consumer bean
    * 
    */
   List<AnnotationMetaData> getAnnotations();

   /**
    * Set the list of annotations to be added to the bean class, method or field
    * 
    * @param annotations The list of annotations
    */
   void setAnnotations(List<AnnotationMetaData> annotations);

   /**
    *  @return Returns the ignore dependency metadata associated with this bean
    *
    */
   IgnoreDependencyMetaData getIgnoreDependency();

   /**
    * Sets the ignore dependency metadata for this bean
    *
    * @param ignoreDependency The ignore dependency metadata
    *
    */
   void setIgnoreDependency(IgnoreDependencyMetaData ignoreDependency);

  /**
   *
   * @return Returns the AOP domain name associated with this bean.
   * The aspect domain contains the interceptor stack and bindings
   *
   */
   String getAopDomainName();

   /**
    * Sets the AOP domain name for this bean
    *
    * @param aopDomainName The AOP domain name
    *
    */
   void setAopDomainName(String aopDomainName);

   /**
    *
    *
    * @return Returns the pool configuration of this bean
    *
    */
   PoolConfigMetaData getPoolConfig();

   /**
    * Sets the pool configuration of this bean
    *
    * @param poolConfig The pool configuration for this bean
    *
    */
   void setPoolConfig(PoolConfigMetaData poolConfig);

   /**
    * @return Returns a list of jndi references for this bean.
    * Returns an empty list if there are no such references.
    *
    */
   List<JndiRefMetaData> getJndiRefs();

   /**
    * Set the jndi references used to inject generic types based on JNDI name,
    * for this bean
    *
    * @param jndiRefs
    */
   void setJndiRefs(List<JndiRefMetaData> jndiRefs);

   /**
    * @return Returns the activation config associated with this consumer bean
    *     
    */
   ActivationConfigMetaData getActivationConfig();

   /**
    * Sets the activation configuration for this consumer bean
    *
    * @param activationConfig
    *     
    */
   void setActivationConfig(ActivationConfigMetaData activationConfig);

}
