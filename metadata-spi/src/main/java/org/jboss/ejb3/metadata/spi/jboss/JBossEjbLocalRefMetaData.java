//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

import org.jboss.ejb3.metadata.spi.javaee.EjbLocalRefMetaData;

/**
 *  The ejb-local-ref element is used to give the jndi-name of an external ejb
 *             reference. In the case of an external ejb reference, you don't provide a ejb-link element in
 *             ejb-jar.xml, but you provide a jndi-name in jboss.xml 
 * 
 * <p>Java class for ejb-local-refType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ejb-local-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-ref-name" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-nameType" minOccurs="0"/>
 *         &lt;element name="ejb-ref-type" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-typeType" minOccurs="0"/>
 *         &lt;element name="local-home" type="{http://java.sun.com/xml/ns/javaee}local-homeType" minOccurs="0"/>
 *         &lt;element name="local" type="{http://java.sun.com/xml/ns/javaee}localType" minOccurs="0"/>
 *         &lt;element name="ejb-link" type="{http://java.sun.com/xml/ns/javaee}ejb-linkType" minOccurs="0"/>
 *         &lt;element name="local-jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
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
public interface JBossEjbLocalRefMetaData extends EjbLocalRefMetaData
{

   /**
    * @return Returns the jndi name of this ejb-local-ref
    *     
    */
   String getLocalJndiName();

   /**
    * Sets the jndi name of this ejb-local-ref
    * 
    * @param localJndiName 
    *     
    */
   void setLocalJndiName(String localJndiName);

   /**
    * Returns the jndi name of this ejb-local-ref
    * 
    * @return
    *     
    */
   String getJndiName();

   /**
    * Sets the jndi name of this ejb-local-ref
    * 
    * @param jndiName
    *     
    */
   void setJndiName(String jndiName);

   /**
    * @return Returns true if ignore dependency is set for this bean. Else
    * returns false
    * 
    *     
    */
   boolean isIgnoreDependency();

   /**
    * Set to true if the dependencies on this bean are to be 
    * ignored. Else set to false.
    * 
    * @param ignoreDependency
    *     
    */
   void setIgnoreDependency(boolean ignoreDependency);

}
