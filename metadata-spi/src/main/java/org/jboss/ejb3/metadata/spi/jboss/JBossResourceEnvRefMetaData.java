//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.jboss;

import org.jboss.ejb3.metadata.spi.javaee.ResourceEnvRefMetaData;

/**
 * <p>Java class for resource-env-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-env-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="resource-env-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="resource-env-ref-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}resourceGroup"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface JBossResourceEnvRefMetaData extends ResourceEnvRefMetaData
{

   /**
    * @return Returns the jndi name of this resource env-ref
    *
    */
   String getJndiName();

   /**
    * Sets the jndi name of this resource env-ref
    *
    * @param jndiName
    *
    */
   void setJndiName(String jndiName);

   /**
    * @return Returns true if ignore dependency is set. Else
    * returns false
    * 
    *     
    */
   boolean isIgnoreDependency();

   /**
    * Set to true if the dependencies are to be 
    * ignored. Else set to false.
    * 
    * @param ignoreDependency
    *     
    */
   void setIgnoreDependency(boolean ignoreDependency);

}
