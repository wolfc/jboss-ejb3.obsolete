//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.06.08 at 07:12:16 PM IST
//

package org.jboss.ejb3.metadata.spi.jboss;

import java.util.List;

import org.jboss.ejb3.metadata.spi.javaee.SessionBeanMetaData;

/**
 *  Represents the JBoss specific metadata for a Session Bean.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="session-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;group ref="{http://www.jboss.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="security-identity" type="{http://www.jboss.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *         &lt;element name="local-binding" type="{http://www.jboss.com/xml/ns/javaee}local-bindingType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="remote-binding" type="{http://www.jboss.com/xml/ns/javaee}remote-bindingType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="business-local" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="business-remote" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
 *         &lt;element name="home-jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
 *         &lt;element name="local-jndi-name" type="{http://www.jboss.com/xml/ns/javaee}local-jndi-nameType" minOccurs="0"/>
 *         &lt;element name="local-home-jndi-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType" minOccurs="0"/>
 *         &lt;element name="jndi-binding-policy" type="{http://www.jboss.com/xml/ns/javaee}jndi-binding-policyType" minOccurs="0"/>
 *         &lt;element name="clustered" type="{http://www.jboss.com/xml/ns/javaee}clusteredType" minOccurs="0"/>
 *         &lt;element name="cluster-config" type="{http://www.jboss.com/xml/ns/javaee}cluster-configType" minOccurs="0"/>
 *         &lt;element name="security-domain" type="{http://www.jboss.com/xml/ns/javaee}security-domainType" minOccurs="0"/>
 *         &lt;element name="method-attributes" type="{http://www.jboss.com/xml/ns/javaee}method-attributesType" minOccurs="0"/>
 *         &lt;element name="depends" type="{http://www.jboss.com/xml/ns/javaee}dependsType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="annotation" type="{http://www.jboss.com/xml/ns/javaee}annotationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ignore-dependency" type="{http://www.jboss.com/xml/ns/javaee}ignore-dependencyType" minOccurs="0"/>
 *         &lt;element name="aop-domain-name" type="{http://www.jboss.com/xml/ns/javaee}aop-domain-nameType" minOccurs="0"/>
 *         &lt;element name="cache-config" type="{http://www.jboss.com/xml/ns/javaee}cache-configType" minOccurs="0"/>
 *         &lt;element name="pool-config" type="{http://www.jboss.com/xml/ns/javaee}pool-configType" minOccurs="0"/>
 *         &lt;element name="concurrent" type="{http://www.jboss.com/xml/ns/javaee}concurrentType" minOccurs="0"/>
 *         &lt;element name="jndi-ref" type="{http://www.jboss.com/xml/ns/javaee}jndi-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="port-component" type="{http://www.jboss.com/xml/ns/javaee}port-componentType" minOccurs="0"/>
 *         &lt;element name="ejb-timeout-identity" type="{http://www.jboss.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface JBossSessionBeanMetaData extends SessionBeanMetaData
{

   //TODO
//   List<JBossMessageDestinationRefMetaData> getMessageDestinationRefs();

   /**
    * @return Returns the local jndi bindings for this bean.
    *
    * Returns an empty list if there are no such bindings.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<LocalBindingMetaData> getLocalBindings();

   /**
    * Sets the local jndi bindings for the bean
    *
    * @param localBindings Local jndi bindings
    */
   void setLocalBindings(List<LocalBindingMetaData> localBindings);

   /**
    * @return Returns the remote jndi bindings of this bean.
    *
    * Returns an empty list if there are no such bindings.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<RemoteBindingMetaData> getRemoteBindings();

   /**
    * Sets the remote jndi bindings of this bean
    *
    * @param remoteBindings Remote jndi bindings
    */
   void setRemoteBindings(List<RemoteBindingMetaData> remoteBindings);

   /**
    * @return Returns the jndi-name for the default remote interface of this bean
    *
    */
   String getJndiName();

   /**
    * Sets the jndi-name for the default remote interface of this bean
    *
    * @param jndiName The jndi-name of the default remote interface
    *
    */
   void setJndiName(String jndiName);

   /**
    * @return Returns the jndi-name for the remote home interface of this bean
    *
    */
   String getHomeJndiName();

   /**
    * Sets the jndi-name for the remote home interface of this bean
    *
    * @param homeJndiName The jndi-name of the remote home interface
    *
    */
   void setHomeJndiName(String homeJndiName);

   /**
    * @return Returns the jndi-name for the default local interface of this bean
    *
    */
   String getLocalJndiName();

   /**
    * Sets the jndi-name for the default local interface of this bean
    *
    * @param localJndiName The jndi-name of the default local interface
    *
    */
   void setLocalJndiName(String localJndiName);

   /**
    * @return Returns the jndi-name for the local home interface of this bean
    *
    */
   String getLocalHomeJndiName();

   /**
    * Sets the jndi-name for the local home interface of this bean
    *
    * @param localHomeJndiName The jndi-name of the local home interface
    *
    */
   void setLocalHomeJndiName(String localHomeJndiName);

   /**
    *
    * @return Returns the jndi binding policy of this bean
    *
    */
   String getJndiBindingPolicy();

   /**
    * Sets the jndi binding policy of this bean
    *
    * @param jndiBindingPolicy The jndi binding policy of the bean
    *
    */
   void setJndiBindingPolicy(String jndiBindingPolicy);

   /**
    *
    * @return Returns true if the bean is clustered. Else returns false.
    *
    */
   boolean isClustered();

   /**
    * If the bean is clustered then this property is set to true. Else set to false
    *
    * @param isClustered True if bean is clustered, false otherwise.
    *
    */
   void setClustered(boolean value);

   /**
    *
    * @return Returns the cluster configuration associated with this bean.
    *
    * @throws IllegalStateException The cluster configuration applies only if the
    * bean is clustered. An {@link IllegalStateException} is thrown if the bean is *not*
    * clustered
    *
    */
   ClusterConfigMetaData getClusterConfig() throws IllegalStateException;

   /**
    * Sets the cluster configuration for this bean.
    *
    * @param clusterConfig The cluster configuration
    *
    * @throws IllegalStateException The cluster configuration applies only if the
    * bean is clustered. An {@link IllegalStateException} is thrown if the bean is *not*
    * clustered
    */
   void setClusterConfig(ClusterConfigMetaData value) throws IllegalStateException;

   /**
    *
    * @return Return the security domain name associated with this bean
    *
    */
   String getSecurityDomain();

   /**
    * Sets the security domain name for this bean.
    *
    * @param securityDomain The security domain name
    *
    */
   void setSecurityDomain(String value);

   /**
    * @return Returns the method attributes associated with this bean
    *
    *
    */
   MethodAttributesMetaData getMethodAttributes();

   /**
    * Sets the method attributes for this bean
    *
    * @param methodAttributes The method attributes for this bean
    *
    */
   void setMethodAttributes(MethodAttributesMetaData methodAttributes);

   /**
    * @return Returns a list of dependencies for this bean. The list contains
    * JMX ObjectName(s) of a service on which the EJB depends.
    *
    * Returns an empty list if there are no such dependencies.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<String> getDepends();

   /**
    * Sets the list of dependencies for this bean
    *
    * @param depends The list of JMX ObjectName(s) of services on which this bean depends
    */
   void setDepends(List<String> depends);

   /**
    * @return Returns a list of annotations that will be added to the bean class,
    * method or field.
    *
    * Returns an empty list if there are no such annotations.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    */
   List<AnnotationMetaData> getAnnotations();

   /**
    * Set the list of annotations to be added to the bean class, method or field
    *
    * @param annotations The list of annotations
    */
   void setAnnotations(List<AnnotationMetaData> annotations);

   /**
    *  @return Returns the ignore dependency metadata associated with this bean
    *
    */
   IgnoreDependencyMetaData getIgnoreDependency();

   /**
    * Sets the ignore dependency metadata for this bean
    *
    * @param ignoreDependency The ignore dependency metadata
    *
    */
   void setIgnoreDependency(IgnoreDependencyMetaData ignoreDependency);

   /**
    *
    * @return Returns the AOP domain name associated with this bean.
    * The aspect domain contains the interceptor stack and bindings
    *
    */
   String getAopDomainName();

   /**
    * Sets the AOP domain name for this bean
    *
    * @param aopDomainName The AOP domain name
    *
    */
   void setAopDomainName(String aopDomainName);

   /**
    *
    * @return Returns the cache configurations of this bean
    *
    *
    */
   CacheConfigMetaData getCacheConfig();

   /**
    * Sets the cache configuration for this bean
    *
    * @param cacheConfig The cache configuration
    *
    */
   void setCacheConfig(CacheConfigMetaData cacheConfig);

   /**
    *
    *
    * @return Returns the pool configuration of this bean
    *
    */
   PoolConfigMetaData getPoolConfig();

   /**
    * Sets the pool configuration of this bean
    *
    * @param poolConfig The pool configuration for this bean
    *
    */
   void setPoolConfig(PoolConfigMetaData value);

   /**
    *
    * @return Returns true if the bean is set to block/serialize access
    * instead of throw an exception on concurrent access to the bean.
    *
    * @throws IllegalStateException The isConcurrent property is applicable
    * only for stateful beans. An {@link IllegalStateException} is thrown
    * if the bean is *not* stateful.
    *
    * @see #isStateful()
    * @see #isStateless()
    *
    */
   boolean isConcurrent() throws IllegalStateException;

   /**
    * Set to true if the access to the bean has to be blocked/serialized, instead of
    * throwing an exception, on concurrent access.
    *
    * @param isConcurrent True if bean access has to be block/serialize instead of
    * throwing an exception on concurrent access
    *
    */
   void setConcurrent(boolean isConcurrent) throws IllegalStateException;

   /**
    * @return Returns a list of jndi references for this bean.
    * Returns an empty list if there are no such references.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list
    *
    *
    */
   List<JndiRefMetaData> getJndiRefs();

   /**
    * Set the jndi references used to inject generic types based on JNDI name,
    * for this bean
    *
    * @param jndiRefs
    */
   void setJndiRefs(List<JndiRefMetaData> jndiRefs);

   /**
    *
    * @return Returns the port component associated with this bean
    *
    */
   PortComponentMetaData getPortComponent();

   /**
    * Sets the port component metadata for this bean
    *
    * @param portComponent
    *
    */
   void setPortComponent(PortComponentMetaData portComponent);

   /**
    *
    * @return
    */
   JBossSecurityIdentityMetaData getEjbTimeoutIdentity();

   /**
    * Sets the value of the ejbTimeoutIdentity property.
    *
    * @param value
    *
    */
   void setEjbTimeoutIdentity(JBossSecurityIdentityMetaData value);

}
