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
 *
 * 	  The ejb-relationship-roleType describes a role within a
 * 	  relationship. There are two roles in each relationship.
 *
 * 	  The ejb-relationship-roleType contains an optional
 * 	  description; an optional name for the relationship role; a
 * 	  specification of the multiplicity of the role; an optional
 * 	  specification of cascade-delete functionality for the role;
 * 	  the role source; and a declaration of the cmr-field, if any,
 * 	  by means of which the other side of the relationship is
 * 	  accessed from the perspective of the role source.
 *
 * 	  The multiplicity and role-source element are mandatory.
 *
 * 	  The relationship-role-source element designates an entity
 * 	  bean by means of an ejb-name element. For bidirectional
 * 	  relationships, both roles of a relationship must declare a
 * 	  relationship-role-source element that specifies a cmr-field
 * 	  in terms of which the relationship is accessed. The lack of
 * 	  a cmr-field element in an ejb-relationship-role specifies
 * 	  that the relationship is unidirectional in navigability and
 * 	  the entity bean that participates in the relationship is
 * 	  "not aware" of the relationship.
 *
 * 	  Example:
 *
 * 	  <ejb-relation>
 * 	      <ejb-relation-name>Product-LineItem</ejb-relation-name>
 * 	      <ejb-relationship-role>
 * 		  <ejb-relationship-role-name>product-has-lineitems
 * 		  </ejb-relationship-role-name>
 * 		  <multiplicity>One</multiplicity>
 * 		  <relationship-role-source>
 * 		  <ejb-name>ProductEJB</ejb-name>
 * 		  </relationship-role-source>
 * 	       </ejb-relationship-role>
 * 	  </ejb-relation>
 *
 *
 *
 *
 * <p>Java class for ejb-relationship-roleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ejb-relationship-roleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-relationship-role-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;element name="multiplicity" type="{http://java.sun.com/xml/ns/javaee}multiplicityType"/>
 *         &lt;element name="cascade-delete" type="{http://java.sun.com/xml/ns/javaee}emptyType" minOccurs="0"/>
 *         &lt;element name="relationship-role-source" type="{http://java.sun.com/xml/ns/javaee}relationship-role-sourceType"/>
 *         &lt;element name="cmr-field" type="{http://java.sun.com/xml/ns/javaee}cmr-fieldType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface EjbRelationshipRoleMetaData extends IdMetaData
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
    * @return Returns the ejb relationship role  name
    *
    */
   String getEjbRelationshipRoleName();

   /**
    * Sets the ejb relationship role name
    *
    * @param roleName
    *
    *
    */
   void setEjbRelationshipRoleName(String roleName);

   /**
    * @return Returns the multiplicity
    *
    */
   MultiplicityType getMultiplicity();

   /**
    * Sets the multiplicity
    *
    * @param multiplicity The multiplicity
    */
   void setMultiplicity(String multiplicity);

   /**
    *
    * @return Returns true if cascade-delete is set. False otherwise
    *
    */
   boolean isCascadeDelete();

   /**
    * Sets the value of the cascadeDelete property.
    *
    * @param cascadeDelete True if cascade-delete has to be set. False otherwise
    *
    */
   void setCascadeDelete(boolean cascadeDelete);

   /**
    * Gets the value of the relationshipRoleSource property.
    *
    * @return
    *     possible object is
    *     {@link RelationshipRoleSourceMetaData }
    *
    */
   RelationshipRoleSourceMetaData getRelationshipRoleSource();

   /**
    * Sets the value of the relationshipRoleSource property.
    *
    * @param value
    *     allowed object is
    *     {@link RelationshipRoleSourceMetaData }
    *
    */
   void setRelationshipRoleSource(RelationshipRoleSourceMetaData value);

   /**
    * Gets the value of the cmrField property.
    *
    * @return
    *     possible object is
    *     {@link CmrFieldMetaData }
    *
    */
   CmrFieldMetaData getCmrField();

   /**
    * Sets the value of the cmrField property.
    *
    * @param value
    *     allowed object is
    *     {@link CmrFieldMetaData }
    *
    */
   void setCmrField(CmrFieldMetaData value);

}
