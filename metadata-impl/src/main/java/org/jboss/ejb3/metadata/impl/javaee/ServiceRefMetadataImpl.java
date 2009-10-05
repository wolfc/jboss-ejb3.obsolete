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
package org.jboss.ejb3.metadata.impl.javaee;

import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.ejb3.metadata.spi.javaee.DescriptionMetaData;
import org.jboss.ejb3.metadata.spi.javaee.DisplayNameMetaData;
import org.jboss.ejb3.metadata.spi.javaee.IconType;
import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.ejb3.metadata.spi.javaee.PortComponentRefMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ServiceRefHandlerChainsMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ServiceRefHandlerMetaData;
import org.jboss.ejb3.metadata.spi.javaee.ServiceRefMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;

/**
 * ServiceRefMetadataImpl
 *
 * Represents the metadata for a service-ref
 * 
 * TODO Needs to be implemented
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ServiceRefMetadataImpl extends IdMetadataImpl implements ServiceRefMetaData
{

   /**
    * The {@link ServiceReferenceMetaData} from which this {@link ServiceRefMetadataImpl} was
    * constructed
    */
   private ServiceReferenceMetaData delegate;

   /**
    * Constructs a {@link ServiceRefMetadataImpl} from a {@link ServiceReferenceMetaData}
    * 
    * @param serviceRef
    * @throws NullPointerException If the passed <code>serviceRef</code> is null
    */
   public ServiceRefMetadataImpl(ServiceReferenceMetaData serviceRef)
   {
      super(serviceRef.getId());
      this.initialize(serviceRef);
   }

   /**
    * Initializes this {@link ServiceRefMetadataImpl} from the state in <code>serviceRef</code>
    * 
    * @param serviceRef
    * @throws NullPointerException If the passed <code>serviceRef</code> is null
    */
   private void initialize(ServiceReferenceMetaData serviceRef)
   {
      // set the delegate
      this.delegate = serviceRef;

   }

   public List<DescriptionMetaData> getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<DisplayNameMetaData> getDisplayName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ServiceRefHandlerChainsMetaData getHandlerChains()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<ServiceRefHandlerMetaData> getHandlers()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<IconType> getIcon()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<InjectionTargetMetaData> getInjectionTargets()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getJaxrpcMappingFile()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getMappedName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<PortComponentRefMetaData> getPortComponentRefs()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getServiceInterface()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public QName getServiceQname()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getServiceRefName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getServiceRefType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getWsdlFile()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setHandlerChains(ServiceRefHandlerChainsMetaData serviceRefHandlerChains)
   {
      // TODO Auto-generated method stub

   }

   public void setHandlers(List<ServiceRefHandlerMetaData> serviceRefHandlers)
   {
      // TODO Auto-generated method stub

   }

   public void setInjectionTargets(List<InjectionTargetMetaData> injectionTargets)
   {
      // TODO Auto-generated method stub

   }

   public void setJaxrpcMappingFile(String jaxRpcMappingFile)
   {
      // TODO Auto-generated method stub

   }

   public void setMappedName(String mappedName)
   {
      // TODO Auto-generated method stub

   }

   public void setPortComponentRefs(List<PortComponentRefMetaData> portComponentRefs)
   {
      // TODO Auto-generated method stub

   }

   public void setServiceInterface(String serviceInterface)
   {
      // TODO Auto-generated method stub

   }

   public void setServiceQname(QName serviceQName)
   {
      // TODO Auto-generated method stub

   }

   public void setServiceRefName(String serviceRefName)
   {
      // TODO Auto-generated method stub

   }

   public void setServiceRefType(String serviceRefType)
   {
      // TODO Auto-generated method stub

   }

   public void setWsdlFile(String wsdlFileURILocation)
   {
      // TODO Auto-generated method stub

   }
}
