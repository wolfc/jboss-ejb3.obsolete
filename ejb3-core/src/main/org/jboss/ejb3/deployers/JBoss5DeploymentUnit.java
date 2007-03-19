/*
* JBoss, Home of Professional Open Source
* Copyright 2005, Red Hat Middleware LLC., and individual contributors as indicated
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.context.jar.JarUtils;
import org.jboss.virtual.plugins.vfs.helpers.FilterVirtualFileVisitor;
import org.jboss.virtual.plugins.vfs.helpers.SuffixesExcludeFilter;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 43304 $
 */
public class JBoss5DeploymentUnit implements org.jboss.ejb3.DeploymentUnit
{
   private DeploymentUnit unit;
   private InterceptorInfoRepository interceptorInfoRepository = new InterceptorInfoRepository();
   private Map defaultPersistenceProperties;

   public JBoss5DeploymentUnit(DeploymentUnit unit)
   {
      this.unit = unit;
   }

   public VirtualFile getRootFile()
   {
      return unit.getDeploymentContext().getRoot();
   }
   
   public URL getRelativeURL(String jar)
   {
      try
      {
         return new URL(jar);
      }
      catch (MalformedURLException e)
      {
         try
         {
            if (jar.startsWith(".."))
            {
               if (getUrl() == null)
                  throw new RuntimeException("relative <jar-file> not allowed when standalone deployment unit is used");
               String tmpjar = jar.substring(3);
               VirtualFile vf = unit.getDeploymentContext().getRoot().getParent().findChild(tmpjar);
               return vf.toURL();
            }
            else
            {
               File fp = new File(jar);
               return fp.toURL();
            }
         }
         catch (Exception e1)
         {
            throw new RuntimeException("could not find relative path: " + jar, e1);
         }
      }
   }

   URL extractDescriptorUrl(String resource)
   {
      try
      {
         VirtualFile vf = unit.getMetaDataFile(resource);
         if (vf == null) return null;
         return vf.toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public URL getPersistenceXml()
   {
      return extractDescriptorUrl("persistence.xml");
   }

   public URL getEjbJarXml()
   {
      return extractDescriptorUrl("ejb-jar.xml");
   }

   public URL getJbossXml()
   {
      return extractDescriptorUrl("jboss.xml");
   }

   public VirtualFile getMetaDataFile(String name)
   {
      return unit.getMetaDataFile(name);
   }
   
   public List<Class> getClasses()
   {
      return null;
   }

   public ClassLoader getClassLoader()
   {
      return unit.getClassLoader();
   }

   public ClassLoader getResourceLoader()
   {
      return unit.getClassLoader();
   }

   public String getShortName()
   {
      return unit.getDeploymentContext().getRoot().getName();
   }

   public URL getUrl()
   {
      try
      {
         return unit.getDeploymentContext().getRoot().toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public String getDefaultEntityManagerName()
   {
      String url = getUrl().toString();
      String name = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
      return name;
   }

   public Map getDefaultPersistenceProperties()
   {
      return defaultPersistenceProperties;
   }

   public void setDefaultPersistenceProperties(Map defaultPersistenceProperties)
   {
      this.defaultPersistenceProperties = defaultPersistenceProperties;
   }

   public Hashtable getJndiProperties()
   {
      return null;
   }

   public InterceptorInfoRepository getInterceptorInfoRepository()
   {
      return interceptorInfoRepository;
   }

   public List<VirtualFile> getResources(VirtualFileFilter filter)
   {
      VisitorAttributes va = new VisitorAttributes();
      va.setLeavesOnly(true);
      SuffixesExcludeFilter noJars = new SuffixesExcludeFilter(JarUtils.getSuffixes());
      va.setRecurseFilter(noJars);
      FilterVirtualFileVisitor visitor = new FilterVirtualFileVisitor(filter, va);

      List<VirtualFile> classpath = unit.getDeploymentContext().getClassPath();
      if (classpath != null)
      {
         for (VirtualFile vf : classpath)
         {
            try
            {
               vf.visit(visitor);
            }
            catch (IOException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
      return visitor.getMatched();
   }
}
