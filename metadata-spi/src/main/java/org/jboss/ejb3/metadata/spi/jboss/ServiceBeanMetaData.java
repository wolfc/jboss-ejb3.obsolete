//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

import java.util.List;

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.DisplayNameMetaData;
import org.jboss.ejb3.metadata.spi.javaee.IconType;
import org.jboss.ejb3.metadata.spi.javaee.LifecycleCallbackMetaData;

/**
 *  The service element holds all of the information specific about a service
 *             bean which is a JBoss proprietary extension to EJB3 creating multithreaded, singleton
 *             services. Service beans are the EJB3 analogy for JMX MBeans. 
 * 
 * <p>Java class for service-beanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="service-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="business-local" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="business-remote" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType" minOccurs="0"/>
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="security-identity" type="{http://www.jboss.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *         &lt;element name="object-name" type="{http://www.jboss.com/xml/ns/javaee}jmx-nameType" minOccurs="0"/>
 *         &lt;element name="management" type="{http://www.jboss.com/xml/ns/javaee}managementType" minOccurs="0"/>
 *         &lt;element name="xmbean" type="{http://www.jboss.com/xml/ns/javaee}xmbeanType" minOccurs="0"/>
 *         &lt;element name="local-binding" type="{http://www.jboss.com/xml/ns/javaee}local-bindingType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="remote-binding" type="{http://www.jboss.com/xml/ns/javaee}remote-bindingType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
 *         &lt;element name="home-jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
 *         &lt;element name="local-jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
 *         &lt;element name="jndi-binding-policy" type="{http://www.jboss.com/xml/ns/javaee}jndi-binding-policyType" minOccurs="0"/>
 *         &lt;element name="clustered" type="{http://www.jboss.com/xml/ns/javaee}clusteredType" minOccurs="0"/>
 *         &lt;element name="cluster-config" type="{http://www.jboss.com/xml/ns/javaee}cluster-configType" minOccurs="0"/>
 *         &lt;element name="security-domain" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="method-attributes" type="{http://www.jboss.com/xml/ns/javaee}method-attributesType" minOccurs="0"/>
 *         &lt;element name="depends" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="annotation" type="{http://www.jboss.com/xml/ns/javaee}annotationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ignore-dependency" type="{http://www.jboss.com/xml/ns/javaee}ignore-dependencyType" minOccurs="0"/>
 *         &lt;element name="aop-domain-name" type="{http://www.jboss.com/xml/ns/javaee}aop-domain-nameType" minOccurs="0"/>
 *         &lt;element name="pool-config" type="{http://www.jboss.com/xml/ns/javaee}pool-configType" minOccurs="0"/>
 *         &lt;element name="concurrent" type="{http://www.jboss.com/xml/ns/javaee}concurrentType" minOccurs="0"/>
 *         &lt;element name="jndi-ref" type="{http://www.jboss.com/xml/ns/javaee}jndi-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="port-component" type="{http://www.jboss.com/xml/ns/javaee}port-componentType" minOccurs="0"/>
 *         &lt;element name="ejb-timeout-identity" type="{http://www.jboss.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface ServiceBeanMetaData
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
    * Gets the value of the mappedName property.
    * 
    * @return
    *     possible object is
    *     {@link XsdStringType }
    *     
    */
   String getMappedName();

   /**
    * Sets the value of the mappedName property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdStringType }
    *     
    */
   void setMappedName(String value);

   /**
    * Gets the value of the businessLocal property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the businessLocal property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getBusinessLocal().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link FullyQualifiedClassType }
    * 
    * 
    */
   List<String> getBusinessLocal();

   /**
    * Gets the value of the businessRemote property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the businessRemote property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getBusinessRemote().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link FullyQualifiedClassType }
    * 
    * 
    */
   List<String> getBusinessRemote();

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
    * Gets the value of the envEntry property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the envEntry property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getEnvEntry().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossEnvEntryMetaData }
    * 
    * 
    */
   List<JBossEnvEntryMetaData> getEnvEntry();

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
    * Gets the value of the serviceRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the serviceRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getServiceRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link ServiceRefMetaData }
    * 
    * 
    */
   List<ServiceRefMetaData> getServiceRef();

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
    * Gets the value of the persistenceContextRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the persistenceContextRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPersistenceContextRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossPersistenceContextRefMetaData }
    * 
    * 
    */
   List<JBossPersistenceContextRefMetaData> getPersistenceContextRef();

   /**
    * Gets the value of the persistenceUnitRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the persistenceUnitRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPersistenceUnitRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link JBossPersistenceUnitRefMetaData }
    * 
    * 
    */
   List<JBossPersistenceUnitRefMetaData> getPersistenceUnitRef();

   /**
    * Gets the value of the postConstruct property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the postConstruct property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPostConstruct().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LifecycleCallbackMetaData }
    * 
    * 
    */
   List<LifecycleCallbackMetaData> getPostConstruct();

   /**
    * Gets the value of the preDestroy property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the preDestroy property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPreDestroy().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LifecycleCallbackMetaData }
    * 
    * 
    */
   List<LifecycleCallbackMetaData> getPreDestroy();

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
    * Gets the value of the objectName property.
    * 
    * @return
    *     possible object is
    *     {@link JmxNameType }
    *     
    */
   String getObjectName();

   /**
    * Sets the value of the objectName property.
    * 
    * @param value
    *     allowed object is
    *     {@link JmxNameType }
    *     
    */
   void setObjectName(String value);

   /**
    * Gets the value of the management property.
    * 
    * @return
    *     possible object is
    *     {@link ManagementType }
    *     
    */
   String getManagement();

   /**
    * Sets the value of the management property.
    * 
    * @param value
    *     allowed object is
    *     {@link ManagementType }
    *     
    */
   void setManagement(String value);

   /**
    * Gets the value of the xmbean property.
    * 
    * @return
    *     possible object is
    *     {@link XmbeanType }
    *     
    */
   String getXmbean();

   /**
    * Sets the value of the xmbean property.
    * 
    * @param value
    *     allowed object is
    *     {@link XmbeanType }
    *     
    */
   void setXmbean(String value);

   /**
    * Gets the value of the localBinding property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the localBinding property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getLocalBinding().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalBindingMetaData }
    * 
    * 
    */
   List<LocalBindingMetaData> getLocalBinding();

   /**
    * Gets the value of the remoteBinding property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the remoteBinding property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getRemoteBinding().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link RemoteBindingMetaData }
    * 
    * 
    */
   List<RemoteBindingMetaData> getRemoteBinding();

   /**
    * Gets the value of the jndiName property.
    * 
    * @return
    *     possible object is
    *     {@link JndiNameType }
    *     
    */
   String getJndiName();

   /**
    * Sets the value of the jndiName property.
    * 
    * @param value
    *     allowed object is
    *     {@link JndiNameType }
    *     
    */
   void setJndiName(String value);

   /**
    * Gets the value of the homeJndiName property.
    * 
    * @return
    *     possible object is
    *     {@link JndiNameType }
    *     
    */
   String getHomeJndiName();

   /**
    * Sets the value of the homeJndiName property.
    * 
    * @param value
    *     allowed object is
    *     {@link JndiNameType }
    *     
    */
   void setHomeJndiName(String value);

   /**
    * Gets the value of the localJndiName property.
    * 
    * @return
    *     possible object is
    *     {@link JndiNameType }
    *     
    */
   String getLocalJndiName();

   /**
    * Sets the value of the localJndiName property.
    * 
    * @param value
    *     allowed object is
    *     {@link JndiNameType }
    *     
    */
   void setLocalJndiName(String value);

   /**
    * Gets the value of the jndiBindingPolicy property.
    * 
    * @return
    *     possible object is
    *     {@link JndiBindingPolicyType }
    *     
    */
   String getJndiBindingPolicy();

   /**
    * Sets the value of the jndiBindingPolicy property.
    * 
    * @param value
    *     allowed object is
    *     {@link JndiBindingPolicyType }
    *     
    */
   void setJndiBindingPolicy(String value);

   /**
    * Gets the value of the clustered property.
    * 
    * @return
    *     possible object is
    *     {@link ClusteredType }
    *     
    */
   boolean getClustered();

   /**
    * Sets the value of the clustered property.
    * 
    * @param value
    *     allowed object is
    *     {@link ClusteredType }
    *     
    */
   void setClustered(boolean value);

   /**
    * Gets the value of the clusterConfig property.
    * 
    * @return
    *     possible object is
    *     {@link ClusterConfigMetaData }
    *     
    */
   ClusterConfigMetaData getClusterConfig();

   /**
    * Sets the value of the clusterConfig property.
    * 
    * @param value
    *     allowed object is
    *     {@link ClusterConfigMetaData }
    *     
    */
   void setClusterConfig(ClusterConfigMetaData value);

   /**
    * Gets the value of the securityDomain property.
    * 
    * @return
    *     possible object is
    *     {@link XsdStringType }
    *     
    */
   String getSecurityDomain();

   /**
    * Sets the value of the securityDomain property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdStringType }
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
    * {@link XsdStringType }
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
    * Gets the value of the concurrent property.
    * 
    * @return
    *     possible object is
    *     {@link ConcurrentType }
    *     
    */
   boolean getConcurrent();

   /**
    * Sets the value of the concurrent property.
    * 
    * @param value
    *     allowed object is
    *     {@link ConcurrentType }
    *     
    */
   void setConcurrent(boolean value);

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
    * Gets the value of the portComponent property.
    * 
    * @return
    *     possible object is
    *     {@link PortComponentMetaData }
    *     
    */
   PortComponentMetaData getPortComponent();

   /**
    * Sets the value of the portComponent property.
    * 
    * @param value
    *     allowed object is
    *     {@link PortComponentMetaData }
    *     
    */
   void setPortComponent(PortComponentMetaData value);

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