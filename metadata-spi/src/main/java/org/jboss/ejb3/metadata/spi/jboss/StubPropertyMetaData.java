//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

/**
 * <p>Java class for stub-propertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="stub-propertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="prop-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="prop-value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface StubPropertyMetaData
{

   /**
    * 
    * @return Returns the property name
    *     
    */
   String getPropName();

   /**
    * Sets the property name
    * 
    * @param propName
    *     
    */
   void setPropName(String propName);

   /**
    * Returns the property value
    * 
    * @return propValue 
    * 
    *     
    */
   String getPropValue();

   /**
    * Sets the property value
    * 
    * @param propValue
    *     
    */
   void setPropValue(String propValue);

}
