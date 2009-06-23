//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

/**
 *  Represents the metadata for an injection-target
 *
 * 	An injection target specifies a class and a name within
 * 	that class into which a resource should be injected.
 *
 * 	The injection target class specifies the fully qualified
 * 	class name that is the target of the injection.  The
 * 	Java EE specifications describe which classes can be an
 * 	injection target.
 *
 * 	The injection target name specifies the target within
 * 	the specified class.  The target is first looked for as a
 * 	JavaBeans property name.  If not found, the target is
 * 	looked for as a field name.
 *
 * 	The specified resource will be injected into the target
 * 	during initialization of the class by either calling the
 * 	set method for the target property or by setting a value
 * 	into the named field.
 *
 *
 *
 * <p>Java class for injection-targetType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="injection-targetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="injection-target-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="injection-target-name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface InjectionTargetMetaData
{

   /**
    * Returns the fully qualified class name of the injection-target
    *
    */
   String getInjectionTargetClassname();

   /**
    * Sets the fully qualified class name of the injection-target
    *
    * @param classname Fully qualified class name of the injection target
    */
   void setInjectionTargetClass(String classname);

   /**
    * Returns the property/field name within the {@link #getInjectionTargetClassname()}
    * injection target classname.
    *
    */
   String getInjectionTargetName();

   /**
    * Sets the property/field name within the {@link #getInjectionTargetClassname()}
    * injection target classname.
    *
    * @param targetName
    *
    */
   void setInjectionTargetName(String targetName);

}
