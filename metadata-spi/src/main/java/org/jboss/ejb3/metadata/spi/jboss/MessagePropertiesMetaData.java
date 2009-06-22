//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

/**
 *  Element for defining JMS message properties (e.g. persistence, priority)
 *             for a consumer bean Used in: consumer 
 * 
 * <p>Java class for message-propertiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="message-propertiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="class" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="method" type="{http://www.jboss.com/xml/ns/javaee}methodType"/>
 *         &lt;element name="delivery" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="priority" type="{http://java.sun.com/xml/ns/javaee}xsdIntegerType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface MessagePropertiesMetaData
{

   /**
    * Gets the value of the clazz property.
    * 
    * @return
    *     possible object is
    *     {@link XsdStringType }
    *     
    */
   String getClazz();

   /**
    * Sets the value of the clazz property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdStringType }
    *     
    */
   void setClazz(String value);

   /**
    * Gets the value of the method property.
    * 
    * @return
    *     possible object is
    *     {@link MethodMetaData }
    *     
    */
   MethodMetaData getMethod();

   /**
    * Sets the value of the method property.
    * 
    * @param value
    *     allowed object is
    *     {@link MethodMetaData }
    *     
    */
   void setMethod(MethodMetaData value);

   /**
    * Gets the value of the delivery property.
    * 
    * @return
    *     possible object is
    *     {@link XsdStringType }
    *     
    */
   String getDelivery();

   /**
    * Sets the value of the delivery property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdStringType }
    *     
    */
   void setDelivery(String value);

   /**
    * Gets the value of the priority property.
    * 
    * @return
    *     possible object is
    *     {@link XsdIntegerType }
    *     
    */
   int getPriority();

   /**
    * Sets the value of the priority property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdIntegerType }
    *     
    */
   void setPriority(int value);

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