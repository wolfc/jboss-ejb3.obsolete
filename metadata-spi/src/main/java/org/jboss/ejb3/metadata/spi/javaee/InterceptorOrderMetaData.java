//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;

/**
 *
 *         The interceptor-orderType element describes a total ordering
 *         of interceptor classes.
 *
 *
 * <p>Java class for interceptor-orderType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="interceptor-orderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="interceptor-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface InterceptorOrderMetaData
{

   /**
    * Returns the ordered list of fully qualified name of interceptor classes.
    *
    *
    */
   List<String> getOrderedInterceptorClasses();
   
   /**
    * Sets the ordered list of fully qualified name of interceptor classes.
    * 
    * @param orderedInterceptorClasses
    */
   void setOrderedInterceptorClasses(List<String> orderedInterceptorClasses);

}
