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
 * 	The security-role-refType contains the declaration of a
 * 	security role reference in a component's or a
 * 	Deployment Component's code. The declaration consists of an
 * 	optional description, the security role name used in the
 * 	code, and an optional link to a security role. If the
 * 	security role is not specified, the Deployer must choose an
 * 	appropriate security role.
 * 
 *       
 * 
 * <p>Java class for security-role-refType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="security-role-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="role-name" type="{http://java.sun.com/xml/ns/javaee}role-nameType"/>
 *         &lt;element name="role-link" type="{http://java.sun.com/xml/ns/javaee}role-nameType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface SecurityRoleRefMetaData
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
    * Gets the value of the roleName property.
    * 
    * @return
    *     possible object is
    *     {@link RoleNameType }
    *     
    */
   String getRoleName();

   /**
    * Sets the value of the roleName property.
    * 
    * @param value
    *     allowed object is
    *     {@link RoleNameType }
    *     
    */
   void setRoleName(String value);

   /**
    * Gets the value of the roleLink property.
    * 
    * @return
    *     possible object is
    *     {@link RoleNameType }
    *     
    */
   String getRoleLink();

   /**
    * Sets the value of the roleLink property.
    * 
    * @param value
    *     allowed object is
    *     {@link RoleNameType }
    *     
    */
   void setRoleLink(String value);

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