//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;

/**
 * Represents the metadata for method params.
 *
 * 	The method-paramsType defines a list of the
 * 	fully-qualified Java type names of the method parameters.
 *
 *
 *
 * <p>Java class for method-paramsType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="method-paramsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="method-param" type="{http://java.sun.com/xml/ns/javaee}java-typeType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface MethodParamsMetaData extends IdMetaData
{

   /**
    * Returns the list of fully qualified classnames of each of the
    * method param types. Returns an empty list if there are no params for a method.
    *
    * Its upto the implementations to return either a modifiable or
    * an unmodifiable {@link List}
    *
    */
   List<String> getMethodParams();


   /**
    * Sets the list of fully qualified classnames of the each of the method param types.
    *
    * @param methodParams
    */
   void setMethodParams(List<String> methodParams);


}
