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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.LinkRef;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployer;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.logging.Logger;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.ObjectNameConverter;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.util.file.ArchiveBrowser;
import org.jboss.util.file.ClassFileFilter;
import org.w3c.dom.Element;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

/**
 * Deployer for Aspects
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class EJB3Deployer extends SubDeployerSupport
   implements SubDeployer, EJB3DeployerMBean
{
   private final static Logger log = Logger.getLogger(EJB3Deployer.class);
   
   private ServiceControllerMBean serviceController;

   /** A map of current deployments */
   private HashMap deployments = new HashMap();

   /** Hold a proxy reference to myself, used when registering to MainDeployer */
   private SubDeployer thisProxy;

   private Properties DefaultProperties;
   
   private boolean deployEjb3ExtensionOnly;

   private HashSet ignoredJarsSet;
   private HashMap<DeploymentInfo, String> jmxNames = new HashMap();

   /**
    * Default CTOR used to set default values to the Suffixes and RelativeOrder
    * attributes. Those are read at subdeployer registration time by the MainDeployer
    * to alter its SuffixOrder.
    */
   public EJB3Deployer()
   {
      setSuffixes(new String[]{".jar", ".ejb3", ".par"});
      setRelativeOrder(400); // before old EJB 2.1 deployer
   }

   public static boolean hasFile(DeploymentInfo di, String filePath)
   {
      String urlStr = di.url.getFile();
      try
      {
         URL dd = di.localCl.findResource(filePath);
         if (dd != null)
         {

            // If the DD url is not a subset of the urlStr then this is coming
            // from a jar referenced by the deployment jar manifest and the
            // this deployment jar it should not be treated as persistence
            if (di.localUrl != null)
            {
               urlStr = di.localUrl.toString();
            }

            String ddStr = dd.toString();
            if (ddStr.indexOf(urlStr) >= 0)
            {
               return true;
            }
         }
      }
      catch (Exception ignore)
      {
      }
      return false;
   }

   public static boolean hasPersistenceXml(DeploymentInfo di)
   {
      return hasFile(di, "META-INF/persistence.xml");
   }

   public static boolean has30EjbJarXml(DeploymentInfo di)
   {
      if (!hasFile(di, "META-INF/ejb-jar.xml")) return false;
      InputStream ddStream = di.localCl.getResourceAsStream("META-INF/ejb-jar.xml");

      return has30EjbJarXml(ddStream);
   }

   public static boolean has30EjbJarXml(InputStream ddStream)
   {
      try
      {
         // look for version="3.0" in the file
         byte[] stringToFind = "version=\"3.0\"".getBytes();
         InputStreamReader reader = new InputStreamReader(ddStream);
         try
         {
            int idx = 0;
            int len = stringToFind.length;
            while (reader.ready())
            {
               int read = reader.read();
               if (read == stringToFind[idx])
               {
                  idx++;
                  if (idx == len)
                  {
                     return true;
                  }
               }
               else
               {
                  idx = 0;
               }
            }

         }
         finally
         {
            try
            {
               reader.close();
               ddStream.close();
            }
            catch (IOException ignored)
            {
            }
         }
      }
      catch (Exception ignore)
      {
      }
      return false;
   }

   protected boolean hasOnlyJbossXml(DeploymentInfo di)
   {
      if (!hasFile(di, "META-INF/ejb-jar.xml")
         && hasFile(di, "META-INF/jboss.xml"))
      {
         return true;
      }
      return false;
   }

   public boolean hasEjbAnnotation(DeploymentInfo di)
   {
      Iterator it = ArchiveBrowser.getBrowser(di.url, new ClassFileFilter());
      try
      {
         while (it.hasNext())
         {
            InputStream stream = (InputStream) it.next();
            DataInputStream dstream = new DataInputStream(new BufferedInputStream(stream));
            ClassFile cf = null;
            try
            {
               cf = new ClassFile(dstream);
               AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
               if (visible != null)
               {
                  if (EJB3Util.isStateless(visible)) return true;
                  if (EJB3Util.isStatefulSession(visible)) return true;
                  if (EJB3Util.isMessageDriven(visible)) return true;
                  if (EJB3Util.isConsumer(visible)) return true;
                  if (EJB3Util.isService(visible)) return true;
               }
            }
            finally
            {
               dstream.close();
               stream.close();
            }
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      return false;
   }

   /**
    * Returns true if this deployer can deploy the given DeploymentInfo.
    *
    * @return True if this deployer can deploy the given DeploymentInfo.
    * @jmx:managed-operation
    */
   public boolean accepts(DeploymentInfo di)
   {
      String urlStr = di.url.getFile();
      if (urlStr.endsWith(".ejb3") || urlStr.endsWith(".ejb3/") || urlStr.endsWith(".par") || urlStr.endsWith(".par/"))
      {
         return true;
      }
      
      // To be accepted the deployment's root name must end in jar
      if (!urlStr.endsWith(".jar") && !urlStr.endsWith(".jar/"))
      {
         return false;
      }

      if (ignoredJarsSet.contains(di.shortName))
      {
         return false;
      }
      
      if (has30EjbJarXml(di)) return true;
         
      if (!deployEjb3ExtensionOnly)
      {
         if (hasPersistenceXml(di)) return true;
         if (hasOnlyJbossXml(di)) return true;
         if (hasEjbAnnotation(di)) return true;
      }


      return false;
   }

   public Properties getDefaultProperties()
   {
      return DefaultProperties;
   }

   public void setJarsIgnoredForScanning(JarsIgnoredForScanningMBean mbean)
   {
      ignoredJarsSet = mbean.getIgnoredJarsSet();
   }
   
   public boolean getDeployEjb3ExtensionOnly()
   {
      return deployEjb3ExtensionOnly;
   }
   
   public void setDeployEjb3ExtensionOnly(boolean deployEjb3ExtensionOnly)
   {
      this.deployEjb3ExtensionOnly = deployEjb3ExtensionOnly;
   }

   /**
    * Overriden to set the hibernate.bytecode.provider from the
    * 
    * @throws Exception
    */
   protected void createService() throws Exception
   {
      URL propsUrl = this.getClass().getClassLoader().getResource("META-INF/persistence.properties");
      DefaultProperties = new Properties();
      DefaultProperties.load(propsUrl.openStream());
      log.debug("Default persistence.properties: " + DefaultProperties);
      /* Current hack to establish the hibernate bytecode provider from the
      externalized persistence.properties
      */
      String bcprovider = DefaultProperties.getProperty("hibernate.bytecode.provider", "javassist");
      System.setProperty("hibernate.bytecode.provider", bcprovider);
      super.createService();
   }

   /**
    * Get a reference to the ServiceController
    */
   protected void startService() throws Exception
   {
      serviceController = (ServiceControllerMBean)
              MBeanProxyExt.create(ServiceControllerMBean.class,
                                   ServiceControllerMBean.OBJECT_NAME, server);

      // make a proxy to myself, so that calls from the MainDeployer
      // can go through the MBeanServer, so interceptors can be added
      thisProxy = (SubDeployer)
              MBeanProxyExt.create(SubDeployer.class, super.getServiceName(), super.getServer());

      // register with the main deployer
      mainDeployer.addDeployer(thisProxy);

      // todo remove when we merge older model of ENC
      InitialContext iniCtx = new InitialContext();
      initializeJavaComp(iniCtx);
   }

   public static void initializeJavaComp(InitialContext iniCtx)
           throws NamingException
   {
      Context ctx = (Context) iniCtx.lookup("java:");
      ctx.rebind("comp.ejb3", new LinkRef("java:comp"));
   }

   /**
    * Implements the template method in superclass. This method stops all the
    * applications in this server.
    */
   protected void stopService() throws Exception
   {
      for (Iterator modules = deployments.values().iterator();
           modules.hasNext();)
      {
         DeploymentInfo di = (DeploymentInfo) modules.next();
         stop(di);
      }      // avoid concurrent modification exception
      for (Iterator modules = new ArrayList(deployments.values()).iterator();
           modules.hasNext();)
      {
         DeploymentInfo di = (DeploymentInfo) modules.next();
         destroy(di);
      }
      deployments.clear();

      // deregister with MainDeployer
      mainDeployer.removeDeployer(thisProxy);

      serviceController = null;
   }

   public void init(DeploymentInfo di) throws DeploymentException
   {     
      try
      {
         if( di.url.getProtocol().equalsIgnoreCase("file") )
         {
            File file = new File(di.url.getFile());

            if( !file.isDirectory() )
            {
               // If not directory we watch the package
               di.watch = di.url;
            }
            else
            {
               // If directory we watch the xml files
               di.watch = new URL(di.url, "META-INF/ejb-jar.xml");
            }
         }
         else
         {
            // We watch the top only, no directory support
            di.watch = di.url;
         }
         
         XmlFileLoader xfl = new XmlFileLoader();
         InputStream in = di.localCl.getResourceAsStream("META-INF/jboss.xml");
         if( in != null )
         {
            try
            {
               Element jboss = xfl.getDocument(in, "META-INF/jboss.xml").getDocumentElement();
               // Check for a ejb level class loading config
               Element loader = MetaData.getOptionalChild(jboss, "loader-repository");
               if( loader != null )
               {
                  LoaderRepositoryFactory.LoaderRepositoryConfig config =
                        LoaderRepositoryFactory.parseRepositoryConfig(loader);
                  di.setRepositoryInfo(config);
               }
               
               Element jmxNameElement = MetaData.getOptionalChild(jboss, "jmx-name");
               if (jmxNameElement != null)
               {
                  jmxNames.put(di, jmxNameElement.getChildNodes().item(0).getNodeValue());
               }
            }
            finally
            {
               in.close();
            }
         }
      }
      catch (Exception e)
      {
         if (e instanceof DeploymentException)
         {
            throw (DeploymentException) e;
         }
         throw new DeploymentException( "failed to initialize", e );
      }

      // invoke super-class initialization
      super.init(di);
   }

   public synchronized void create(DeploymentInfo di) throws DeploymentException
   {
      log.debug("create, " + di.shortName);

      try
      {
         // initialize the annotations loader
         URL loaderURL = (di.localUrl != null ? di.localUrl : di.url);
         di.annotationsCl = new URLClassLoader(new URL[]{loaderURL}, di.ucl);

         Ejb3Module ejbModule = new Ejb3Module(di);
         String name = jmxNames.get(di);
         if (name == null)
            name = Ejb3Module.BASE_EJB3_JMX_NAME + ",module=" + di.shortName;
         // Build an escaped JMX name including deployment shortname
         ObjectName ejbModuleName = ObjectNameConverter.convert(name);
         // Check that the name is not registered
         if (server.isRegistered(ejbModuleName) == true)
         {
            log.debug("The EJBModule name: " + ejbModuleName
                      + "is already registered, adding uid=" + System.identityHashCode(ejbModule));
            name = name + ",uid=" + System.identityHashCode(ejbModule);
            ejbModuleName = ObjectNameConverter.convert(name);
         }
         server.registerMBean(ejbModule, ejbModuleName);
         di.deployedObject = ejbModuleName;
         log.debug("Deploying: " + di.url);
         // Invoke the create life cycle method
         serviceController.create(di.deployedObject);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Error during create of EjbModule: "
                                       + di.url, e);
      }
      super.create(di);
   }

   public synchronized void start(DeploymentInfo di)
           throws DeploymentException
   {
      try
      {
         // Start application
         log.debug("start application, deploymentInfo: " + di +
                   ", short name: " + di.shortName +
                   ", parent short name: " +
                   (di.parent == null ? "null" : di.parent.shortName));
         serviceController.start(di.deployedObject);
         log.info("Deployed: " + di.url);         // Register deployment. Use the application name in the hashtable
         // FIXME: this is obsolete!! (really?!)
         deployments.put(di.url, di);
      }
      catch (Exception e)
      {
         stop(di);
         destroy(di);
         throw new DeploymentException("Could not deploy " + di.url, e);
      }
      super.start(di);
   }

   public void stop(DeploymentInfo di)
           throws DeploymentException
   {
      log.debug("init, " + di.shortName);
      try
      {
         serviceController.stop(di.deployedObject);
      }
      catch (Exception e)
      {
         throw new DeploymentException("problem stopping ejb module: " +
                                       di.url, e);
      }
      
      super.stop(di);
   }

   public void destroy(DeploymentInfo di)
           throws DeploymentException
   {
      // FIXME: If the put() is obsolete above, this is obsolete, too
      deployments.remove(di.url);
      try
      {
         serviceController.destroy(di.deployedObject);
         serviceController.remove(di.deployedObject);
      }
      catch (Exception e)
      {
         throw new DeploymentException("problem destroying ejb module: " +
                                       di.url, e);
      }
      
      jmxNames.remove(di);
      
      super.destroy(di);
   }
}
