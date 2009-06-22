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
public interface ConsumerBeanMetaData
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
    * Gets the value of the ejbName property.
    * 
    * @return
    *     possible object is
    *     {@link EjbNameType }
    *     
    */
   String getEjbName();

   /**
    * Sets the value of the ejbName property.
    * 
    * @param value
    *     allowed object is
    *     {@link EjbNameType }
    *     
    */
   void setEjbName(String value);

   /**
    * Gets the value of the ejbClass property.
    * 
    * @return
    *     possible object is
    *     {@link EjbClassType }
    *     
    */
   String getEjbClass();

   /**
    * Sets the value of the ejbClass property.
    * 
    * @param value
    *     allowed object is
    *     {@link EjbClassType }
    *     
    */
   void setEjbClass(String value);

   /**
    * Gets the value of the messageDestination property.
    * 
    * @return
    *     possible object is
    *     {@link ConsumerMessageDestinationType }
    *     
    */
   String getMessageDestination();

   /**
    * Sets the value of the messageDestination property.
    * 
    * @param value
    *     allowed object is
    *     {@link ConsumerMessageDestinationType }
    *     
    */
   void setMessageDestination(String value);

   /**
    * Gets the value of the messageDestinationType property.
    * 
    * @return
    *     possible object is
    *     {@link MessageDestinationTypeType }
    *     
    */
   String getMessageDestinationType();

   /**
    * Sets the value of the messageDestinationType property.
    * 
    * @param value
    *     allowed object is
    *     {@link MessageDestinationTypeType }
    *     
    */
   void setMessageDestinationType(String value);

   /**
    * Gets the value of the producer property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the producer property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getProducer().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossProducerMetaData }
    * 
    * 
    */
   List<JBossProducerMetaData> getProducer();

   /**
    * Gets the value of the localProducer property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the localProducer property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getLocalProducer().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossProducerMetaData }
    * 
    * 
    */
   List<JBossProducerMetaData> getLocalProducer();

   /**
    * Gets the value of the currentMessage property.
    * 
    * @return
    *     possible object is
    *     {@link MethodAttributesMetaData }
    *     
    */
   MethodAttributesMetaData getCurrentMessage();

   /**
    * Sets the value of the currentMessage property.
    * 
    * @param value
    *     allowed object is
    *     {@link MethodAttributesMetaData }
    *     
    */
   void setCurrentMessage(MethodAttributesMetaData value);

   /**
    * Gets the value of the messageProperties property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the messageProperties property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getMessageProperties().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link MessagePropertiesMetaData }
    * 
    * 
    */
   List<MessagePropertiesMetaData> getMessageProperties();

   /**
    * Gets the value of the ejbRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the ejbRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getEjbRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossEjbRefMetaData }
    * 
    * 
    */
   List<JBossEjbRefMetaData> getEjbRef();

   /**
    * Gets the value of the ejbLocalRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the ejbLocalRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getEjbLocalRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossEjbLocalRefMetaData }
    * 
    * 
    */
   List<JBossEjbLocalRefMetaData> getEjbLocalRef();

   /**
    * Gets the value of the securityIdentity property.
    * 
    * @return
    *     possible object is
    *     {@link JBossSecurityIdentityMetaData }
    *     
    */
   JBossSecurityIdentityMetaData getSecurityIdentity();

   /**
    * Sets the value of the securityIdentity property.
    * 
    * @param value
    *     allowed object is
    *     {@link JBossSecurityIdentityMetaData }
    *     
    */
   void setSecurityIdentity(JBossSecurityIdentityMetaData value);

   /**
    * Gets the value of the resourceRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getResourceRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossResourceRefMetaData }
    * 
    * 
    */
   List<JBossResourceRefMetaData> getResourceRef();

   /**
    * Gets the value of the resourceEnvRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the resourceEnvRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getResourceEnvRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossResourceEnvRefMetaData }
    * 
    * 
    */
   List<JBossResourceEnvRefMetaData> getResourceEnvRef();

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
    * {@link JBossMessageDestinationRefMetaData }
    * 
    * 
    */
   List<JBossMessageDestinationRefMetaData> getMessageDestinationRef();

   /**
    * Gets the value of the securityDomain property.
    * 
    * @return
    *     possible object is
    *     {@link SecurityDomainType }
    *     
    */
   String getSecurityDomain();

   /**
    * Sets the value of the securityDomain property.
    * 
    * @param value
    *     allowed object is
    *     {@link SecurityDomainType }
    *     
    */
   void setSecurityDomain(String value);

   /**
    * Gets the value of the methodAttributes property.
    * 
    * @return
    *     possible object is
    *     {@link MethodAttributesMetaData }
    *     
    */
   MethodAttributesMetaData getMethodAttributes();

   /**
    * Sets the value of the methodAttributes property.
    * 
    * @param value
    *     allowed object is
    *     {@link MethodAttributesMetaData }
    *     
    */
   void setMethodAttributes(MethodAttributesMetaData value);

   /**
    * Gets the value of the depends property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the depends property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getDepends().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link DependsType }
    * 
    * 
    */
   List<String> getDepends();

   /**
    * Gets the value of the annotation property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the annotation property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getAnnotation().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link AnnotationMetaData }
    * 
    * 
    */
   List<AnnotationMetaData> getAnnotation();

   /**
    * Gets the value of the ignoreDependency property.
    * 
    * @return
    *     possible object is
    *     {@link IgnoreDependencyMetaData }
    *     
    */
   IgnoreDependencyMetaData getIgnoreDependency();

   /**
    * Sets the value of the ignoreDependency property.
    * 
    * @param value
    *     allowed object is
    *     {@link IgnoreDependencyMetaData }
    *     
    */
   void setIgnoreDependency(IgnoreDependencyMetaData value);

   /**
    * Gets the value of the aopDomainName property.
    * 
    * @return
    *     possible object is
    *     {@link AopDomainNameType }
    *     
    */
   String getAopDomainName();

   /**
    * Sets the value of the aopDomainName property.
    * 
    * @param value
    *     allowed object is
    *     {@link AopDomainNameType }
    *     
    */
   void setAopDomainName(String value);

   /**
    * Gets the value of the poolConfig property.
    * 
    * @return
    *     possible object is
    *     {@link PoolConfigMetaData }
    *     
    */
   PoolConfigMetaData getPoolConfig();

   /**
    * Sets the value of the poolConfig property.
    * 
    * @param value
    *     allowed object is
    *     {@link PoolConfigMetaData }
    *     
    */
   void setPoolConfig(PoolConfigMetaData value);

   /**
    * Gets the value of the jndiRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the jndiRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getJndiRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JndiRefMetaData }
    * 
    * 
    */
   List<JndiRefMetaData> getJndiRef();

   /**
    * Gets the value of the activationConfig property.
    * 
    * @return
    *     possible object is
    *     {@link ActivationConfigMetaData }
    *     
    */
   ActivationConfigMetaData getActivationConfig();

   /**
    * Sets the value of the activationConfig property.
    * 
    * @param value
    *     allowed object is
    *     {@link ActivationConfigMetaData }
    *     
    */
   void setActivationConfig(ActivationConfigMetaData value);

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   String getId();

   /**
    * Sets the value of the id property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   void setId(String value);

}
