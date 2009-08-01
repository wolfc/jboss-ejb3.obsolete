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
 *         The application-exceptionType declares an application
 *         exception. The declaration consists of:
 *
 *             - the exception class. When the container receives
 *               an exception of this type, it is required to
 *               forward this exception as an applcation exception
 *               to the client regardless of whether it is a checked
 *               or unchecked exception.
 *             - an optional rollback element. If this element is
 *               set to true, the container must rollback the current
 *               transaction before forwarding the exception to the
 *               client.  If not specified, it defaults to false.
 *
 *
 *
 * <p>Java class for application-exceptionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="application-exceptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="exception-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="rollback" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface ApplicationExceptionMetaData extends IdMetaData
{

   /**
    * @return Returns the fully qualified name of the
    * exception class
    *
    */
   String getExceptionClassname();

   /**
    * Sets the fully qualified name of the exception class
    *
    * @param exceptionClass Fully qualified name of the exception
    * class
    *
    */
   void setExceptionClassname(String value);

   /**
    * Gets the value of the rollback property.
    *
    * @return Returns true if the current transaction has to be
    * rolled back if the exception represented by {@link #getExceptionClassname()}
    * is thrown
    *
    */
   boolean isRollback();

   /**
    * Set to true if the current transaction has to be
    * rolled back if the exception represented by {@link #getExceptionClassname()}
    * is thrown
    *
    * @param rollback True if the current transaction has to be rolled back, false
    * otherwise
    */
   void setRollback(boolean value);

}
