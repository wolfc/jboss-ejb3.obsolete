/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.ejb3.metadata.spi.javaee;

import java.util.List;
import java.util.Set;

/**
 *
 * Represents metadata for a session bean
 *
 *
 * 	The session-beanType declares an session bean. The
 * 	declaration consists of:
 *
 * 	    - an optional description
 * 	    - an optional display name
 * 	    - an optional icon element that contains a small and a large
 * 	      icon file name
 * 	    - a name assigned to the enterprise bean
 * 	      in the deployment description
 *             - an optional mapped-name element that can be used to provide
 *               vendor-specific deployment information such as the physical
 *               jndi-name of the session bean's remote home/business interface.
 *               This element is not required to be supported by all
 *               implementations. Any use of this element is non-portable.
 *             - the names of all the remote or local business interfaces,
 *               if any
 * 	    - the names of the session bean's remote home and
 * 	      remote interfaces, if any
 * 	    - the names of the session bean's local home and
 * 	      local interfaces, if any
 * 	    - the name of the session bean's web service endpoint
 * 	      interface, if any
 * 	    - the session bean's implementation class
 * 	    - the session bean's state management type
 *             - an optional declaration of the session bean's timeout method.
 * 	    - the optional session bean's transaction management type.
 *               If it is not present, it is defaulted to Container.
 *             - an optional list of the session bean class and/or
 *               superclass around-invoke methods.
 * 	    - an optional declaration of the bean's
 * 	      environment entries
 * 	    - an optional declaration of the bean's EJB references
 * 	    - an optional declaration of the bean's local
 * 	      EJB references
 * 	    - an optional declaration of the bean's web
 * 	      service references
 * 	    - an optional declaration of the security role
 * 	      references
 * 	    - an optional declaration of the security identity
 * 	      to be used for the execution of the bean's methods
 * 	    - an optional declaration of the bean's resource
 * 	      manager connection factory references
 * 	    - an optional declaration of the bean's resource
 * 	      environment references.
 * 	    - an optional declaration of the bean's message
 * 	      destination references
 *
 * 	The elements that are optional are "optional" in the sense
 * 	that they are omitted when if lists represented by them are
 * 	empty.
 *
 * 	Either both the local-home and the local elements or both
 * 	the home and the remote elements must be specified for the
 * 	session bean.
 *
 * 	The service-endpoint element may only be specified if the
 * 	bean is a stateless session bean.
 *
 *
 *
 * <p>Java class for session-beanType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="session-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="home" type="{http://java.sun.com/xml/ns/javaee}homeType" minOccurs="0"/>
 *         &lt;element name="remote" type="{http://java.sun.com/xml/ns/javaee}remoteType" minOccurs="0"/>
 *         &lt;element name="local-home" type="{http://java.sun.com/xml/ns/javaee}local-homeType" minOccurs="0"/>
 *         &lt;element name="local" type="{http://java.sun.com/xml/ns/javaee}localType" minOccurs="0"/>
 *         &lt;element name="business-local" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="business-remote" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="service-endpoint" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType" minOccurs="0"/>
 *         &lt;element name="session-type" type="{http://java.sun.com/xml/ns/javaee}session-typeType" minOccurs="0"/>
 *         &lt;element name="timeout-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/>
 *         &lt;element name="init-method" type="{http://java.sun.com/xml/ns/javaee}init-methodType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="remove-method" type="{http://java.sun.com/xml/ns/javaee}remove-methodType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="transaction-type" type="{http://java.sun.com/xml/ns/javaee}transaction-typeType" minOccurs="0"/>
 *         &lt;element name="around-invoke" type="{http://java.sun.com/xml/ns/javaee}around-invokeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="post-activate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="pre-passivate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-role-ref" type="{http://java.sun.com/xml/ns/javaee}security-role-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-identity" type="{http://java.sun.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface SessionBeanMetaData extends IdMetaData
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
    * Returns the ejb name
    *
    */
   String getEjbName();

   /**
    * Sets the ejb name
    *
    * @param name EJB name
    */
   void setEjbName(String name);

   /**
    * Returns the mapped-name of the bean.
    * Returns null if there is no mapped-name for
    * this bean
    *
    */
   String getMappedName();

   /**
    * Sets the mapped-name of the bean
    * @param mappedName The mapped-name of the bean
    */
   void setMappedName(String mappedName);

   /**
    * Returns the classname of the home interface corresponding to this
    * bean. Returns null if there is no home interface
    *
    */
   String getHome();

   /**
    * Sets the classname of the home interface for this bean
    *
    * @param homeInterface The home interface of the bean
    *
    */
   void setHome(String homeInterface);

   /**
    * Returns the fully qualified classname of the remote interface corresponding to this
    * bean. Returns null if there is no remote interface.
    *
    * Note that this is *not* the same as {@link #getBusinessRemotes()}. This method
    * returns the EJB2.x remote view interface name
    *
    */
   String getRemote();

   /**
    * Sets the EJB2.x remote interface for this bean.
    *
    * @param remoteInterface The EJB2.x remote interface for the bean
    *
    */
   void setRemote(String remoteInterface);

   /**
    * Returns the fully qualified classname of the local home interface corresponding to this
    * bean. Returns null if there is no local home interface
    *
    */
   String getLocalHome();

   /**
    * Sets the local home for this bean
    *
    * @param localHome The local home interface of this bean
    *
    */
   void setLocalHome(String localHome);

   /**
    * Returns the fully qualified classname of the local interface corresponding to this
    * bean. Returns null if there is no local interface.
    *
    * Note that this is *not* the same as {@link #getBusinessLocals()}. This method
    * returns the EJB2.x local view interface name
    *
    */
   String getLocal();

   /**
    * Sets the EJB2.x local interface for this bean.
    *
    * @param The EJB2.x local interface
    *
    */
   void setLocal(String local);

   /**
    * Returns the fully qualified classnames of EJB3.x business locals of this bean.
    * Returns an empty {@link Set} if there are no business locals.
    *
    * Note that its upto the implementations to either return a modifiable
    * {@link Set} or an umodifiable one.
    *
    *
    */
   Set<String> getBusinessLocals();

   /**
    * Sets the business locals of this bean
    *
    * @param businessLocals The business locals
    *
    */
   void setBusinessLocals(Set<String> businessLocals);

   /**
    * Returns the EJB3.x business remotes of this bean.
    * Returns an empty {@link Set} if there are no business remotes.
    *
    * Note that its upto the implementations to either return a modifiable
    * {@link Set} or an umodifiable one.
    *
    *
    */
   Set<String> getBusinessRemotes();

   /**
    * Sets the business remotes for this bean
    *
    * @param businessRemotes The business remotes
    */
   void setBusinessRemotes(Set<String> businessRemotes);

   /**
    * Returns the fully qualified class name of the service endpoint
    * of the bean.
    *
    * @throws IllegalStateException Service endpoints are valid only for
    * Stateless Session beans. An {@link IllegalStateException} is thrown
    * by this method if the bean is not stateless.
    *
    * @see #isStateless()
    * @see #isStateful()
    *
    */
   String getServiceEndpoint() throws IllegalStateException;

   /**
    * Sets the fully qualified class name of the service endpoint
    * of this bean.
    *
    * @param serviceEndpoint Fully qualified class name of the service endpoint
    *
    * @throws IllegalStateException Service endpoints are valid only for
    * Stateless Session beans. An {@link IllegalStateException} is thrown
    * by this method if the bean is not stateless.
    *
    * @see #isStateless()
    * @see #isStateful()
    */
   void setServiceEndpoint(String value) throws IllegalStateException;

   /**
    * Returns true if the bean is stateless. Returns false otherwise
    * @return
    */
   boolean isStateless();

   /**
    * Returns true if the bean is stateful. Returns false otherwise
    *
    * @return
    */
   boolean isStateful();

   /**
    * Returns the fully qualified classname of the bean implementation
    * class.
    *
    */
   String getEjbClass();

   /**
    * Sets the fully qualified classname of the bean implementation class.
    *
    * @param beanClass Fully qualified classname of the bean implementation
    *
    */
   void setEjbClass(String beanClass);

   /**
    * TODO: Revisit this - do we need this
    */
   String getSessionType();

   /**
    * TODO: Revisit this - do we need this
    *
    */
   void setSessionType(String value);

   /**
    * Returns the timeout method of this bean.
    *
    * Returns null if there is no such method
    *
    */
   NamedMethodMetaData getTimeoutMethod();

   /**
    * Sets the timeout-method of this bean
    *
    * @param timeoutMethod The timeout method metadata
    */
   void setTimeoutMethod(NamedMethodMetaData timeoutMethod);

   /**
    * Returns the init-methods metadata of this bean. Returns
    * an empty list if there are none.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    * @throws IllegalStateException The init-methods are applicable
    * only for stateful session beans. An {@link IllegalStateException} is thrown
    * if the bean is not stateful
    *
    * @see #isStateful()
    * @see #isStateless()
    *
    */
   List<InitMethodMetaData> getInitMethods() throws IllegalStateException;

   /**
    * Sets the init-method metadata of this bean
    *
    * @param initMethods
    *
    * @throws IllegalStateException The init-methods are applicable
    * only for stateful session beans. An {@link IllegalStateException} is thrown
    * if the bean is not stateful
    *
    * @see #isStateful()
    * @see #isStateless()
    */
   void setInitMethods(List<InitMethodMetaData> initMethods) throws IllegalStateException;

   /**
    * Returns the remove-methods metadata of this bean. Returns
    * an empty list if there are none.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    * @throws IllegalStateException The remove-methods are applicable
    * only for stateful session beans. An {@link IllegalStateException} is thrown
    * if the bean is not stateful
    *
    * @see #isStateful()
    * @see #isStateless()
    *
    *
    */
   List<RemoveMethodMetaData> getRemoveMethods() throws IllegalStateException;

   /**
    * Sets the remove-method metadata of this bean
    *
    * @param removeMethods
    *
    * @throws IllegalStateException The remove-methods are applicable
    * only for stateful session beans. An {@link IllegalStateException} is thrown
    * if the bean is not stateful
    *
    * @see #isStateful()
    * @see #isStateless()
    */
   void setRemoveMethods(List<RemoveMethodMetaData> removeMethods) throws IllegalStateException;

   /**
    * Returns the transaction type of this bean
    *
    */
   TransactionType getTransactionType();

   /**
    * Sets the transaction type of this bean
    *
    * @param transactionType The transaction type of this bean
    *
    */
   void setTransactionType(TransactionType transactionType);

   /**
    * Returns a list of around-invoke metadata of this bean.
    * Returns an empty list if there is no around-invoke for this bean
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    */
   List<AroundInvokeMetaData> getAroundInvokes();

   /**
    * Sets the list of around-invoke metadata of the bean.
    *
    * @param aroundInvokes
    */
   void setAroundInvokes(List<AroundInvokeMetaData> aroundInvokes);

   /**
    * Returns a list of env-entry metadata of this bean.
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
    * Sets the list of env-entry metadata of this bean
    *
    * @param envEntries The list of env-entry of this bean
    */
   void setEnvEntries(List<EnvEntryMetaData> envEntries);

   /**
    * Returns the list of EJB references of this bean
    * Returns an empty list if there is no EJB reference for this bean.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    *
    */
   List<EjbRefMetaData> getEjbRefs();

   /**
    * Sets the list of EJB references for this bean
    *
    * @param ejbRefs The list of EJB references
    */
   void setEjbRefs(List<EjbRefMetaData> ejbRefs);

   /**
    * Returns the list of EJB local references of this bean
    * Returns an empty list if there is no EJB local reference for this bean.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    */
   List<EjbLocalRefMetaData> getEjbLocalRefs();

   /**
    * Sets the list of EJB local references for this bean
    *
    * @param ejbLocalRefs The list of EJB local references
    */
   void setEjbLocalRefs(List<EjbLocalRefMetaData> ejbLocalRefs);

   /**
    * Returns the list of web service reference(s) of this bean.
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    *
    */
   List<ServiceRefMetaData> getServiceRefs();

   /**
    * Sets the list of web service references for this bean
    *
    * @param serviceRefs The service references
    */
   void setServiceRefs(List<ServiceRefMetaData> serviceRefs);

   /**
    * @return Returns the list of resource references of this bean.
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<ResourceRefMetaData> getResourceRefs();

   /**
    * Sets the resource references of this bean
    *
    * @param resourceRefs List of resource references of this bean
    */
   void setResourceRefs(List<ResourceRefMetaData> resourceRefs);

   /**
    * @return Returns the list of resource environment references of this bean.
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<ResourceEnvRefMetaData> getResourceEnvRefs();

   /**
    * Sets the resource env references of this bean
    *
    * @param resourceEnvRefs
    */
   void setResourceEnvRefs(List<ResourceEnvRefMetaData> resourceEnvRefs);

   /**
    * Gets the value of the messageDestinationRef property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the messageDestinationRef property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getMessageDestinationRef().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link MessageDestinationRefType }
    *
    *
    */
   // TODO: Revisit, we need this
   //List<MessageDestinationRefType> getMessageDestinationRef();
   /**
    * @return Returns the list of persistence context references of this bean
    *
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<PersistenceContextRefMetaData> getPersistenceContextRefs();

   /**
    * Sets the list of persistence context references of this bean
    *
    * @param persistenceContextRefs The persistence context references
    */
   void setPeristenceContextRefs(List<PersistenceContextRefMetaData> persistenceContextRefs);

   /**
    * @returns Returns the persistence unit references associated with this bean
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
    * @param persistenceUnitRefs The persistence unit references of this bean
    */
   void setPersistenceUnitRefs(List<PersistenceUnitRefMetaData> persistenceUnitRefs);

   /**
    * @return Returns a list of post-construct methods associated with this bean
    * Returns an empty list if there are no post-constructs.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<LifecycleCallbackMetaData> getPostConstructs();

   /**
    * Sets the post-constructs associated with this bean
    *
    * @param postConstructs The list of post-constructs
    */
   void setPostConstructs(List<LifecycleCallbackMetaData> postConstructs);

   /**
    * @return Returns a list of pre-destroy methods associated with this bean
    * Returns an empty list if there are no pre-destroy callbacks for this bean.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<LifecycleCallbackMetaData> getPreDestroys();

   /**
    * Sets the list of pre-destroy callbacks associated with this bean
    *
    * @param preDestroys The list of pre-destroys for this bean
    */
   void setPreDestroys(List<LifecycleCallbackMetaData> preDestroys);

   /**
    * @return Returns a list of post-activate methods associated with this bean
    * Returns an empty list if there are no post-activate callbacks for this bean.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<LifecycleCallbackMetaData> getPostActivates();

   /**
    * Sets the list of post-activate callbacks associated with this bean
    *
    * @param postActivates The post-activate callbacks associated with this bean
    */
   void setPostActivates(List<LifecycleCallbackMetaData> postActivates);

   /**
    * @return Returns a list of pre-passivate methods associated with this bean
    * Returns an empty list if there are no pre-passivate callbacks for this bean.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<LifecycleCallbackMetaData> getPrePassivates();

   /**
    * Sets the list of pre-passivate callbacks associated with this bean
    *
    * @param prePassivates The pre-passivate callbacks for this bean
    */
   void setPrePassivates(List<LifecycleCallbackMetaData> prePassivates);

   /**
    * @return Returns a list of security roles references associated with this bean
    * Returns an empty list if there are no such references
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<SecurityRoleRefMetaData> getSecurityRoleRefs();

   /**
    * Sets the security role references for this bean
    *
    * @param securityRoleRefs The security role references for this bean
    */
   void setSecurityRoleRefs(List<SecurityRoleRefMetaData> securityRoleRefs);

   /**
    * @return Returns the security identity associated with this bean
    *
    */
   SecurityIdentityMetaData getSecurityIdentity();

   /**
    * Sets the security identity associated with this bean
    *
    * @param securityIdentity The security identity associated with this bean
    *
    */
   void setSecurityIdentity(SecurityIdentityMetaData securityIdentity);

}
