/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.deployers;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.AnnotatedClassFilter;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.spec.EjbJar3xMetaData;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;
import org.jboss.virtual.VirtualFile;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EjbAnnotationMetaDataDeployer extends AbstractDeployer
{
   public static final String EJB_ANNOTATED_ATTACHMENT_NAME = "annotated."+EjbJarMetaData.class.getName();

   public EjbAnnotationMetaDataDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      addInput(EjbJarMetaData.class);
      addOutput(EJB_ANNOTATED_ATTACHMENT_NAME);
   }
   
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (unit instanceof VFSDeploymentUnit == false)
         return;
      
      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      deploy(vfsDeploymentUnit);
   }
   
   protected void deploy(VFSDeploymentUnit unit) throws DeploymentException
   {
      boolean isComplete = false;
      EjbJarMetaData ejbJarMetaData = unit.getAttachment(EjbJarMetaData.class);
      if(ejbJarMetaData != null && ejbJarMetaData instanceof EjbJar3xMetaData)
      {
         isComplete |= ((EjbJar3xMetaData) ejbJarMetaData).isMetadataComplete();
      }
      
      if(isComplete)
      {
         log.debug("Deployment is metadata-complete, skipping annotation processing"
               + ", ejbJarMetaData="+ejbJarMetaData);
         return;
      }
      
      // why is this?
      VirtualFile root = unit.getRoot();
      boolean isLeaf = true;
      try
      {
         isLeaf = root.isLeaf();
      }
      catch(IOException ignore)
      {
         // ignore
      }
      if(isLeaf == true)
         return;

      List<VirtualFile> classpath = unit.getClassPath();
      if(classpath == null || classpath.isEmpty())
         return;

      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Deploying annotations for unit: " + unit + ", classpath: " + classpath);

      try
      {
         processMetaData(unit, classpath);
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot process metadata", e);
      }
   }
   
   /**
    * Get the classes we want to scan.
    *
    * @param unit the deployment unit
    * @param mainClassName the main class name
    * @param classpath the classpath
    * @return possible classes containing metadata annotations
    * @throws IOException for any error
    */
   protected Collection<Class<?>> getClasses(VFSDeploymentUnit unit, List<VirtualFile> classpath) throws IOException
   {
      Map<VirtualFile, Class<?>> classpathClasses = new HashMap<VirtualFile, Class<?>>();
      for(VirtualFile path : classpath)
      {
         AnnotatedClassFilter classVisitor = new AnnotatedClassFilter(unit, unit.getClassLoader(), path, null);
         path.visit(classVisitor);
         Map<VirtualFile, Class<?>> classes = classVisitor.getAnnotatedClasses();
         if(classes != null && classes.size() > 0)
         {
            if(log.isTraceEnabled())
               log.trace("Annotated classes: " + classes);
            classpathClasses.putAll(classes);
         }
      }
      return classpathClasses.values();
   }

   /**
    * Process annotations.
    *
    * @param unit the deployment unit
    * @param finder the annotation finder
    * @param classes the candidate classes
    */
   protected void processJBossMetaData(VFSDeploymentUnit unit, AnnotationFinder<AnnotatedElement> finder, Collection<Class<?>> classes)
   {
      // Create the metadata model from the annotations
      JBoss50Creator creator = new JBoss50Creator(finder);
      JBossMetaData annotationMetaData = creator.create(classes);
      if(annotationMetaData != null)
         unit.addAttachment(EJB_ANNOTATED_ATTACHMENT_NAME, annotationMetaData, JBossMetaData.class);
   }
   
   protected void processMetaData(VFSDeploymentUnit unit, List<VirtualFile> classpath) throws IOException
   {
      Collection<Class<?>> classes = getClasses(unit, classpath);
      if(classes.size() > 0)
      {
         AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
         processJBossMetaData(unit, finder, classes);
      }
   }
}
