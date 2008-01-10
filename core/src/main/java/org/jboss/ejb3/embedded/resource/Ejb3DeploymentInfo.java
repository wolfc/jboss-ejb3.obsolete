/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ejb3.embedded.resource;

import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.jboss.deployment.DeploymentInfo;

import org.jboss.logging.Logger;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.MessageListenerMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import org.jboss.resource.deployment.ResourceAdapterObjectModelFactory;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class Ejb3DeploymentInfo extends DeploymentInfo
{
   private static final long serialVersionUID = -4205809229239091579L;

   private static final Logger log = Logger.getLogger(Ejb3DeploymentInfo.class);
   
   protected String rarName;
   protected String listenerType;
   protected String activationSpecType;
  
   public Ejb3DeploymentInfo(String rarName, String listenerType, String activationSpecType) throws Exception
   {
      super(Thread.currentThread().getContextClassLoader().getResource(rarName), null, null);
      
      this.rarName = rarName;
      this.listenerType = listenerType;
      this.activationSpecType = activationSpecType;
        
      MessageListenerMetaData listener = new MessageListenerMetaData();
      listener.setType(listenerType);
      listener.setActivationSpecType(activationSpecType);
      ConnectorMetaData metaData = getConnectorMetaData();
      this.metaData = metaData;
   }
   
   protected ConnectorMetaData getConnectorMetaData() throws Exception
   {  
      ObjectModelFactory factory = new ResourceAdapterObjectModelFactory();
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      
      URL rar = Thread.currentThread().getContextClassLoader().getResource(rarName);
      JarFile rarFile = new JarFile(rar.getFile());
      ZipEntry entry = rarFile.getEntry("META-INF/ra.xml");
      
      ConnectorMetaData metaData = (ConnectorMetaData) unmarshaller.unmarshal(rarFile.getInputStream(entry),
               factory, null);
      
      return metaData;
   }
}
