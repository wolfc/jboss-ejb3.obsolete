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
package org.jboss.ejb3.singleton.test.common;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MockDeploymentUnit implements DeploymentUnit
{

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#addAttachment(java.lang.String, java.lang.Object)
    */
   public Object addAttachment(String name, Object attachment)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getAttachment(java.lang.String)
    */
   public Object getAttachment(String name)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getClassLoader()
    */
   public ClassLoader getClassLoader()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getClasses()
    */
   public List<Class> getClasses()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getDefaultEntityManagerName()
    */
   public String getDefaultEntityManagerName()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getDefaultPersistenceProperties()
    */
   public Map getDefaultPersistenceProperties()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getEjbJarXml()
    */
   public URL getEjbJarXml()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getInterceptorInfoRepository()
    */
   public InterceptorInfoRepository getInterceptorInfoRepository()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getJbossXml()
    */
   public URL getJbossXml()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getJndiProperties()
    */
   public Hashtable getJndiProperties()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getMetaDataFile(java.lang.String)
    */
   public VirtualFile getMetaDataFile(String string)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getPersistenceXml()
    */
   public URL getPersistenceXml()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getRelativePath()
    */
   public String getRelativePath()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getRelativeURL(java.lang.String)
    */
   public URL getRelativeURL(String path)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getResourceLoader()
    */
   public ClassLoader getResourceLoader()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getResources(org.jboss.virtual.VirtualFileFilter)
    */
   public List<VirtualFile> getResources(VirtualFileFilter filter)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getRootFile()
    */
   public VirtualFile getRootFile()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getShortName()
    */
   public String getShortName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#getUrl()
    */
   public URL getUrl()
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.DeploymentUnit#removeAttachment(java.lang.String)
    */
   public Object removeAttachment(String name)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

}
