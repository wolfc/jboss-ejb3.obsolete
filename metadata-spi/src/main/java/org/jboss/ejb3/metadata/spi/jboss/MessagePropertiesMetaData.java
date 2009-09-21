//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

import org.jboss.ejb3.metadata.spi.javaee.IdMetaData;

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
public interface MessagePropertiesMetaData extends IdMetaData
{

   /**
    * @return     
    */
   String getClassName();

   /**
    * @param className
    *     
    */
   void setClassName(String className);

   /**
    * @return 
    *     
    */
   JBossMethodMetaData getMethod();

   /**
    * @param
    *     
    */
   void setMethod(JBossMethodMetaData value);

   /**
    * @return
    *     
    */
   String getDelivery();

   /**
    * @param 
    *     
    */
   void setDelivery(String delivery);

   /**
    * @return Returns the message priority
    *     
    */
   int getPriority();

   /**
    * Sets the message priority
    * 
    * @param priority
    */
   void setPriority(int priority);

}