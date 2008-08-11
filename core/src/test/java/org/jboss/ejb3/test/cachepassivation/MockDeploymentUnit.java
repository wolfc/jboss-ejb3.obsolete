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
package org.jboss.ejb3.test.cachepassivation;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockDeploymentUnit implements DeploymentUnit
{

   public Object addAttachment(String name, Object attachment)
   {
      return null;
   }
   public Object getAttachment(String name)
   {
      return null;
   }
   public Object removeAttachment(String name)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getClassLoader()
    */
   public ClassLoader getClassLoader()
   {
      return Thread.currentThread().getContextClassLoader();
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getClasses()
    */
   @SuppressWarnings("unchecked")
   public List<Class> getClasses()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getDefaultEntityManagerName()
    */
   public String getDefaultEntityManagerName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getDefaultPersistenceProperties()
    */
   @SuppressWarnings("unchecked")
   public Map getDefaultPersistenceProperties()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getEjbJarXml()
    */
   public URL getEjbJarXml()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getInterceptorInfoRepository()
    */
   public InterceptorInfoRepository getInterceptorInfoRepository()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getJbossXml()
    */
   public URL getJbossXml()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getJndiProperties()
    */
   @SuppressWarnings("unchecked")
   public Hashtable getJndiProperties()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getMetaDataFile(java.lang.String)
    */
   public VirtualFile getMetaDataFile(String string)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getPersistenceXml()
    */
   public URL getPersistenceXml()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getRelativeURL(java.lang.String)
    */
   public URL getRelativeURL(String path)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getResourceLoader()
    */
   public ClassLoader getResourceLoader()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getResources(org.jboss.virtual.VirtualFileFilter)
    */
   public List<VirtualFile> getResources(VirtualFileFilter filter)
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getRootFile()
    */
   public VirtualFile getRootFile()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getShortName()
    */
   public String getShortName()
   {
      return null;
   }

   public String getRelativePath()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getUrl()
    */
   public URL getUrl()
   {
      return null;
   }

}
