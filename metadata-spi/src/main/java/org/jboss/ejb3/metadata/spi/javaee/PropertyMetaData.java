//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.javaee;

/**
 * 
 * 
 * 	Specifies a name/value pair.
 * 
 *       
 * 
 * <p>Java class for propertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="propertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *         &lt;element name="value" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface PropertyMetaData
{

   /**
    * Gets the value of the name property.
    * 
    * @return
    *     possible object is
    *     {@link XsdStringType }
    *     
    */
   String getName();

   /**
    * Sets the value of the name property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdStringType }
    *     
    */
   void setName(String value);

   /**
    * Gets the value of the value property.
    * 
    * @return
    *     possible object is
    *     {@link XsdStringType }
    *     
    */
   String getValue();

   /**
    * Sets the value of the value property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdStringType }
    *     
    */
   void setValue(String value);

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link java.lang.String }
    *     
    */
   java.lang.String getId();

   /**
    * Sets the value of the id property.
    * 
    * @param value
    *     allowed object is
    *     {@link java.lang.String }
    *     
    */
   void setId(java.lang.String value);

}