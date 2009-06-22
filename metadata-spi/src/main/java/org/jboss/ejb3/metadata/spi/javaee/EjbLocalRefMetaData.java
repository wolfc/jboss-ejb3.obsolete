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
public interface EjbLocalRefMetaData
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
    * Gets the value of the ejbRefName property.
    * 
    * @return
    *     possible object is
    *     {@link EjbRefNameType }
    *     
    */
   String getEjbRefName();

   /**
    * Sets the value of the ejbRefName property.
    * 
    * @param value
    *     allowed object is
    *     {@link EjbRefNameType }
    *     
    */
   void setEjbRefName(String value);

   /**
    * Gets the value of the ejbRefType property.
    * 
    * @return
    *     possible object is
    *     {@link EjbRefTypeType }
    *     
    */
   String getEjbRefType();

   /**
    * Sets the value of the ejbRefType property.
    * 
    * @param value
    *     allowed object is
    *     {@link EjbRefTypeType }
    *     
    */
   void setEjbRefType(String value);

   /**
    * Gets the value of the localHome property.
    * 
    * @return
    *     possible object is
    *     {@link LocalHomeType }
    *     
    */
   String getLocalHome();

   /**
    * Sets the value of the localHome property.
    * 
    * @param value
    *     allowed object is
    *     {@link LocalHomeType }
    *     
    */
   void setLocalHome(String value);

   /**
    * Gets the value of the local property.
    * 
    * @return
    *     possible object is
    *     {@link LocalType }
    *     
    */
   String getLocal();

   /**
    * Sets the value of the local property.
    * 
    * @param value
    *     allowed object is
    *     {@link LocalType }
    *     
    */
   void setLocal(String value);

   /**
    * Gets the value of the ejbLink property.
    * 
    * @return
    *     possible object is
    *     {@link EjbLinkType }
    *     
    */
   String getEjbLink();

   /**
    * Sets the value of the ejbLink property.
    * 
    * @param value
    *     allowed object is
    *     {@link EjbLinkType }
    *     
    */
   void setEjbLink(String value);

   /**
    * Gets the value of the mappedName property.
    * 
    * @return
    *     possible object is
    *     {@link XsdStringType }
    *     
    */
   String getMappedName();

   /**
    * Sets the value of the mappedName property.
    * 
    * @param value
    *     allowed object is
    *     {@link XsdStringType }
    *     
    */
   void setMappedName(String value);

   /**
    * Gets the value of the injectionTarget property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the injectionTarget property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getInjectionTarget().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link InjectionTargetMetaData }
    * 
    * 
    */
   List<InjectionTargetMetaData> getInjectionTarget();

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link java.lang.String }
    *     
    */
   java.lang.String getId();

   /**
    * Sets the value of the id property.
    * 
    * @param value
    *     allowed object is
    *     {@link java.lang.String }
    *     
    */
   void setId(java.lang.String value);

}
