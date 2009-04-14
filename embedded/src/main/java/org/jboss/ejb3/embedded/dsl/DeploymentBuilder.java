/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.embedded.dsl;

import java.io.IOException;
import java.net.URL;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DeploymentBuilder
{
   public static <T> Deployment deployment(String name, Attachment<?>... attachments)
   {
      try
      {
         URL url = new URL("vfsmemory", name, "");
         return deployment(url, attachments);
      }
      catch(IOException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public static <T> Deployment deployment(String name, Class<T> attachmentType, T attachment)
   {
      try
      {
         URL url = new URL("vfsmemory", name, "");
         VirtualFile root = VFS.getRoot(url);
         VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
         MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
         attachments.addAttachment(attachmentType, attachment);
         return deployment;
      }
      catch(IOException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public static Deployment deployment(URL url) throws IOException
   {
      VirtualFile root = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      /*
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      ClassLoadingMetaData attachment = new ClassLoadingMetaData();
//      attachment.setIncludedPackages("org.jboss.ejb3.embedded.test.stateless");
      attachment.setExcluded(ClassFilter.EVERYTHING);
      attachment.setIncluded(ClassFilter.NOTHING);
      attachment.setImportAll(false);
      attachments.addAttachment(ClassLoadingMetaData.class, attachment);
      
      AbstractScanningMetaData scanningMetaData = new AbstractScanningMetaData();
      List<PathMetaData> paths = new ArrayList<PathMetaData>();
      scanningMetaData.setPaths(paths);
      AbstractPathMetaData path = new AbstractPathMetaData();
      Set<PathEntryMetaData> excludes = new HashSet<PathEntryMetaData>();
      path.setExcludes(excludes);
      paths.add(path);
      AbstractPathEntryMetaData entry = new AbstractPathEntryMetaData();
      entry.setName("org.jboss.ejb3.embedded.test.jpa");
      excludes.add(entry);
      
      attachments.addAttachment(ScanningMetaData.class, scanningMetaData);
      */
      return deployment;
   }

   public static Deployment deployment(URL url, Attachment<?>... attachments) throws IOException
   {
      VirtualFile root = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      MutableAttachments managedObjects = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      for(Attachment<?> attachment : attachments)
      {
         processAttachment(managedObjects, attachment);
      }
      return deployment;
   }
   
   private static <T> void processAttachment(MutableAttachments managedObjects, Attachment<T> attachment)
   {
      managedObjects.addAttachment(attachment.getAttachmentType(), attachment.getAttachment());
   }
}
