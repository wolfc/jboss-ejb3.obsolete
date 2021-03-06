//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

/**
 *
 *  Represents the metadata for the around-invoke element of a bean
 *
 *         The around-invoke type specifies a method on a
 *         class to be called during the around invoke portion of an
 *         ejb invocation.  Note that each class may have only one
 *         around invoke method and that the method may not be
 *         overloaded.
 *
 *         If the class element is missing then
 *         the class defining the callback is assumed to be the
 *         interceptor class or component class in scope at the
 *         location in the descriptor in which the around invoke
 *         definition appears.
 *
 *
 *
 * <p>Java class for around-invokeType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="around-invokeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="method-name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface AroundInvokeMetaData
{

   /**
    * Returns the fully qualified classname of the class
    * whose method will be called during the around invoke portion
    * of the EJB invocation
    *
    */
   String getClassname();

   /**
    * Sets the fully qualified classname of the class whose
    * method will be called during the around invoke portion
    * of the EJB invocation
    *
    * @param classname Fully qualified classname
    *
    */
   void setClassname(String classname);

   /**
    * Returns the method name of the around-invoke method
    *
    */
   String getMethodName();

   /**
    * Sets the method name of the around-invoke method
    */
   void setMethodName(String methodName);

}
