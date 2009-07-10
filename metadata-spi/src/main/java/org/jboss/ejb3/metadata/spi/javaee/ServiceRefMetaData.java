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
 * Represents the metadata for the webservice reference.
 *
 * 	The service-ref element declares a reference to a Web
 * 	service. It contains optional description, display name and
 * 	icons, a declaration of the required Service interface,
 * 	an optional WSDL document location, an optional set
 * 	of JAX-RPC mappings, an optional QName for the service element,
 * 	an optional set of Service Endpoint Interfaces to be resolved
 * 	by the container to a WSDL port, and an optional set of handlers.
 *
 *
 *
 * <p>Java class for service-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="service-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="service-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="service-interface" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="service-ref-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="wsdl-file" type="{http://java.sun.com/xml/ns/javaee}xsdAnyURIType" minOccurs="0"/>
 *         &lt;element name="jaxrpc-mapping-file" type="{http://java.sun.com/xml/ns/javaee}pathType" minOccurs="0"/>
 *         &lt;element name="service-qname" type="{http://java.sun.com/xml/ns/javaee}xsdQNameType" minOccurs="0"/>
 *         &lt;element name="port-component-ref" type="{http://java.sun.com/xml/ns/javaee}port-component-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="handler" type="{http://java.sun.com/xml/ns/javaee}service-ref_handlerType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="handler-chains" type="{http://java.sun.com/xml/ns/javaee}service-ref_handler-chainsType" minOccurs="0"/>
 *         &lt;/choice>
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
public interface ServiceRefMetaData extends IdMetaData
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
    * Returns the service reference name
    * The service-ref-name element declares logical name that the
    *    components in the module use to look up the Web service.
    *
    */
   String getServiceRefName();

   /**
    * Sets the service reference name
    *
    * @param serviceRefName The service reference name
    */
   void setServiceRefName(String serviceRefName);

   /**
    * Returns the fully qualified classname of the JAX-RPC Service interface.
    *
    */
   String getServiceInterface();

   /**
    * Sets the fully qualified classname of the JAX-RPC Service interface.
    *
    * @param serviceInterface Fully qualified classname of the service interface
    *
    */
   void setServiceInterface(String serviceInterface);

   /**
    * Returns the fully qualified class name of the service class or the
    * service endpoint interface class
    * @return
    */
   String getServiceRefType();

   /**
    * Sets the fully qualified class name of the service class or the service
    * endpoint interface class
    *
    * @param serviceRefType Fully qualified classname of the service class or the
    * service endpoint interface.
    */
   void setServiceRefType(String serviceRefType);

   /**
    * Returns the URI location of a WSDL file.
    * The location is relative to the root of the module
    *
    */
   String getWsdlFile();

   /**
    * Sets the URI location of the WSDL file
    *
    * @param wsdlFile The URI location of the WSDL file
    */
   void setWsdlFile(String wsdlFileURILocation);

   /**
    * @return Returns the name of a file that
    *    describes the JAX-RPC mapping between the Java interaces used by
    *    the application and the WSDL description in the wsdl-file.  The
    *    file name is a relative path within the module file.
    */
   String getJaxrpcMappingFile();

   /**
    * Sets the file name of the JAX-RPC mapping.
    *
    * @param jaxRpcMappingFile THe file name
    */
   void setJaxrpcMappingFile(String jaxRpcMappingFile);

   /**
    *
    * @return Returns the specific WSDL service
    *    element that is being refered to
    */
   QName getServiceQname();

   /**
    * Sets the specific WSDL service element being refered by this
    * service reference.
    *
    * @param serviceQName
    *
    */
   void setServiceQname(QName serviceQName);

   /**
    * Returns the list of port component reference metadata
    * of this service reference.
    *
    */
   List<PortComponentRefMetaData> getPortComponentRefs();

   /**
    * Sets the port component references
    *
    * @param portComponentRefs
    */
   void setPortComponentRefs(List<PortComponentRefMetaData> portComponentRefs);

   /**
    * Returns the list of service reference handler metadata
    *
    */
   List<ServiceRef_HandlerMetaData> getHandlers();

   /**
    * Sets the list of service reference handlers
    *
    * @param serviceRefHandlers
    */
   void setHandlers(List<ServiceRef_HandlerMetaData> serviceRefHandlers);

   /**
    * Returns the service reference handler chains
    *
    */
   ServiceRef_HandlerChainsMetaData getHandlerChains();

   /**
    * Sets the service reference handler chains
    *
    * @param serviceRefHandlerChains
    *
    */
   void setHandlerChains(ServiceRef_HandlerChainsMetaData serviceRefHandlerChains);

   /**
    * Returns the mapped-name of the service reference.
    * Returns null if the mapped-name is not set
    *
    */
   String getMappedName();

   /**
    * Sets the mapped-name of the service reference
    *
    * @param mappedName mapped-name of the service reference
    */
   void setMappedName(String mappedName);

   /**
    * Returns a list of injection target(s) metadata for
    * this service reference
    * Returns an empty list if there is no injection-target.
    *
    * It's upto the implementation to return either a modifiable
    * or an unmodifiable list.
    */
   List<InjectionTargetMetaData> getInjectionTargets();

   /**
    * Sets the list of injection targets for this service reference
    *
    * @param injectionTargets List of injection targets metadata for this service reference
    */
   void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets);

}