//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;

import javax.xml.namespace.QName;

/**
 *
 *
 * 	Declares the handler for a port-component. Handlers can access the
 * 	init-param name/value pairs using the HandlerInfo interface. If
 * 	port-name is not specified, the handler is assumed to be associated
 * 	with all ports of the service.
 *
 * 	Used in: service-ref
 *
 *
 *
 * <p>Java class for service-ref_handlerType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="service-ref_handlerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="handler-name" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="handler-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="init-param" type="{http://java.sun.com/xml/ns/javaee}param-valueType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="soap-header" type="{http://java.sun.com/xml/ns/javaee}xsdQNameType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="soap-role" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="port-name" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface ServiceRef_HandlerMetaData extends IdMetaData
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
    * Gets the value of the displayName property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the displayName property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getDisplayName().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link DisplayNameMetaData }
    *
    *
    */
   List<DisplayNameMetaData> getDisplayName();

   /**
    * Gets the value of the icon property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the icon property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getIcon().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link IconType }
    *
    *
    */
   List<IconType> getIcon();

   /**
    * Gets the value of the handlerName property.
    *
    * @return
    *     possible object is
    *     {@link com.sun.java.xml.ns.javaee.String }
    *
    */
   String getHandlerName();

   /**
    * Sets the value of the handlerName property.
    *
    * @param value
    *     allowed object is
    *     {@link com.sun.java.xml.ns.javaee.String }
    *
    */
   void setHandlerName(String value);

   /**
    * Gets the value of the handlerClass property.
    *
    * @return
    *     possible object is
    *     {@link FullyQualifiedClassType }
    *
    */
   String getHandlerClass();

   /**
    * Sets the value of the handlerClass property.
    *
    * @param value
    *     allowed object is
    *     {@link FullyQualifiedClassType }
    *
    */
   void setHandlerClass(String value);

   /**
    * Gets the value of the initParam property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the initParam property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getInitParam().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link ParamValueMetaData }
    *
    *
    */
   List<ParamValueMetaData> getInitParam();

   /**
    * Gets the value of the soapHeader property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the soapHeader property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getSoapHeader().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link XsdQNameType }
    *
    *
    */
   List<QName> getSoapHeader();

   /**
    * Gets the value of the soapRole property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the soapRole property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getSoapRole().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link com.sun.java.xml.ns.javaee.String }
    *
    *
    */
   List<String> getSoapRole();

   /**
    * Gets the value of the portName property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the portName property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPortName().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link com.sun.java.xml.ns.javaee.String }
    *
    *
    */
   List<String> getPortName();



}
