/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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

import java.net.URL;
import java.util.HashMap;

import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.cfg.EJB3DTDEntityResolver;
import org.hibernate.ejb.packaging.PersistenceXmlLoader;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb3.metadata.jpa.spec.PersistenceUnitsMetaData;
import org.jboss.ejb3.protocol.jarjar.Handler;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * Find and parse persistence.xml.
 * 
 * In a jar:
 * META-INF/persistence.xml
 * 
 * In a war (JPA 6.2):
 * WEB-INF/classes/META-INF/persistence.xml
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PersistenceUnitParsingDeployer extends AbstractVFSParsingDeployer<PersistenceUnitsMetaData>
{
   private static final Logger log = Logger.getLogger(PersistenceUnitParsingDeployer.class);
   
   static
   {
      Handler.init();
   }
   
   public PersistenceUnitParsingDeployer()
   {
      super(PersistenceUnitsMetaData.class);
      setName("persistence.xml");
   }

   @Override
   protected PersistenceUnitsMetaData parse(DeploymentUnit unit, String name, PersistenceUnitsMetaData root) throws Exception
   {
      // Try to find the metadata
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;

      VirtualFile file = vfsDeploymentUnit.getMetaDataFile(name);
      if (file == null)
      {
         // FIXME: hack to get a war persistence unit
         try
         {
            file = vfsDeploymentUnit.getFile("WEB-INF/classes/META-INF/persistence.xml");
            if(file == null) 
               return null;
         }
         catch(IllegalStateException e)
         {
            return null;
         }
         // -- //
      }
      
      PersistenceUnitsMetaData result = parse(vfsDeploymentUnit, file, root);
      if (result != null)
         init(vfsDeploymentUnit, result, file);
      return result;
   }

   @Override
   protected PersistenceUnitsMetaData parse(VFSDeploymentUnit unit, VirtualFile file, PersistenceUnitsMetaData root)
         throws Exception
   {
      VirtualFile persistenceRoot = file.getParent().getParent();
      
      // We can't pass in a VFS url.
      //URL persistenceUnitRootUrl = persistenceRoot.toURL();
      
      // FIXME: is this a supported hack?
      // This introduces severe regression, will go the jar: url way (EJB-326)
      //URL persistenceUnitRootUrl = new URL("jarjar:" + persistenceRoot.getHandler().toURL());
      
      // http://opensource.atlassian.com/projects/hibernate/browse/EJB-326
      URL persistenceUnitRootUrl = persistenceRoot.getHandler().toURL();
      assert persistenceUnitRootUrl.getProtocol().equals("jar") || persistenceUnitRootUrl.getProtocol().equals("file") : "expected a jar or file url, but was " + persistenceUnitRootUrl;
      
      URL persistenceXmlUrl = file.toURL();
      PersistenceUnitsMetaData metaData = new PersistenceUnitsMetaData(persistenceUnitRootUrl, PersistenceXmlLoader.deploy(persistenceXmlUrl, new HashMap<String, String>(),
            new EJB3DTDEntityResolver(), PersistenceUnitTransactionType.JTA));
      log.info("Found persistence units " + metaData);
      // FIXME: if in EAR then unscoped else scoped
      return metaData;
   }
}
