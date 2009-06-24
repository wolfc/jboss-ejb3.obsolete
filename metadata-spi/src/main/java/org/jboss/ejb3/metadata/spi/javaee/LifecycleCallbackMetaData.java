//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

/**
 *  Represents the lifecycle-callback metadata (ex: post-construct)
 *
 * 	The lifecycle-callback type specifies a method on a
 * 	class to be called when a lifecycle event occurs.
 * 	Note that each class may have only one lifecycle callback
 *         method for any given event and that the method may not
 * 	be overloaded.
 *
 *         If the lifefycle-callback-class element is missing then
 *         the class defining the callback is assumed to be the
 *         component class in scope at the place in the descriptor
 *         in which the callback definition appears.
 *
 *
 *
 * <p>Java class for lifecycle-callbackType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="lifecycle-callbackType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lifecycle-callback-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="lifecycle-callback-method" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface LifecycleCallbackMetaData
{

   /**
    *
    * @return Returns the fully qualified classname of the lifecycle-callback
    * class
    *
    */
   String getLifecycleCallbackClass();

   /**
    * Sets the fully qualified classname of the lifecycle-callback
    * class
    *
    * @param lifecycleCallbackClass Fully qualified classname of the lifecycle callback
    * class
    *
    */
   void setLifecycleCallbackClass(String lifecycleCallbackClass);

   /**
    * @return Returns the method name of the lifecycle callback method
    */
   String getLifecycleCallbackMethod();

   /**
    * Sets the method name of the lifecycle callback method
    *
    * @param methodName The lifecycle callback method name
    *
    */
   void setLifecycleCallbackMethod(String methodName);

}
