/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.deployers;

import java.io.IOException;

import org.jboss.deployers.plugins.deployers.helpers.ObjectModelFactoryDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ejb3.metamodel.ApplicationClientDD;
import org.jboss.ejb3.metamodel.ApplicationClientDDObjectFactory;
import org.jboss.util.xml.DOMUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.binding.ObjectModelFactory;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentType;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AppClientParsingDeployer extends ObjectModelFactoryDeployer<ApplicationClientDD>
{
   private String appClientXmlPath = "application-client.xml";
   
   public AppClientParsingDeployer()
   {
      super(ApplicationClientDD.class);
   }
   
   @Override
   protected ObjectModelFactory getObjectModelFactory(ApplicationClientDD root)
   {
      return new ApplicationClientDDObjectFactory();
   }

   @Override
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (accepts(unit))
         createMetaData(unit, appClientXmlPath, null);
   }
   
   /**
    * This method looks to the deployment for a META-INF/application-client.xml
    * descriptor to identify a j2ee client jar.
    */
   private boolean accepts(DeploymentUnit unit) throws DeploymentException
   {
      boolean accepts = false;

      // The jar must contain an META-INF/application-client.xml
      VirtualFile dd = unit.getMetaDataFile(appClientXmlPath);
      if (dd != null)
      {
         log.debug("Found application-client.xml file: " + unit.getName());
         try
         {
            Element root = DOMUtils.parse(dd.openStream());
            String namespaceURI = root.getNamespaceURI();
            // Accept the J2EE5 namespace
            accepts = "http://java.sun.com/xml/ns/javaee".equals(namespaceURI);
            if (accepts == false)
               log.debug("Ignore application-client.xml with namespace: " + namespaceURI);
         }
         catch (IOException ex)
         {
            DeploymentException.rethrowAsDeploymentException("Cannot parse " + appClientXmlPath, ex);
         }
      }

      if(accepts)
      {
         // in the cts there are apps with application-client with version 5
         // and jboss-client with version 4
         dd = unit.getMetaDataFile("jboss-client.xml");
         if (dd != null)
         {
            log.debug("Found jboss-client.xml file: " + unit.getName());
            try
            {
               Element root = DOMUtils.parse(dd.openStream());
               DocumentType doctype = root.getOwnerDocument().getDoctype();
               String publicId = (doctype != null ? doctype.getPublicId() : null);

               accepts = !"-//JBoss//DTD Application Client 4.0//EN".equals(publicId);
            }
            catch (IOException ex)
            {
               DeploymentException.rethrowAsDeploymentException("Cannot parse " + appClientXmlPath, ex);
            }
         }
      }

      return accepts;
   }
}
