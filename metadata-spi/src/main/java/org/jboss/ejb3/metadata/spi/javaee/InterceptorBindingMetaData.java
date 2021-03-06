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
 *         The interceptor-bindingType element describes the binding of
 *         interceptor classes to beans within the ejb-jar.
 *         It consists of :
 *
 *           - An optional description.
 *           - The name of an ejb within the ejb-jar or the wildcard value "*",
 *             which is used to define interceptors that are bound to all
 *             beans in the ejb-jar.
 *           - A list of interceptor classes that are bound to the contents of
 *             the ejb-name element or a specification of the total ordering
 *             over the interceptors defined for the given level and above.
 *           - An optional exclude-default-interceptors element.  If set to true,
 *             specifies that default interceptors are not to be applied to
 *             a bean-class and/or business method.
 *           - An optional exclude-class-interceptors element.  If set to true,
 *             specifies that class interceptors are not to be applied to
 *             a business method.
 *           - An optional set of method elements for describing the name/params
 *             of a method-level interceptor.
 *
 *          Interceptors bound to all classes using the wildcard syntax
 *          "*" are default interceptors for the components in the ejb-jar.
 *          In addition, interceptors may be bound at the level of the bean
 *          class (class-level interceptors) or business methods (method-level
 *          interceptors ).
 *
 *          The binding of interceptors to classes is additive.  If interceptors
 *          are bound at the class-level and/or default-level as well as the
 *          method-level, both class-level and/or default-level as well as
 *          method-level will apply.
 *
 *          There are four possible styles of the interceptor element syntax :
 *
 *          1.
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;interceptor-binding xmlns="http://www.w3.org/2001/XMLSchema" xmlns:javaee="http://java.sun.com/xml/ns/javaee" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;ejb-name&gt;*&lt;/ejb-name&gt;&lt;interceptor-class&gt;INTERCEPTOR&lt;/interceptor-class&gt;&lt;/interceptor-binding&gt;
 * </pre>
 *
 *
 *          Specifying the ejb-name as the wildcard value "*" designates
 *          default interceptors (interceptors that apply to all session and
 *          message-driven beans contained in the ejb-jar).
 *
 *          2.
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;interceptor-binding xmlns="http://www.w3.org/2001/XMLSchema" xmlns:javaee="http://java.sun.com/xml/ns/javaee" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;ejb-name&gt;EJBNAME&lt;/ejb-name&gt;&lt;interceptor-class&gt;INTERCEPTOR&lt;/interceptor-class&gt;&lt;/interceptor-binding&gt;
 * </pre>
 *
 *
 *          This style is used to refer to interceptors associated with the
 *          specified enterprise bean(class-level interceptors).
 *
 *          3.
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;interceptor-binding xmlns="http://www.w3.org/2001/XMLSchema" xmlns:javaee="http://java.sun.com/xml/ns/javaee" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;ejb-name&gt;EJBNAME&lt;/ejb-name&gt;&lt;interceptor-class&gt;INTERCEPTOR&lt;/interceptor-class&gt;&lt;method&gt;&lt;method-name&gt;METHOD&lt;/method-name&gt;&lt;/method&gt;&lt;/interceptor-binding&gt;
 * </pre>
 *
 *
 *          This style is used to associate a method-level interceptor with
 *          the specified enterprise bean.  If there are multiple methods
 *          with the same overloaded name, the element of this style refers
 *          to all the methods with the overloaded name.  Method-level
 *          interceptors can only be associated with business methods of the
 *          bean class.   Note that the wildcard value "*" cannot be used
 *          to specify method-level interceptors.
 *
 *          4.
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;interceptor-binding xmlns="http://www.w3.org/2001/XMLSchema" xmlns:javaee="http://java.sun.com/xml/ns/javaee" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;ejb-name&gt;EJBNAME&lt;/ejb-name&gt;&lt;interceptor-class&gt;INTERCEPTOR&lt;/interceptor-class&gt;&lt;method&gt;&lt;method-name&gt;METHOD&lt;/method-name&gt;&lt;method-params&gt;&lt;method-param&gt;PARAM-1&lt;/method-param&gt;&lt;method-param&gt;PARAM-2&lt;/method-param&gt;
 *                ...
 *                &lt;method-param&gt;PARAM-N&lt;/method-param&gt;&lt;/method-params&gt;&lt;/method&gt;&lt;/interceptor-binding&gt;
 * </pre>
 *
 *
 *          This style is used to associate a method-level interceptor with
 *          the specified method of the specified enterprise bean.  This
 *          style is used to refer to a single method within a set of methods
 *          with an overloaded name.  The values PARAM-1 through PARAM-N
 *          are the fully-qualified Java types of the method's input parameters
 *          (if the method has no input arguments, the method-params element
 *          contains no method-param elements). Arrays are specified by the
 *          array element's type, followed by one or more pair of square
 *          brackets (e.g. int[][]).
 *
 *
 *
 * <p>Java class for interceptor-bindingType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="interceptor-bindingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;choice>
 *           &lt;element name="interceptor-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="interceptor-order" type="{http://java.sun.com/xml/ns/javaee}interceptor-orderType"/>
 *         &lt;/choice>
 *         &lt;element name="exclude-default-interceptors" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="exclude-class-interceptors" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface InterceptorBindingMetaData extends IdMetaData
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
    * @return Returns the name of the EJB
    *
    */
   String getEjbName();

   /**
    * Sets the name of the EJB
    *
    * @param ejbName Name of the EJB
    *
    */
   void setEjbName(String ejbName);

   /**
    * @return Returns a list of fully qualified names of the interceptor classes
    *
    *
    */
   List<String> getInterceptorClasses();

   /**
    * Sets the list of fully qualified names of the interceptor classes
    *
    * @param interceptorClasses List of fully qualified names of the interceptor classes
    */
   void setInterceptorClasses(List<String> interceptorClasses);

   /**
    * Gets the value of the interceptorOrder property.
    *
    * @return Returns a list of fully qualified names of the intereceptor classes
    * ordered through the interceptor-order element. If there is no interceptor-order
    * configured, then this returns the same the list as {@link #getInterceptorClasses()}
    */
   List<String> getOrderedInterceptorClasses();

   /**
    *
    * @param orderedInterceptors List of ordered interceptor classes
    */
   void setOrderedInterceptorClasses(List<String> orderedInterceptors);

   /**
    *
    * @return Returns true if the default interceptors are not to be applied to
    * the bean-class and/or business method. Else returns false.
    *
    */
   boolean isExcludeDefaultInterceptors();

   /**
    *
    * @param excludeDefaultInterceptors Set to true if the default interceptors
    * are not to be applied to the bean-class and/or business method.
    *
    */
   void setExcludeDefaultInterceptors(boolean value);

   /**
    *
    * @return Returns true if the class interceptors are not to be applied to
    * the business method. Else returns false.
    */
   boolean isExcludeClassInterceptors();

   /**
    *
    *
    * @param excludeClassInterceptors Set to true if the class interceptors are not to be applied to
    * the business method.
    */
   void setExcludeClassInterceptors(boolean excludeClassInterceptors);

   /**
    *
    * @return Returns the methods associated with this interceptor binding
    *
    */
   NamedMethodMetaData getMethod();

   /**
    * Sets the methods associated with this interceptor binding
    *
    */
   void setMethod(NamedMethodMetaData value);

}
