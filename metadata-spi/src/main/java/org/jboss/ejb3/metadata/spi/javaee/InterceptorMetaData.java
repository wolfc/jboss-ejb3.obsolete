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
 *         The interceptorType element declares information about a single
 *         interceptor class.  It consists of :
 *
 *             - An optional description.
 *             - The fully-qualified name of the interceptor class.
 *             - An optional list of around invoke methods declared on the
 *               interceptor class and/or its super-classes.
 *             - An optional list environment dependencies for the interceptor
 *               class and/or its super-classes.
 *             - An optional list of post-activate methods declared on the
 *               interceptor class and/or its super-classes.
 *             - An optional list of pre-passivate methods declared on the
 *               interceptor class and/or its super-classes.
 *
 *
 *
 * <p>Java class for interceptorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="interceptorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="interceptor-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="around-invoke" type="{http://java.sun.com/xml/ns/javaee}around-invokeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="post-activate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="pre-passivate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface InterceptorMetaData extends IdMetaData
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
    * @return Returns the fully qualified classname of the interceptor class
    *
    */
   String getInterceptorClass();

   /**
    * Sets the fully qualified class name of the interceptor
    * 
    * @param Fully qualified classname of the interceptor
    *
    */
   void setInterceptorClass(String interceptorClassName);

   /**
    * @return Returns a list of around invoke methods declared on the
    *          interceptor class and/or its super-classes
    *
    */
   List<AroundInvokeMetaData> getAroundInvokes();
   
   /**
    * Sets the list of around invoke methods declared on the 
    * interceptor class and/or its super-classes
    * 
    * @param aroundInvokes
    */
   void setAroundInvokes(List<AroundInvokeMetaData> aroundInvokes);

   /**
    * Returns a list of env-entry metadata of this interceptor.
    *
    * Returns an empty list if there is no env-entry.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    *
    */
   List<EnvEntryMetaData> getEnvEntries();
   
   /**
    * Sets the list of env-entry metadata of this interceptor
    *
    * @param envEntries The list of env-entry of this interceptor
    */
   void setEnvEntries(List<EnvEntryMetaData> envEntries);

   /**
    * Returns the list of EJB references of this interceptor
    * Returns an empty list if there is no EJB reference for this interceptor.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    *
    */
   List<EjbRefMetaData> getEjbRefs();

   /**
    * Sets the list of EJB references for this interceptor
    *
    * @param ejbRefs The list of EJB references
    */
   void setEjbRefs(List<EjbRefMetaData> ejbRefs);


   /**
    * Returns the list of EJB local references of this interceptor
    * Returns an empty list if there is no EJB local reference for this interceptor.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    */
   List<EjbLocalRefMetaData> getEjbLocalRefs();

   /**
    * Sets the list of EJB local references for this interceptor
    *
    * @param ejbLocalRefs The list of EJB local references
    */
   void setEjbLocalRefs(List<EjbLocalRefMetaData> ejbLocalRefs);

   /**
    * Returns the list of web service reference(s) of this interceptor.
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    */
   List<ServiceRefMetaData> getServiceRefs();

   /**
    * Sets the list of web service references for this interceptor
    *
    * @param serviceRefs The service references
    */
   void setServiceRefs(List<ServiceRefMetaData> serviceRefs);

   /**
    * @return Returns the list of resource references of this interceptor.
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<ResourceRefMetaData> getResourceRefs();

   /**
    * Sets the resource references of this interceptor
    *
    * @param resourceRefs List of resource references of this interceptor
    */
   void setResourceRefs(List<ResourceRefMetaData> resourceRefs);

   /**
    * @return Returns the list of resource environment references of this interceptor.
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<ResourceEnvRefMetaData> getResourceEnvRefs();

   /**
    * Sets the resource env references of this interceptor
    *
    * @param resourceEnvRefs
    */
   void setResourceEnvRefs(List<ResourceEnvRefMetaData> resourceEnvRefs);

   /**
    * Returns the message destination references associated with this interceptor
    *
    *
    */
   List<MessageDestinationRefMetaData> getMessageDestinationRefs();
   
   /**
    * Sets the message destination references associated with this interceptor
    * 
    * @param messageDestinationRefs
    */
   void setMessageDestinationRefs(List<MessageDestinationRefMetaData> messageDestinationRefs);

   /**
    * @return Returns the list of persistence context references of this interceptor
    *
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<PersistenceContextRefMetaData> getPersistenceContextRefs();

   /**
    * Sets the list of persistence context references of this interceptor
    *
    * @param persistenceContextRefs The persistence context references
    */
   void setPeristenceContextRefs(List<PersistenceContextRefMetaData> persistenceContextRefs);



   /**
    * @returns Returns the persistence unit references associated with this interceptor
    *
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<PersistenceUnitRefMetaData> getPersistenceUnitRefs();

   /**
    * Sets the persistence unit references
    *
    * @param persistenceUnitRefs The persistence unit references of this interceptor
    */
   void setPersistenceUnitRefs(List<PersistenceUnitRefMetaData> persistenceUnitRefs);

   /**
    * @return Returns a list of post-construct methods associated with this interceptor
    * Returns an empty list if there are no post-constructs.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<LifecycleCallbackMetaData> getPostConstructs();

   /**
    * Sets the post-constructs associated with this interceptor
    *
    * @param postConstructs The list of post-constructs
    */
   void setPostConstructs(List<LifecycleCallbackMetaData> postConstructs);



   /**
    * @return Returns a list of pre-destroy methods associated with this interceptor
    * Returns an empty list if there are no pre-destroy callbacks for this interceptor.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<LifecycleCallbackMetaData> getPreDestroys();

   /**
    * Sets the list of pre-destroy callbacks associated with this interceptor
    *
    * @param preDestroys The list of pre-destroys for this bean
    */
   void setPreDestroys(List<LifecycleCallbackMetaData> preDestroys);

   /**
    * @return Returns a list of post-activate methods associated with this interceptor
    * Returns an empty list if there are no post-activate callbacks for this interceptor.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<LifecycleCallbackMetaData> getPostActivates();

   /**
    * Sets the list of post-activate callbacks associated with this interceptor
    *
    * @param postActivates The post-activate callbacks associated with this interceptor
    */
   void setPostActivates(List<LifecycleCallbackMetaData> postActivates);

   /**
    * @return Returns a list of pre-passivate methods associated with this interceptor
    * Returns an empty list if there are no pre-passivate callbacks for this interceptor.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<LifecycleCallbackMetaData> getPrePassivates();

   /**
    * Sets the list of pre-passivate callbacks associated with this interceptor
    *
    * @param prePassivates The pre-passivate callbacks for this interceptor
    */
   void setPrePassivates(List<LifecycleCallbackMetaData> prePassivates);



}