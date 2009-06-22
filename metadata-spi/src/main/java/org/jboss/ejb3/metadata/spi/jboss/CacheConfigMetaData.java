//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.08 at 07:12:16 PM IST 
//

package org.jboss.ejb3.metadata.spi.jboss;

/**
 * Element for specifying the class used to provide the caching mechanism for a bean,
 *             and the cache parameters
 *          
 * 
 * <p>Java class for cache-configType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cache-configType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cache-value" type="{http://www.jboss.com/xml/ns/javaee}cache-valueType" minOccurs="0"/>
 *         &lt;element name="cache-max-size" type="{http://www.jboss.com/xml/ns/javaee}cache-max-sizeType" minOccurs="0"/>
 *         &lt;element name="idle-timeout-seconds" type="{http://www.jboss.com/xml/ns/javaee}idle-timeout-secondsType" minOccurs="0"/>
 *         &lt;element name="remove-timeout-seconds" type="{http://www.jboss.com/xml/ns/javaee}remove-timeout-secondsType" minOccurs="0"/>
 *         &lt;element name="cache-name" type="{http://www.jboss.com/xml/ns/javaee}cache-nameType" minOccurs="0"/>
 *         &lt;element name="persistence-manager" type="{http://www.jboss.com/xml/ns/javaee}persistence-managerType" minOccurs="0"/>
 *         &lt;element name="replication-is-passivation" type="{http://www.jboss.com/xml/ns/javaee}replication-is-passivationType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public interface CacheConfigMetaData
{

   /**
    * Gets the value of the cacheValue property.
    * 
    * @return
    *     possible object is
    *     {@link CacheValueType }
    *     
    */
   String getCacheValue();

   /**
    * Sets the value of the cacheValue property.
    * 
    * @param value
    *     allowed object is
    *     {@link CacheValueType }
    *     
    */
   void setCacheValue(String value);

   /**
    * Gets the value of the cacheMaxSize property.
    * 
    * @return
    *     possible object is
    *     {@link CacheMaxSizeType }
    *     
    */
   int getCacheMaxSize();

   /**
    * Sets the value of the cacheMaxSize property.
    * 
    * @param value
    *     allowed object is
    *     {@link CacheMaxSizeType }
    *     
    */
   void setCacheMaxSize(int value);

   /**
    * Gets the value of the idleTimeoutSeconds property.
    * 
    * @return
    *     possible object is
    *     {@link IdleTimeoutSecondsType }
    *     
    */
   long getIdleTimeoutSeconds();

   /**
    * Sets the value of the idleTimeoutSeconds property.
    * 
    * @param value
    *     allowed object is
    *     {@link IdleTimeoutSecondsType }
    *     
    */
   void setIdleTimeoutSeconds(long value);

   /**
    * Gets the value of the removeTimeoutSeconds property.
    * 
    * @return
    *     possible object is
    *     {@link RemoveTimeoutSecondsType }
    *     
    */
   long getRemoveTimeoutSeconds();

   /**
    * Sets the value of the removeTimeoutSeconds property.
    * 
    * @param value
    *     allowed object is
    *     {@link RemoveTimeoutSecondsType }
    *     
    */
   void setRemoveTimeoutSeconds(long value);

   /**
    * Gets the value of the cacheName property.
    * 
    * @return
    *     possible object is
    *     {@link CacheNameType }
    *     
    */
   String getCacheName();

   /**
    * Sets the value of the cacheName property.
    * 
    * @param value
    *     allowed object is
    *     {@link CacheNameType }
    *     
    */
   void setCacheName(String value);

   /**
    * Gets the value of the persistenceManager property.
    * 
    * @return
    *     possible object is
    *     {@link PersistenceManagerType }
    *     
    */
   String getPersistenceManager();

   /**
    * Sets the value of the persistenceManager property.
    * 
    * @param value
    *     allowed object is
    *     {@link PersistenceManagerType }
    *     
    */
   void setPersistenceManager(String value);

   /**
    * Gets the value of the replicationIsPassivation property.
    * 
    * @return
    *     possible object is
    *     {@link ReplicationIsPassivationType }
    *     
    */
   String getReplicationIsPassivation();

   /**
    * Sets the value of the replicationIsPassivation property.
    * 
    * @param value
    *     allowed object is
    *     {@link ReplicationIsPassivationType }
    *     
    */
   void setReplicationIsPassivation(String value);

}