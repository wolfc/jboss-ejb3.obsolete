//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

import java.util.List;

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;

/**
 *  The resource-manager element is used to provide a mapping between the "xml
 *             name" of a resource (res-name) and its "runtime jndi name" (res-jndi-name or res-url
 *             according to the type of the resource). If it is not provided, and if the type of the
 *             resource is javax.sql.DataSource, jboss will look for a javax.sql.DataSource in the jndi
 *             tree. See resource-managers. Used in: resource-managers 
 * 
 * <p>Java class for resource-managerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resource-managerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="res-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *         &lt;choice>
 *           &lt;element name="res-jndi-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *           &lt;element name="res-url" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="res-class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface ResourceManagerMetaData
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
    * @return Returns the name of the resource
    *     
    */
   String getResName();

   /**
    * Sets the name of the resource
    * 
    * @param resName Resource name
    *     
    */
   void setResName(String resName);

   /**
    * @return Returns the resource jndi name
    *     
    */
   String getResJndiName();

   /**
    * Sets the resource jndi name
    * 
    * @param jndiName
    *     
    */
   void setResJndiName(String jndiName);

   /**
    * @return Returns the resource URL
    *     
    */
   String getResUrl();

   /**
    * Sets the resource URL
    * 
    * @param resURL
    *     
    */
   void setResUrl(String resURL);

   /**
    * @return Returns the fully qualified classname of the resource
    * 
    * ex: javax.sql.DataSource
    *     
    */
   String getResClass();

   /**
    * Sets the resource class name
    * 
    * @param className Fully qualified classname of the resource (ex: javax.sql.DataSource)
    *     
    */
   void setResClass(String className);

}