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

/**
 *
 * EnterpriseBeanMetaData
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface EnterpriseBeanMetaData extends IdMetaData
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
    * @throws IllegalArgumentException If <code>name</code> is null
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
    * Returns the fully qualified classname of the bean implementation
    * class.
    *
    */
   String getEjbClass();

   /**
    * Sets the fully qualified classname of the bean implementation class.
    *
    * @param beanClass Fully qualified classname of the bean implementation
    * @throws IllegalArgumentException If <code>beanClass</code> is null
    *
    */
   void setEjbClass(String beanClass);

   /**
    * Returns a list of env-entry metadata of this bean.
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
    * @return Returns the list of persistence context references of this bean
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
