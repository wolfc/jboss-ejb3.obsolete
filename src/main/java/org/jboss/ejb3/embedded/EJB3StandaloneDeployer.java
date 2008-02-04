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
package org.jboss.ejb3.embedded;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.InitialContextFactory;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.context.jar.JarUtils;
import org.jboss.virtual.plugins.vfs.helpers.FilterVirtualFileVisitor;
import org.jboss.virtual.plugins.vfs.helpers.SuffixesExcludeFilter;

/**
 * When initialized properly, this class will search for annotated classes and archives in your
 * classpath and try to create EJB containers and EntityManagers automatically.
 * <p/>
 * All classes and jars must already be in your classpath for this deployer to work.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class EJB3StandaloneDeployer
{
   private static class DeployerUnit implements DeploymentUnit
   {
      private URL url;
      private ClassLoader resourceLoader;
      private ClassLoader loader;
      private Map defaultProps;
      private Hashtable jndiProperties;
      private InterceptorInfoRepository interceptorInfoRepository;
      private VirtualFile vfsRoot;

      public DeployerUnit(ClassLoader loader, URL url, Map defaultProps, Hashtable jndiProperties)
      {
         this.loader = loader;
         this.url = url;
         URL[] urls = {url};
         URL[] empty = {};
         URLClassLoader parent = new URLClassLoader(empty)
         {
            @Override
            public URL getResource(String name)
            {
               return null;
            }
         };
         resourceLoader = new URLClassLoader(urls, parent);
         this.defaultProps = defaultProps;
         this.jndiProperties = jndiProperties;
         try
         {
            VFS vfs = VFS.getVFS(url);
            vfsRoot = vfs.getRoot();
         }
         catch (IOException e)
         {
            throw new RuntimeException();
         }
         this.interceptorInfoRepository = new InterceptorInfoRepository(loader);
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

      public Hashtable getJndiProperties()
      {
         return jndiProperties;
      }

      public URL getPersistenceXml()
      {
         return getResourceLoader().getResource("META-INF/persistence.xml");
      }

      public URL getEjbJarXml()
      {
         return getResourceLoader().getResource("META-INF/ejb-jar.xml");
      }

      public URL getJbossXml()
      {
         return getResourceLoader().getResource("META-INF/jboss.xml");
      }

      public List<Class> getClasses()
      {
         return null;
      }

      public ClassLoader getClassLoader()
      {
         return loader;
      }

      public ClassLoader getResourceLoader()
      {
         return resourceLoader;
      }

      public String getShortName()
      {
         String url = getUrl().toString();
         if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

         int dotIdx = url.lastIndexOf('.');
         int slashIdx = url.lastIndexOf('/');
         String name = null;
         if (slashIdx > dotIdx)
         {
            name = url.substring(url.lastIndexOf('/') + 1);
         }
         else
         {
            name = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
         }
         return name;
      }

      public URL getUrl()
      {
         return url;
      }

      public String getDefaultEntityManagerName()
      {
         return getShortName();
      }

      public Map getDefaultPersistenceProperties()
      {
         return defaultProps;
      }

      public InterceptorInfoRepository getInterceptorInfoRepository()
      {
         return interceptorInfoRepository;
      }

      public VirtualFile getMetaDataFile(String path)
      {
         try
         {
            return vfsRoot.getChild(path);
         }
         catch (IOException e)
         {
            log.debug("Cannot get meta data file: " + path);
            return null;
         }
      }
   }

   protected static final Logger log = Logger.getLogger(EJB3StandaloneDeployer.class);

   protected Set<URL> archives = new HashSet<URL>();
   protected Set<URL> deployDirs = new HashSet<URL>();
   protected Set<String> archivesByResource = new HashSet<String>();
   protected Set<String> deployDirsByResource = new HashSet<String>();
   protected ClassLoader classLoader;
   private Map defaultPersistenceProperties;
   private Hashtable jndiProperties;

   private List<EJB3StandaloneDeployment> deployments = new ArrayList<EJB3StandaloneDeployment>();

   private Kernel kernel;
   private MBeanServer mbeanServer;


   public EJB3StandaloneDeployer()
   {
      classLoader = Thread.currentThread().getContextClassLoader();
   }

   public Kernel getKernel()
   {
      return kernel;
   }

   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   /**
    * This is used by deployer for @Service beans that have @Management interfaces
    *
    * @return
    */
   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   /**
    * A set of URLs of jar archives to search for annotated EJB classes
    *
    * @return this will not return null.
    */
   public Set<URL> getArchives()
   {
      return archives;
   }

   public void setArchives(Set archives)
   {
      new Exception().printStackTrace();
      this.archives = archives;
   }


   /**
    * Set of directories where there are jar files and directories
    * The deployer will search through all archives and directories for
    * annotated classes
    *
    * @return this will not return null.
    */
   public Set<URL> getDeployDirs()
   {
      return deployDirs;
   }

   public void setDeployDirs(Set deployDirs)
   {
      this.deployDirs = deployDirs;
   }

   /**
    * All strings in this set will be used with ClassLoader.getResources()
    * All URLs returned will be used to search for annotated classes.
    *
    * @return
    */
   public Set<String> getArchivesByResource()
   {
      return archivesByResource;
   }

   public void setArchivesByResource(Set archivesByResource)
   {
      this.archivesByResource = archivesByResource;
   }

   /**
    * All strings in this set will be used with ClassLoader.getResources().
    * All URLs returned will be create a set of deploy directories that will
    * be used to find archives and directories that will be searched for annotated classes
    *
    * @return
    */
   public Set<String> getDeployDirsByResource()
   {
      return deployDirsByResource;
   }

   public void setDeployDirsByResource(Set deployDirsByResource)
   {
      this.deployDirsByResource = deployDirsByResource;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }


   /**
    * You can set the classloader that will be used
    *
    * @param classLoader
    */
   public void setClassLoader(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   public Map getDefaultPersistenceProperties()
   {
      return defaultPersistenceProperties;
   }


   /**
    * If you do not specifiy the default persistence properties, the resource
    * "default.persistence.properties" will be search for in your classpath
    *
    * @param defaultPersistenceProperties
    */
   public void setDefaultPersistenceProperties(Map defaultPersistenceProperties)
   {
      this.defaultPersistenceProperties = defaultPersistenceProperties;
   }

   public Hashtable getJndiProperties()
   {
      return jndiProperties;
   }

   public void setJndiProperties(Hashtable jndiProperties)
   {
      this.jndiProperties = jndiProperties;
   }

   /**
    * Returns a list of deployments found in this
    *
    * @return
    */
   public List<EJB3StandaloneDeployment> getDeployments()
   {
      return deployments;
   }

   public void setDeployments(List<EJB3StandaloneDeployment> deployments)
   {
      this.deployments = deployments;
   }

   public static URL getContainingUrlFromResource(URL url, String resource) throws Exception
   {
      if (url.getProtocol().equals("jar"))
      {
         URL jarURL = url;
         URLConnection urlConn = jarURL.openConnection();
         JarURLConnection jarConn = (JarURLConnection) urlConn;
         // Extract the archive to dest/jarName-contents/archive
         String parentArchiveName = jarConn.getJarFile().getName();
         File fp = new File(parentArchiveName);
         return fp.toURL();
      }

      // its a file
      String base = url.toString();
      int idx = base.lastIndexOf(resource);
      base = base.substring(0, idx);
      return new URL(base);
   }

   public static URL getDeployDirFromResource(URL url, String resource) throws Exception
   {
      if (url.getProtocol().equals("jar"))
      {
         URL jarURL = url;
         URLConnection urlConn = jarURL.openConnection();
         JarURLConnection jarConn = (JarURLConnection) urlConn;
         // Extract the archive to dest/jarName-contents/archive
         String parentArchiveName = jarConn.getJarFile().getName();
         File fp = new File(parentArchiveName);
         return fp.getParentFile().toURL();
      }

      // its a file
      String base = url.toString();
      int idx = base.lastIndexOf(resource);
      base = base.substring(0, idx);
      File fp = new File(base);
      return fp.getParentFile().toURL();
   }

   public void create() throws Exception
   {
      try
      {
         for (String resource : deployDirsByResource)
         {
            Enumeration<URL> urls = classLoader.getResources(resource);
            while (urls.hasMoreElements())
            {
               URL url = urls.nextElement();
               URL deployUrl = getDeployDirFromResource(url, resource);
               deployDirs.add(deployUrl);
            }
         }

         for (URL url : deployDirs)
         {
            File dir = new File(url.toURI());
            for (File fp : dir.listFiles())
            {
               if (fp.isDirectory())
               {
                  archives.add(fp.toURL());
                  continue;
               }
               try
               {
                  ZipFile zip = new ZipFile(fp);
                  zip.entries();
                  zip.close();
                  archives.add(fp.toURL());
               }
               catch (IOException e)
               {
               }
            }
         }

         for (String resource : archivesByResource)
         {
            Enumeration<URL> urls = classLoader.getResources(resource);
            while (urls.hasMoreElements())
            {
               URL url = urls.nextElement();
               URL archiveUrl = getContainingUrlFromResource(url, resource);
               archives.add(archiveUrl);
            }
         }

         if (defaultPersistenceProperties == null)
         {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("default.persistence.properties");
            if (is == null) throw new RuntimeException("cannot find default.persistence.properties");
            Properties defaults = new Properties();
            defaults.load(is);
            defaultPersistenceProperties = defaults;
         }

         for (URL archive : archives)
         {
            DeployerUnit du = new DeployerUnit(classLoader, archive, defaultPersistenceProperties, jndiProperties);
            EJB3StandaloneDeployment deployment = new EJB3StandaloneDeployment(du, kernel, mbeanServer);
            deployments.add(deployment);
            deployment.create();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }

   private void lookup(String name)
   {
      System.out.println("lookup " + name);
      try {
         InitialContext jndiContext = InitialContextFactory.getInitialContext();
         NamingEnumeration names = jndiContext.list(name);
         if (names != null){
            while (names.hasMore()){
               System.out.println("  " + names.next());
            }
         }
      } catch (Exception e){
      }
   }

   public void start() throws Exception
   {
      try
      {
         loadMbeanServer();

         for (EJB3StandaloneDeployment deployment : deployments)
         {
            if (deployment.getMbeanServer() == null)
            {
               deployment.setMbeanServer(mbeanServer);
            }

            deployment.start();
            lookup("");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }

   private void loadMbeanServer()
   {
      if (mbeanServer == null)
      {
         ControllerContext context = kernel.getController().getInstalledContext("MBeanServer");

         if (context != null)
            mbeanServer = (MBeanServer) context.getTarget();
         else
         {
            ArrayList servers = MBeanServerFactory.findMBeanServer(null);
            if (servers.size() == 0)
               mbeanServer = MBeanServerFactory.createMBeanServer();
            else
               mbeanServer = (MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
         }
      }
   }

   public void stop() throws Exception
   {
      for (EJB3StandaloneDeployment deployment : deployments)
      {
         deployment.stop();
      }
   }

   public void destroy() throws Exception
   {
      for (EJB3StandaloneDeployment deployment : deployments)
      {
         deployment.destroy();
      }
   }
}
