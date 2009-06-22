//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

import java.util.List;

import org.jboss.ejb3.metadata.spi.javaee.MessageDrivenBeanMetaData;

/**
 *  The message-driven element holds information specific to jboss and not
 *             declared in ejb-jar.xml about a message-driven bean, such as container configuration and
 *             resources. The bean should already be declared in ejb-jar.xml, with the same ejb-name.
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
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="activation-config" type="{http://www.jboss.com/xml/ns/javaee}activation-configType" minOccurs="0"/>
 *         &lt;element name="destination-jndi-name" type="{http://www.jboss.com/xml/ns/javaee}destination-jndi-nameType" minOccurs="0"/>
 *         &lt;element name="mdb-user" type="{http://www.jboss.com/xml/ns/javaee}mdb-userType" minOccurs="0"/>
 *         &lt;element name="mdb-passwd" type="{http://www.jboss.com/xml/ns/javaee}mdb-passwdType" minOccurs="0"/>
 *         &lt;element name="mdb-client-id" type="{http://www.jboss.com/xml/ns/javaee}mdb-client-idType" minOccurs="0"/>
 *         &lt;element name="mdb-subscription-id" type="{http://www.jboss.com/xml/ns/javaee}mdb-subscription-idType" minOccurs="0"/>
 *         &lt;element name="resource-adapter-name" type="{http://www.jboss.com/xml/ns/javaee}resource-adapter-nameType" minOccurs="0"/>
 *         &lt;element name="ejb-ref" type="{http://www.jboss.com/xml/ns/javaee}ejb-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-local-ref" type="{http://www.jboss.com/xml/ns/javaee}ejb-local-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="service-ref" type="{http://www.jboss.com/xml/ns/javaee}service-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="resource-ref" type="{http://www.jboss.com/xml/ns/javaee}resource-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="resource-env-ref" type="{http://www.jboss.com/xml/ns/javaee}resource-env-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="message-destination-ref" type="{http://www.jboss.com/xml/ns/javaee}message-destination-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-identity" type="{http://www.jboss.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *         &lt;element name="security-domain" type="{http://www.jboss.com/xml/ns/javaee}security-domainType" minOccurs="0"/>
 *         &lt;element name="method-attributes" type="{http://www.jboss.com/xml/ns/javaee}method-attributesType" minOccurs="0"/>
 *         &lt;element name="depends" type="{http://www.jboss.com/xml/ns/javaee}dependsType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-timeout-identity" type="{http://www.jboss.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *         &lt;element name="annotation" type="{http://www.jboss.com/xml/ns/javaee}annotationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ignore-dependency" type="{http://www.jboss.com/xml/ns/javaee}ignore-dependencyType" minOccurs="0"/>
 *         &lt;element name="aop-domain-name" type="{http://www.jboss.com/xml/ns/javaee}aop-domain-nameType" minOccurs="0"/>
 *         &lt;element name="pool-config" type="{http://www.jboss.com/xml/ns/javaee}pool-configType" minOccurs="0"/>
 *         &lt;element name="jndi-ref" type="{http://www.jboss.com/xml/ns/javaee}jndi-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="create-destination" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface JBossMessageDrivenBeanMetaData extends MessageDrivenBeanMetaData
{

   /**
    * Gets the value of the destinationJndiName property.
    * 
    * @return
    *     possible object is
    *     {@link DestinationJndiNameType }
    *     
    */
   String getDestinationJndiName();

   /**
    * Sets the value of the destinationJndiName property.
    * 
    * @param value
    *     allowed object is
    *     {@link DestinationJndiNameType }
    *     
    */
   void setDestinationJndiName(String value);

   /**
    * Gets the value of the mdbUser property.
    * 
    * @return
    *     possible object is
    *     {@link MdbUserType }
    *     
    */
   String getMdbUser();

   /**
    * Sets the value of the mdbUser property.
    * 
    * @param value
    *     allowed object is
    *     {@link MdbUserType }
    *     
    */
   void setMdbUser(String value);

   /**
    * Gets the value of the mdbPasswd property.
    * 
    * @return
    *     possible object is
    *     {@link MdbPasswdType }
    *     
    */
   String getMdbPasswd();

   /**
    * Sets the value of the mdbPasswd property.
    * 
    * @param value
    *     allowed object is
    *     {@link MdbPasswdType }
    *     
    */
   void setMdbPasswd(String value);

   /**
    * Gets the value of the mdbClientId property.
    * 
    * @return
    *     possible object is
    *     {@link MdbClientIdType }
    *     
    */
   String getMdbClientId();

   /**
    * Sets the value of the mdbClientId property.
    * 
    * @param value
    *     allowed object is
    *     {@link MdbClientIdType }
    *     
    */
   void setMdbClientId(String value);

   /**
    * Gets the value of the mdbSubscriptionId property.
    * 
    * @return
    *     possible object is
    *     {@link MdbSubscriptionIdType }
    *     
    */
   String getMdbSubscriptionId();

   /**
    * Sets the value of the mdbSubscriptionId property.
    * 
    * @param value
    *     allowed object is
    *     {@link MdbSubscriptionIdType }
    *     
    */
   void setMdbSubscriptionId(String value);

   /**
    * Gets the value of the resourceAdapterName property.
    * 
    * @return
    *     possible object is
    *     {@link ResourceAdapterNameType }
    *     
    */
   String getResourceAdapterName();

   /**
    * Sets the value of the resourceAdapterName property.
    * 
    * @param value
    *     allowed object is
    *     {@link ResourceAdapterNameType }
    *     
    */
   void setResourceAdapterName(String value);

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
    * Gets the value of the ejbTimeoutIdentity property.
    * 
    * @return
    *     possible object is
    *     {@link JBossSecurityIdentityMetaData }
    *     
    */
   JBossSecurityIdentityMetaData getEjbTimeoutIdentity();

   /**
    * Sets the value of the ejbTimeoutIdentity property.
    * 
    * @param value
    *     allowed object is
    *     {@link JBossSecurityIdentityMetaData }
    *     
    */
   void setEjbTimeoutIdentity(JBossSecurityIdentityMetaData value);

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
    * Gets the value of the createDestination property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   Boolean isCreateDestination();

   /**
    * Sets the value of the createDestination property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   void setCreateDestination(Boolean value);

}
