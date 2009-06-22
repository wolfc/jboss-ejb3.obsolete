//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.ejb3.metadata.spi.javaee.AssemblyDescriptorMetaData;
import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.DisplayNameMetaData;
import org.jboss.ejb3.metadata.spi.javaee.EnterpriseBeansMetaData;
import org.jboss.ejb3.metadata.spi.javaee.IconType;

/**
 *  The jboss element is the root element of the jboss.xml file. It contains
 *             all the information used by jboss but not described in the ejb-jar.xml file. All of it is
 *             optional. 1- the application assembler can define custom container configurations for the
 *             beans. Standard configurations are provided in standardjboss.xml 2- the deployer can
 *             override the jndi names under which the beans are deployed 3- the deployer can specify
 *             runtime jndi names for resource managers. 
 * 
 * <p>Java class for jbossType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="jbossType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="loader-repository" type="{http://www.jboss.com/xml/ns/javaee}loader-repositoryType" minOccurs="0"/>
 *         &lt;element name="jmx-name" type="{http://www.jboss.com/xml/ns/javaee}jmx-nameType" minOccurs="0"/>
 *         &lt;element name="security-domain" type="{http://www.jboss.com/xml/ns/javaee}security-domainType" minOccurs="0"/>
 *         &lt;element name="missing-method-permissions-excluded-mode" type="{http://www.jboss.com/xml/ns/javaee}missing-method-permissions-excluded-modeType" minOccurs="0"/>
 *         &lt;element name="unauthenticated-principal" type="{http://www.jboss.com/xml/ns/javaee}unauthenticated-principalType" minOccurs="0"/>
 *         &lt;element name="jndi-binding-policy" type="{http://www.jboss.com/xml/ns/javaee}jndi-binding-policyType" minOccurs="0"/>
 *         &lt;element name="jacc-context-id" type="{http://www.jboss.com/xml/ns/javaee}jacc-context-idType" minOccurs="0"/>
 *         &lt;element name="webservices" type="{http://www.jboss.com/xml/ns/javaee}webservicesType" minOccurs="0"/>
 *         &lt;element name="enterprise-beans" type="{http://www.jboss.com/xml/ns/javaee}enterprise-beansType" minOccurs="0"/>
 *         &lt;element name="assembly-descriptor" type="{http://www.jboss.com/xml/ns/javaee}assembly-descriptorType" minOccurs="0"/>
 *         &lt;element name="resource-managers" type="{http://www.jboss.com/xml/ns/javaee}resource-managersType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://java.sun.com/xml/ns/javaee}dewey-versionType" fixed="5.1" />
 *       &lt;attribute name="metadata-complete" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface JBossMetaData
{

   /**
    * 
    * 
    */
   @XmlAttribute
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   public final static String VERSION = "5.1";

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
    * Gets the value of the loaderRepository property.
    * 
    * @return
    *     possible object is
    *     {@link LoaderRepositoryMetaData }
    *     
    */
   LoaderRepositoryMetaData getLoaderRepository();

   /**
    * Sets the value of the loaderRepository property.
    * 
    * @param value
    *     allowed object is
    *     {@link LoaderRepositoryMetaData }
    *     
    */
   void setLoaderRepository(LoaderRepositoryMetaData value);

   /**
    * Gets the value of the jmxName property.
    * 
    * @return
    *     possible object is
    *     {@link JmxNameType }
    *     
    */
   String getJmxName();

   /**
    * Sets the value of the jmxName property.
    * 
    * @param value
    *     allowed object is
    *     {@link JmxNameType }
    *     
    */
   void setJmxName(String value);

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
    * Gets the value of the missingMethodPermissionsExcludedMode property.
    * 
    * @return
    *     possible object is
    *     {@link MissingMethodPermissionsExcludedModeType }
    *     
    */
   boolean getMissingMethodPermissionsExcludedMode();

   /**
    * Sets the value of the missingMethodPermissionsExcludedMode property.
    * 
    * @param value
    *     allowed object is
    *     {@link MissingMethodPermissionsExcludedModeType }
    *     
    */
   void setMissingMethodPermissionsExcludedMode(boolean value);

   /**
    * Gets the value of the unauthenticatedPrincipal property.
    * 
    * @return
    *     possible object is
    *     {@link UnauthenticatedPrincipalType }
    *     
    */
   String getUnauthenticatedPrincipal();

   /**
    * Sets the value of the unauthenticatedPrincipal property.
    * 
    * @param value
    *     allowed object is
    *     {@link UnauthenticatedPrincipalType }
    *     
    */
   void setUnauthenticatedPrincipal(String value);

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
    * Gets the value of the jaccContextId property.
    * 
    * @return
    *     possible object is
    *     {@link JaccContextIdType }
    *     
    */
   String getJaccContextId();

   /**
    * Sets the value of the jaccContextId property.
    * 
    * @param value
    *     allowed object is
    *     {@link JaccContextIdType }
    *     
    */
   void setJaccContextId(String value);

   /**
    * Gets the value of the webservices property.
    * 
    * @return
    *     possible object is
    *     {@link WebservicesMetaData }
    *     
    */
   WebservicesMetaData getWebservices();

   /**
    * Sets the value of the webservices property.
    * 
    * @param value
    *     allowed object is
    *     {@link WebservicesMetaData }
    *     
    */
   void setWebservices(WebservicesMetaData value);

   /**
    * Gets the value of the enterpriseBeans property.
    * 
    * @return
    *     possible object is
    *     {@link EnterpriseBeansMetaData }
    *     
    */
   EnterpriseBeansMetaData getEnterpriseBeans();

   /**
    * Sets the value of the enterpriseBeans property.
    * 
    * @param value
    *     allowed object is
    *     {@link EnterpriseBeansMetaData }
    *     
    */
   void setEnterpriseBeans(EnterpriseBeansMetaData value);

   /**
    * Gets the value of the assemblyDescriptor property.
    * 
    * @return
    *     possible object is
    *     {@link AssemblyDescriptorMetaData }
    *     
    */
   AssemblyDescriptorMetaData getAssemblyDescriptor();

   /**
    * Sets the value of the assemblyDescriptor property.
    * 
    * @param value
    *     allowed object is
    *     {@link AssemblyDescriptorMetaData }
    *     
    */
   void setAssemblyDescriptor(AssemblyDescriptorMetaData value);

   /**
    * Gets the value of the resourceManagers property.
    * 
    * @return
    *     possible object is
    *     {@link ResourceManagersMetaData }
    *     
    */
   ResourceManagersMetaData getResourceManagers();

   /**
    * Sets the value of the resourceManagers property.
    * 
    * @param value
    *     allowed object is
    *     {@link ResourceManagersMetaData }
    *     
    */
   void setResourceManagers(ResourceManagersMetaData value);

   /**
    * Gets the value of the metadataComplete property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   Boolean isMetadataComplete();

   /**
    * Sets the value of the metadataComplete property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   void setMetadataComplete(Boolean value);

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
