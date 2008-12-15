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
package org.jboss.ejb3.test.deployers;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.attachments.Attachments;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.ClassLoaderFactory;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentContextVisitor;
import org.jboss.deployers.structure.spi.DeploymentResourceLoader;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.MutableMetaData;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * MockDeploymentContext
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class MockDeploymentContext implements DeploymentContext
{

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#addChild(org.jboss.deployers.structure.spi.DeploymentContext)
    */
   public void addChild(DeploymentContext child)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#addComponent(org.jboss.deployers.structure.spi.DeploymentContext)
    */
   public void addComponent(DeploymentContext component)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#addControllerContextName(java.lang.Object)
    */
   public void addControllerContextName(Object name)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#cleanup()
    */
   public void cleanup()
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#createClassLoader(org.jboss.deployers.structure.spi.ClassLoaderFactory)
    */
   public boolean createClassLoader(ClassLoaderFactory factory) throws DeploymentException
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#deployed()
    */
   public void deployed()
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getChildren()
    */
   public List<DeploymentContext> getChildren()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getClassLoader()
    */
   public ClassLoader getClassLoader()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getComparator()
    */
   public Comparator<DeploymentContext> getComparator()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getComponents()
    */
   public List<DeploymentContext> getComponents()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getControllerContextNames()
    */
   public Set<Object> getControllerContextNames()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getDependencyInfo()
    */
   public DependencyInfo getDependencyInfo()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getDeployment()
    */
   public Deployment getDeployment()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getDeploymentUnit()
    */
   public DeploymentUnit getDeploymentUnit()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getMetaData()
    */
   public MetaData getMetaData()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getMutableMetaData()
    */
   public MutableMetaData getMutableMetaData()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getMutableScope()
    */
   public ScopeKey getMutableScope()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getName()
    */
   public String getName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getParent()
    */
   public DeploymentContext getParent()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getProblem()
    */
   public Throwable getProblem()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getRelativeOrder()
    */
   public int getRelativeOrder()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getRelativePath()
    */
   public String getRelativePath()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getResourceClassLoader()
    */
   public ClassLoader getResourceClassLoader()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getResourceLoader()
    */
   public DeploymentResourceLoader getResourceLoader()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getScope()
    */
   public ScopeKey getScope()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getSimpleName()
    */
   public String getSimpleName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getState()
    */
   public DeploymentState getState()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#getTopLevel()
    */
   public DeploymentContext getTopLevel()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#isComponent()
    */
   public boolean isComponent()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#isDeployed()
    */
   public boolean isDeployed()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#isTopLevel()
    */
   public boolean isTopLevel()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#removeChild(org.jboss.deployers.structure.spi.DeploymentContext)
    */
   public boolean removeChild(DeploymentContext child)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#removeClassLoader()
    */
   public void removeClassLoader()
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#removeClassLoader(org.jboss.deployers.structure.spi.ClassLoaderFactory)
    */
   public void removeClassLoader(ClassLoaderFactory factory)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#removeComponent(org.jboss.deployers.structure.spi.DeploymentContext)
    */
   public boolean removeComponent(DeploymentContext component)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#removeControllerContextName(java.lang.Object)
    */
   public void removeControllerContextName(Object name)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setClassLoader(java.lang.ClassLoader)
    */
   public void setClassLoader(ClassLoader classLoader)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setComparator(java.util.Comparator)
    */
   public void setComparator(Comparator<DeploymentContext> comparator)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setDeployment(org.jboss.deployers.client.spi.Deployment)
    */
   public void setDeployment(Deployment deployment)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setDeploymentUnit(org.jboss.deployers.structure.spi.DeploymentUnit)
    */
   public void setDeploymentUnit(DeploymentUnit unit)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setMutableScope(org.jboss.metadata.spi.scope.ScopeKey)
    */
   public void setMutableScope(ScopeKey key)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setParent(org.jboss.deployers.structure.spi.DeploymentContext)
    */
   public void setParent(DeploymentContext parent)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setProblem(java.lang.Throwable)
    */
   public void setProblem(Throwable problem)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setRelativeOrder(int)
    */
   public void setRelativeOrder(int relativeOrder)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setScope(org.jboss.metadata.spi.scope.ScopeKey)
    */
   public void setScope(ScopeKey key)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#setState(org.jboss.deployers.spi.DeploymentState)
    */
   public void setState(DeploymentState state)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.structure.spi.DeploymentContext#visit(org.jboss.deployers.structure.spi.DeploymentContextVisitor)
    */
   public void visit(DeploymentContextVisitor visitor) throws DeploymentException
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.attachments.ManagedObjectsWithTransientAttachments#getTransientAttachments()
    */
   public MutableAttachments getTransientAttachments()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.attachments.ManagedObjectAttachments#getTransientManagedObjects()
    */
   public MutableAttachments getTransientManagedObjects()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments#getPredeterminedManagedObjects()
    */
   public Attachments getPredeterminedManagedObjects()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.deployers.spi.attachments.PredeterminedManagedObjectAttachments#setPredeterminedManagedObjects(org.jboss.deployers.spi.attachments.Attachments)
    */
   public void setPredeterminedManagedObjects(Attachments predetermined)
   {
      // TODO Auto-generated method stub

   }

}
