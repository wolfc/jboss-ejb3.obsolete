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
 * 
 *             
 *           Element for specifying the local jndi binding for a bean.
 * 	    
 *          
 * 
 * <p>Java class for local-bindingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="local-bindingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface LocalBindingMetaData
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

}