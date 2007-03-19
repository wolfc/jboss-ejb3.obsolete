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
package org.jboss.ejb3;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.virtual.VFS;
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
 * @version $Revision$
 */
public class JmxDeploymentUnit implements DeploymentUnit
{
   protected static final Logger log = Logger.getLogger(JmxDeploymentUnit.class);

   private DeploymentInfo deploymentInfo;
   InterceptorInfoRepository interceptorInfoRepository = new InterceptorInfoRepository();
   private VirtualFile vfsRoot;

   public JmxDeploymentUnit(DeploymentInfo deploymentInfo)
   {
      this.deploymentInfo = deploymentInfo;
      try
      {
         VFS vfs = VFS.getVFS(deploymentInfo.url);
         vfsRoot = vfs.getRoot();
      }
      catch (IOException e)
      {
         throw new RuntimeException();
      }
   }

   public VirtualFile getRootFile()
   {
      return vfsRoot;
   }
   
   public URL getRelativeURL(String jar)
   {
      URL url = null;
      try
      {
         url = new URL(jar);
      }
      catch (MalformedURLException e)
      {
         try
         {
            if (jar.startsWith(".."))
            {
               if (getUrl() == null)
                  throw new RuntimeException("relative <jar-file> not allowed when standalone deployment unit is used");
               String base = getUrl().toString();
               jar = jar.replaceAll("\\.\\./", "+");
               int idx = jar.lastIndexOf('+');
               jar = jar.substring(idx + 1);
               for (int i = 0; i < idx + 1; i++)
               {
                  int slash = base.lastIndexOf('/');
                  base = base.substring(0, slash + 1);
               }
               url = new URL(base + jar.substring(idx));
            }
            else
            {
               File fp = new File(jar);
               url = fp.toURL();
            }
         }
         catch (MalformedURLException e1)
         {
            throw new RuntimeException("Unable to find relative url: " + jar, e1);
         }
      }
      return url;
   }

   URL extractDescriptorUrl(String resource)
   {
      String urlStr = deploymentInfo.url.getFile();
      // However the jar must also contain at least one ejb-jar.xml
      try
      {
         URL dd = deploymentInfo.localCl.findResource(resource);
         if (dd == null)
         {
            return null;
         }

         // If the DD url is not a subset of the urlStr then this is coming
         // from a jar referenced by the deployment jar manifest and the
         // this deployment jar it should not be treated as an ejb-jar
         if (deploymentInfo.localUrl != null)
         {
            urlStr = deploymentInfo.localUrl.toString();
         }

         String ddStr = dd.toString();
         if (ddStr.indexOf(urlStr) >= 0)
         {
            return dd;
         }
      }
      catch (Exception ignore)
      {
      }
      return null;
   }

   public URL getPersistenceXml()
   {
      return extractDescriptorUrl("META-INF/persistence.xml");
   }

   public URL getEjbJarXml()
   {
      return extractDescriptorUrl("META-INF/ejb-jar.xml");
   }

   public URL getJbossXml()
   {
      return extractDescriptorUrl("META-INF/jboss.xml");
   }

   public List<Class> getClasses()
   {
      return null;
   }

   public ClassLoader getClassLoader()
   {
      return deploymentInfo.ucl;
   }

   public ClassLoader getResourceLoader()
   {
      return deploymentInfo.localCl;
   }

   public String getShortName()
   {
      return deploymentInfo.shortName;
   }

   public URL getUrl()
   {
      return deploymentInfo.url;
   }

   public String getDefaultEntityManagerName()
   {
      String url = getUrl().toString();
      String name = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
      return name;
   }

   public Map getDefaultPersistenceProperties()
   {
      try
      {
         EJB3DeployerMBean deployer = (EJB3DeployerMBean) MBeanProxyExt.create(EJB3DeployerMBean.class, EJB3DeployerMBean.OBJECT_NAME,
                 deploymentInfo.getServer());

         return deployer.getDefaultProperties();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
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
      try
      {
         vfsRoot.visit(visitor);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      return visitor.getMatched();

   }

   public VirtualFile getMetaDataFile(String path)
   {
      try
      {
         return vfsRoot.findChild(path);
      }
      catch (IOException e)
      {
         log.debug("Cannot get meta data file: " + path);
         return null;
      }
   }
}
