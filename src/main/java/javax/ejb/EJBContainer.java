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
package javax.ejb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;

import org.jboss.ejb3.api.spi.EJBContainerProvider;
import org.jboss.ejb3.api.spi.EJBContainerWrapper;

/**
 * Used to execute an EJB application in an embeddable container.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 * @since 3.1
 */
public abstract class EJBContainer
{
   public static final String EMBEDDABLE_INITIAL = "javax.ejb.embeddable.initial";
   
   private static EJBContainerWrapper currentEJBContainer;
   
   private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");

   private static List<EJBContainerProvider> factories = new ArrayList<EJBContainerProvider>();
   
   static
   {
      findAllFactories();
   }
   
   /**
    * Shutdown an embeddable EJBContainer instance.
    */
   public abstract void close();
   
   /**
    * Create and initialize an embeddable EJB container. 
    * JVM classpath is searched for all ejb-jars or exploded ejb-jars in directory format.
    * 
    * @return EJBContainer instance
    * @throws EJBException Thrown if the container or application could not 
    *   be successfully initialized.
    */
   public static EJBContainer createEJBContainer() throws EJBException
   {
      return createEJBContainer(null);
   }
   
   /**
    * Create and initialize an embeddable EJB container with an 
    * optional set of configuration properties and names of modules to be initialized. 
    * 
    * @param properties One or more spec-defined or vendor-specific properties. 
    *   The spec reserves the prefix "javax.ejb." for spec-defined properties. Can be null.
    * @param modules Specific set of module names to be initialized. Can be null. 
    *   If null, defaults to module scanning algorithm in createEJBContainer(). 
    * @return EJBContainer instance
    * @throws EJBException Thrown if the container or application could not 
    *   be successfully initialized.
    */
   public static EJBContainer createEJBContainer(Map<?, ?> properties, String... modules)
      throws EJBException
   {
      for(EJBContainerProvider factory : factories)
      {
         EJBContainer container = factory.createEJBContainer(properties, modules);
         if(container != null)
         {
            currentEJBContainer = new EJBContainerWrapper(container);
            return currentEJBContainer;
         }
      }
      throw new EJBException("Unable to instantiate container with factories " + factories);
   }
   
   /**
    * Create and initialize an embeddable EJB container with an 
    * optional set of configuration properties and names of modules to be initialized. 
    * 
    * @param properties One or more spec-defined or vendor-specific properties. 
    *   The spec reserves the prefix "javax.ejb." for spec-defined properties. Can be null.
    * @param modules Specific set of module names to be initialized. Can be null. 
    *   If null, defaults to module scanning algorithm in createEJBContainer(). 
    * @return EJBContainer instance
    * @throws EJBException Thrown if the container or application could not 
    *   be successfully initialized.
    */
   @Deprecated
   public static EJBContainer createEJBContainer(Map<String, Object> properties, Set<String> modules)
      throws EJBException
   {
      String[] modulesArray = null;
      if(modules != null)
         modulesArray = modules.toArray(new String[0]);
      return createEJBContainer(properties, modulesArray);
   }
   
   private static List<String> factoryNamesFromReader(BufferedReader reader) throws IOException
   {
      List<String> names = new ArrayList<String>();
      String line;
      while ((line = reader.readLine()) != null)
      {
         line = line.trim();
         Matcher m = nonCommentPattern.matcher(line);
         if (m.find())
         {
            names.add(m.group().trim());
         }
      }
      return names;
   }
   
   private static void findAllFactories()
   {
      try
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         Enumeration<URL> resources = loader.getResources("META-INF/services/" + EJBContainerProvider.class.getName());
         Set<String> names = new HashSet<String>();
         while (resources.hasMoreElements())
         {
            URL url = resources.nextElement();
            InputStream is = url.openStream();
            try
            {
               names.addAll(factoryNamesFromReader(new BufferedReader(new InputStreamReader(is))));
            }
            finally
            {
               is.close();
            }
         }
         for (String s : names)
         {
            Class<?> factoryClass = loader.loadClass(s);
            factories.add(EJBContainerProvider.class.cast(factoryClass.newInstance()));
         }
      }
      catch (IOException e)
      {
         throw new EJBException(e);
      }
      catch (InstantiationException e)
      {
         throw new EJBException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new EJBException(e);
      }
      catch (ClassNotFoundException e)
      {
         throw new EJBException(e);
      }
   }
   
   /**
    * Retrieve the last EJBContainer instance to be successfully returned 
    * from an invocation to a createEJBContainer method. 
    * @return EJBContainer instance, or null if none exists or if the last EJBContainer 
    *   instance has been closed.
    */
   public static EJBContainer getCurrentEJBContainer()
   {
      if(currentEJBContainer != null && currentEJBContainer.isClosed())
         return null;
      return currentEJBContainer;
   }
   
   /**
    * Retrieve a naming context for looking up references to session beans executing in
    * the embeddable container.
    * 
    * @return The naming context.
    */
   public Context getContext()
   {
      throw new UnsupportedOperationException(this + " does not support a naming context");
   }
}
