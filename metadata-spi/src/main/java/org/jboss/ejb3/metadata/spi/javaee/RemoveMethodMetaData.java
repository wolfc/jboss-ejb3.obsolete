//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.javaee;

/**
 * <p>Java class for remove-methodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remove-methodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bean-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType"/>
 *         &lt;element name="retain-if-exception" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface RemoveMethodMetaData extends IdMetaData
{

   /**
    * Returns the remove method on the bean
    *     
    */
   NamedMethodMetaData getBeanMethod();

   /**
    * Sets the remove method of the bean
    * 
    * @param removedMethod
    *     
    */
   void setBeanMethod(NamedMethodMetaData removeMethod);

   /**
    * Returns true if the bean has to be retained on exception.
    * Else returns false.
    *     
    */
   boolean isRetainIfException();

   /**
    * Set to true if the bean has to be retained on exception.
    * Else set to false.
    * 
    * @param retainIfException
    *     
    */
   void setRetainIfException(boolean retainIfException);

   

}
