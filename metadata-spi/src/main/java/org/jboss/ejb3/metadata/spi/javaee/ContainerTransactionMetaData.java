//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;
import java.util.Set;

/**
 *
 *
 * 	The container-transactionType specifies how the container
 * 	must manage transaction scopes for the enterprise bean's
 * 	method invocations. It defines an optional description, a
 * 	list of method elements, and a transaction attribute. The
 * 	transaction attribute is to be applied to all the specified
 * 	methods.
 *
 *
 *
 * <p>Java class for container-transactionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="container-transactionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="method" type="{http://java.sun.com/xml/ns/javaee}methodType" maxOccurs="unbounded"/>
 *         &lt;element name="trans-attribute" type="{http://java.sun.com/xml/ns/javaee}trans-attributeType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface ContainerTransactionMetaData extends IdMetaData
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
    * @return Returns the methods which are configured for container managed transactions
    *
    * Returns an empty list if no methods have been configured for CMT
    *
    * Note that its upto the implementations to either return a modifiable
    * {@link Set} or an umodifiable one.
    *
    */
   List<EjbMethodMetaData> getMethods();

   /**
    *
    * @param methods Sets the methods for container managed transactions
    *
    */
   void setMethods(List<EjbMethodMetaData> methods);

   /**
    * @return Returns the {@link TransactionAttribute} associated with the
    * methods returned by {@link #getMethods()}
    *
    */
   TransactionAttribute getTransAttribute();

   /**
    * Sets the {@link TransactionAttribute} associated with the methods returned by
    * {@link #getMethods()}
    *
    * @param txAttribute {@link TransactionAttribute} associated with the
    * method of the bean
    */
   void setTransAttribute(TransactionAttribute txAttribute);


}