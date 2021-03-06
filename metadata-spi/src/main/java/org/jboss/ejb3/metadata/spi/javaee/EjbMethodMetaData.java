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
 * 	  The methodType is used to denote a method of an enterprise
 * 	  bean's business, home, component, and/or web service endpoint
 * 	  interface, or, in the case of a message-driven bean, the
 * 	  bean's message listener method, or a set of such
 * 	  methods. The ejb-name element must be the name of one of the
 * 	  enterprise beans declared in the deployment descriptor; the
 * 	  optional method-intf element allows to distinguish between a
 * 	  method with the same signature that is multiply defined
 * 	  across the business, home, component, and/or web service
 *           endpoint nterfaces; the method-name element specifies the
 *           method name; and the optional method-params elements identify
 *           a single method among multiple methods with an overloaded
 * 	  method name.
 *
 * 	  There are three possible styles of using methodType element
 * 	  within a method element:
 *
 * 	  1.
 * 	  <method>
 * 	      <ejb-name>EJBNAME</ejb-name>
 * 	      <method-name>*</method-name>
 * 	  </method>
 *
 * 	     This style is used to refer to all the methods of the
 * 	     specified enterprise bean's business, home, component,
 *              and/or web service endpoint interfaces.
 *
 * 	  2.
 * 	  <method>
 * 	      <ejb-name>EJBNAME</ejb-name>
 * 	      <method-name>METHOD</method-name>
 * 	  </method>
 *
 * 	     This style is used to refer to the specified method of
 * 	     the specified enterprise bean. If there are multiple
 * 	     methods with the same overloaded name, the element of
 * 	     this style refers to all the methods with the overloaded
 * 	     name.
 *
 * 	  3.
 * 	  <method>
 * 	      <ejb-name>EJBNAME</ejb-name>
 * 	      <method-name>METHOD</method-name>
 * 	      <method-params>
 * 		  <method-param>PARAM-1</method-param>
 * 		  <method-param>PARAM-2</method-param>
 * 		  ...
 * 		  <method-param>PARAM-n</method-param>
 * 	      </method-params>
 * 	  </method>
 *
 * 	     This style is used to refer to a single method within a
 * 	     set of methods with an overloaded name. PARAM-1 through
 * 	     PARAM-n are the fully-qualified Java types of the
 * 	     method's input parameters (if the method has no input
 * 	     arguments, the method-params element contains no
 * 	     method-param elements). Arrays are specified by the
 * 	     array element's type, followed by one or more pair of
 * 	     square brackets (e.g. int[][]). If there are multiple
 * 	     methods with the same overloaded name, this style refers
 * 	     to all of the overloaded methods.
 *
 * 	  Examples:
 *
 * 	  Style 1: The following method element refers to all the
 * 	  methods of the EmployeeService bean's business, home,
 *           component, and/or web service endpoint interfaces:
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>*</method-name>
 * 	  </method>
 *
 * 	  Style 2: The following method element refers to all the
 * 	  create methods of the EmployeeService bean's home
 * 	  interface(s).
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>create</method-name>
 * 	  </method>
 *
 * 	  Style 3: The following method element refers to the
 * 	  create(String firstName, String LastName) method of the
 * 	  EmployeeService bean's home interface(s).
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 *
 * 	  The following example illustrates a Style 3 element with
 * 	  more complex parameter types. The method
 * 	  foobar(char s, int i, int[] iar, mypackage.MyClass mycl,
 * 	  mypackage.MyClass[][] myclaar) would be specified as:
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-name>foobar</method-name>
 * 	      <method-params>
 * 		  <method-param>char</method-param>
 * 		  <method-param>int</method-param>
 * 		  <method-param>int[]</method-param>
 * 		  <method-param>mypackage.MyClass</method-param>
 * 		  <method-param>mypackage.MyClass[][]</method-param>
 * 	      </method-params>
 * 	  </method>
 *
 * 	  The optional method-intf element can be used when it becomes
 * 	  necessary to differentiate between a method that is multiply
 * 	  defined across the enterprise bean's business, home, component,
 *           and/or web service endpoint interfaces with the same name and
 * 	  signature. However, if the same method is a method of both the
 *           local business interface, and the local component interface,
 *           the same attribute applies to the method for both interfaces.
 *           Likewise, if the same method is a method of both the remote
 *           business interface and the remote component interface, the same
 *           attribute applies to the method for both interfaces.
 *
 * 	  For example, the method element
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>Remote</method-intf>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 *
 * 	  can be used to differentiate the create(String, String)
 * 	  method defined in the remote interface from the
 * 	  create(String, String) method defined in the remote home
 * 	  interface, which would be defined as
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>Home</method-intf>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 *
 * 	  and the create method that is defined in the local home
 * 	  interface which would be defined as
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>LocalHome</method-intf>
 * 	      <method-name>create</method-name>
 * 	      <method-params>
 * 		  <method-param>java.lang.String</method-param>
 * 		  <method-param>java.lang.String</method-param>
 * 	      </method-params>
 * 	  </method>
 *
 * 	  The method-intf element can be used with all three Styles
 * 	  of the method element usage. For example, the following
 * 	  method element example could be used to refer to all the
 * 	  methods of the EmployeeService bean's remote home interface
 *           and the remote business interface.
 *
 * 	  <method>
 * 	      <ejb-name>EmployeeService</ejb-name>
 * 	      <method-intf>Home</method-intf>
 * 	      <method-name>*</method-name>
 * 	  </method>
 *
 *
 *
 *
 * <p>Java class for methodType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="methodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="method-intf" type="{http://java.sun.com/xml/ns/javaee}method-intfType" minOccurs="0"/>
 *         &lt;element name="method-name" type="{http://java.sun.com/xml/ns/javaee}method-nameType"/>
 *         &lt;element name="method-params" type="{http://java.sun.com/xml/ns/javaee}method-paramsType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public interface EjbMethodMetaData extends IdMetaData
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
    * Gets the value of the ejbName property.
    *
    * @return Returns the name of the EJB corresponding to this bean
    * method
    */
   String getEjbName();

   /**
    * Sets the name of the EJB corresponding to this bean method
    *
    * @param beanName The name of the EJB
    *
    */
   void setEjbName(String beanName);

   /**
    * @return Returns the fully qualified name of the business interface
    * of the bean, to which this method belongs
    *
    */
   String getMethodIntferace();

   /**
    * @param interfaceName Fully qualified name of the business interface
    * of the bean, to which this method belongs
    *
    */
   void setMethodIntface(String interfaceName);

   /**
    * @return Returns the name of the method
    *
    */
   String getMethodName();

   /**
    * Sets the name of the method
    *
    * @param methodName Name of the method
    *
    */
   void setMethodName(String methodName);

   /**
    * @return Returns the method parameters represented by this method
    */
   MethodParamsMetaData getMethodParams();

   /**
    * Sets the method params of this method
    *
    * @param methodParams The method params of this method
    *
    */
   void setMethodParams(MethodParamsMetaData methodParams);


}
