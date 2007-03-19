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
import org.jboss.ejb3.metamodel.JBossClientDDObjectFactory;
import org.jboss.util.xml.DOMUtils;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.binding.ObjectModelFactory;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class JBossClientParsingDeployer extends ObjectModelFactoryDeployer<ApplicationClientDD>
{
   private String jbossClientXmlPath = "jboss-client.xml";
   
   /**
    * Set the relative order to PARSER_DEPLOYER+1 by default
    *
    */   
   public JBossClientParsingDeployer()
   {
      super(ApplicationClientDD.class);
      setRelativeOrder(PARSER_DEPLOYER + 1);
   }
   
   @Override
   protected boolean allowsReparse()
   {
      return true;
   }
   
   @Override
   protected ObjectModelFactory getObjectModelFactory(ApplicationClientDD root)
   {
      // if we haven't gotten an application-client.xml yet
      if(root == null)
         root = new ApplicationClientDD();
      
      return new JBossClientDDObjectFactory(root);
   }

   @Override
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (accepts(unit))
         createMetaData(unit, jbossClientXmlPath, null);
   }

   /**
    * This method looks to the deployment for a META-INF/application-client.xml
    * descriptor to identify a j2ee client jar.
    */
   private boolean accepts(DeploymentUnit unit) throws DeploymentException
   {
      boolean accepts = false;

      // The jar must contain an META-INF/application-client.xml
      VirtualFile dd = unit.getMetaDataFile(jbossClientXmlPath);
      if (dd != null)
      {
         log.debug("Found application-client.xml file: " + unit.getName());
         try
         {
            Element root = DOMUtils.parse(dd.openStream());
            DocumentType doctype = root.getOwnerDocument().getDoctype();
            String publicId = (doctype != null ? doctype.getPublicId() : null);
            // Accept the JBoss5 publicId
            accepts = "-//JBoss//DTD Application Client 5.0//EN".equals(publicId);
            if (accepts == false)
               log.debug("Ignore jboss-client.xml with publicId: " + publicId);
         }
         catch (IOException ex)
         {
            DeploymentException.rethrowAsDeploymentException("Cannot parse " + jbossClientXmlPath, ex);
         }
      }

      return accepts;
   }
}
