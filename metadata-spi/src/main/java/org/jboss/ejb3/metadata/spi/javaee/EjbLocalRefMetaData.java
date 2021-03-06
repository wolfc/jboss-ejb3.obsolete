//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;

/**
 * Represents the metadata for ejb-local-ref element
 *
 * 	The ejb-local-refType is used by ejb-local-ref elements for
 * 	the declaration of a reference to an enterprise bean's local
 * 	home or to the local business interface of a 3.0 bean.
 *         The declaration consists of:
 *
 * 	    - an optional description
 * 	    - the EJB reference name used in the code of the Deployment
 * 	      Component that's referencing the enterprise bean.
 * 	    - the optional expected type of the referenced enterprise bean
 * 	    - the optional expected local interface of the referenced
 *               enterprise bean or the local business interface of the
 *               referenced enterprise bean.
 * 	    - the optional expected local home interface of the referenced
 *               enterprise bean. Not applicable if this ejb-local-ref refers
 *               to the local business interface of a 3.0 bean.
 * 	    - optional ejb-link information, used to specify the
 * 	      referenced enterprise bean
 *             - optional elements to define injection of the named enterprise
 *               bean into a component field or property.
 *
 *
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
 *         &lt;element name="ejb-ref-name" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-nameType"/>
 *         &lt;element name="ejb-ref-type" type="{http://java.sun.com/xml/ns/javaee}ejb-ref-typeType" minOccurs="0"/>
 *         &lt;element name="local-home" type="{http://java.sun.com/xml/ns/javaee}local-homeType" minOccurs="0"/>
 *         &lt;element name="local" type="{http://java.sun.com/xml/ns/javaee}localType" minOccurs="0"/>
 *         &lt;element name="ejb-link" type="{http://java.sun.com/xml/ns/javaee}ejb-linkType" minOccurs="0"/>
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
public interface EjbLocalRefMetaData extends IdMetaData
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
    * Returns the local EJB reference name
    *
    */
   String getEjbRefName();

   /**
    * Sets the local EJB reference name
    *
    * @param ejbRefName
    */
   void setEjbRefName(String ejbRefName);

   /**
    * Returns the type of the EJB reference.
    *
    */
   EjbRefType getEjbRefType();

   /**
    * Sets the type of the EJB reference
    *
    * @param ejbRefType The type of the EJB reference
    */
   void setEjbRefType(EjbRefType ejbRefType);

   /**
    * Returns the fully qualified classname of the local home interface
    * of this EJB reference. Returns null if there is no local home interface
    * for this EJB reference.
    *
    */
   String getLocalHome();

   /**
    * Sets the fully qualified classname of the local home interface of this
    * EJB reference.
    *
    * @param localHome Fully qualified classname of the local home interface of this
    * EJB reference
    */
   void setLocalHome(String localHome);

   /**
    * Returns the fully qualified classname of
    * EJB2.x local interface or the EJB3.x local business interface of
    * this EJB reference. Returns null if neither exists.
    */
   String getLocal();

   /**
    * Sets the fully qualified classname of the EJB2.x local interface or
    * EJB3.x local business interface of this EJB reference.
    *
    * @param localInterface Either the EJB2.x local interface or
    * the EJB3.x local business interface's fully qualified classname
    *
    */
   void setLocal(String value);

   /**
    * Returns the EJB name of the linked bean. Returns null if there is
    * no ejb-link specified.
    *
    *
    * The value of the ejb-link element must be the ejb-name of an
    * enterprise bean in the same ejb-jar file or in another ejb-jar
    * file in the same Java EE application unit.

    * Alternatively, the name in the ejb-link element may be
    * composed of a path name specifying the ejb-jar containing the
    * referenced enterprise bean with the ejb-name of the target
    * bean appended and separated from the path name by "#".  The
    * path name is relative to the Deployment File containing
    * Deployment Component that is referencing the enterprise
    * bean.  This allows multiple enterprise beans with the same
    * ejb-name to be uniquely identified.
    *
    *  Examples:
    *
    *      <ejb-link>EmployeeRecord</ejb-link>
    *
    *     <ejb-link>../products/product.jar#ProductEJB</ejb-link>
    *
    */
   String getEjbLink();

   /**
    * Sets the name of the linked EJB
    *
    *
    * @param ejbLink Name of the linked EJB
    * @see #getEjbLink()
    *
    *
    */
   void setEjbLink(String ejbLink);

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
