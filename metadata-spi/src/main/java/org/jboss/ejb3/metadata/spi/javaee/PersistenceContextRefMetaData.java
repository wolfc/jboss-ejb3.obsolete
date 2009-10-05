//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;

import javax.persistence.PersistenceContextType;

/**
 * Represents the metadata for persistence context references.
 *
 *
 * 	  The persistence-context-ref element contains a declaration
 * 	  of Deployment Component's reference to a persistence context
 * 	  associated within a Deployment Component's
 * 	  environment. It consists of:
 *
 * 		  - an optional description
 * 		  - the persistence context reference name
 * 		  - an optional persistence unit name.  If not specified,
 *                     the default persistence unit is assumed.
 * 		  - an optional specification as to whether
 * 		    the persistence context type is Transaction or
 * 		    Extended.  If not specified, Transaction is assumed.
 *                   - an optional list of persistence properties
 * 		  - optional injection targets
 *
 * 	  Examples:
 *
 *             <persistence-context-ref>
 *               <persistence-context-ref-name>myPersistenceContext
 *               </persistence-context-ref-name>
 *             </persistence-context-ref>
 *
 *             <persistence-context-ref>
 *               <persistence-context-ref-name>myPersistenceContext
 *                 </persistence-context-ref-name>
 *               <persistence-unit-name>PersistenceUnit1
 *                 </persistence-unit-name>
 *               <persistence-context-type>Extended</persistence-context-type>
 *             </persistence-context-ref>
 *
 *
 *
 *
 * <p>Java class for persistence-context-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-context-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="persistence-context-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="persistence-unit-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;element name="persistence-context-type" type="{http://java.sun.com/xml/ns/javaee}persistence-context-typeType" minOccurs="0"/>
 *         &lt;element name="persistence-property" type="{http://java.sun.com/xml/ns/javaee}propertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface PersistenceContextRefMetaData extends IdMetaData
{

   /**
    * Gets the value of the description property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the description property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getDescription().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link DescriptionMetaData }
    *
    *
    */
   List<DescriptionMetaData> getDescription();

   /**
    *
    * @return Returns the persistence context reference name
    *
    */
   String getPersistenceContextRefName();

   /**
    * Sets the persistence context reference name
    *
    * @param persistenceContextRefName The persistence context ref name
    *
    */
   void setPersistenceContextRefName(String persistenceContextRefName);

   /**
    * @return Returns the persistence unit name of this persistence context reference
    *
    */
   String getPersistenceUnitName();

   /**
    * Sets the persistence unit name of this persistence context reference
    *
    * @param persistenceUnitName The persistence unit name
    *
    */
   void setPersistenceUnitName(String value);

   /**
    * @return Returns the persistence context type
    *
    */
   PersistenceContextType getPersistenceContextType();

   /**
    * Sets the persistence context type of this persistence context reference
    *
    * @param persistenceContextType The persistence context type of this reference
    *
    */
   void setPersistenceContextType(PersistenceContextType persistenceContextType);

   /**
    * @return Returns the list of properties set for this persistence
    * context reference
    *
    *
    */
   List<PropertyMetaData> getPersistenceProperties();

   /**
    * Sets the persistence properties of this persistence context reference
    *
    * @param properties The properties of this persistence context reference
    */
   void setPersistenceProperties(List<PropertyMetaData> properties);

   /**
    * Returns the mapped-name of the EJB reference.
    * Returns null if the mapped-name is not set
    *
    */
   String getMappedName();

   /**
    * Sets the mapped-name of the EJB reference
    *
    * @param mappedName mapped-name of the EJB reference
    */
   void setMappedName(String mappedName);

   /**
    * Returns a list of injection target(s) metadata for
    * this EJB reference
    * Returns an empty list if there is no injection-target.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    */
   List<InjectionTargetMetaData> getInjectionTargets();

   /**
    * Sets the list of injection targets for this EJB reference
    *
    * @param injectionTargets List of injection targets metadata for this EJB reference
    */
   void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets);
}
