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

import org.jboss.annotation.ejb.LocalHomeBinding;
import org.jboss.annotation.ejb.RemoteHomeBinding;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.aop.Advisor;
import org.jboss.ejb.LocalImpl;
import org.jboss.ejb.RemoteImpl;
import org.jboss.logging.Logger;
import org.jboss.ejb3.remoting.RemoteProxyFactory;

import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.jws.WebService;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class ProxyFactoryHelper
{
   private static final Logger log = Logger.getLogger(ProxyFactoryHelper.class);

   public static Context getProxyFactoryContext(Context ctx)
           throws NamingException
   {

      try
      {
         return (Context) ctx.lookup("proxyFactory");
      }
      catch (NameNotFoundException e)
      {
         return ctx.createSubcontext("proxyFactory");
      }
   }

   public static String getEndpointInterface(Container container)
   {
      WebService ws = (javax.jws.WebService) ((EJBContainer) container).resolveAnnotation(javax.jws.WebService.class);
      if (ws != null)
      {
         return ws.endpointInterface();
      }
      return null;

   }

   public static Class[] getLocalInterfaces(Container container)
   {
      Local li = (javax.ejb.Local) ((EJBContainer) container).resolveAnnotation(javax.ejb.Local.class);

      if (li != null)
      {
         if (li.value().length > 0) return li.value();

         // We have an emtpy @Local annotated bean class

         ArrayList list = getBusinessInterfaces(container.getBeanClass());
         if (list.size() == 0)
            throw new RuntimeException("Use of empty @Local on bean class and there are no valid business interfaces: " + container.getEjbName());
         if (list.size() > 1)
            throw new RuntimeException("Use of empty @Local on bean class and there are more than one default interface: " + container.getEjbName());
         Class[] rtn = {(Class) list.get(0)};
         li = new LocalImpl(rtn);
         ((EJBContainer) container).getAnnotations().addClassAnnotation(javax.ejb.Local.class, li);
         return rtn;
      }

      Class beanClass = container.getBeanClass();
      String endpoint = getEndpointInterface(container);
      Class[] ri = getRemoteInterfaces(container);

      if (li == null && ri == null && endpoint == null && (beanClass.getInterfaces() == null || beanClass.getInterfaces().length == 0))
         throw new RuntimeException("bean class has no local, webservice, or remote interfaces defined and does not implement at least one business interface: " + container.getEjbName());

      // introspect implemented interfaces.
      if (li == null)
      {
         Class[] intfs = beanClass.getInterfaces();
         ArrayList<Class> locals = new ArrayList<Class>();
         for (Class clazz : intfs)
         {
            if (clazz.isAnnotationPresent(javax.ejb.Local.class))
            {
               locals.add(clazz);
            }
         }
         if (locals.size() > 0)
         {
            intfs = locals.toArray(new Class[locals.size()]);
            li = new LocalImpl(intfs);
            ((Advisor) container).getAnnotations().addClassAnnotation(javax.ejb.Local.class, li);
            //return li.value(); ALR Removed (EJBTHREE-751)
         }
      }
      // no @Local interfaces implemented
      if (li == null)
      {
         // search for default
         ArrayList<Class> interfaces = getBusinessInterfaces(beanClass);
         if (interfaces.size() != 1) return null; // indeterminate

         Class intf = interfaces.get(0);
         if (ri != null)
         {
            for (Class rintf : ri)
            {
               if (intf.getName().equals(rintf.getName()))
               {
                  return null;
               }
            }
         }
         if (intf.getName().equals(endpoint)) return null;

         Class[] rtn = {intf};
         li = new LocalImpl(rtn);
         ((EJBContainer) container).getAnnotations().addClassAnnotation(javax.ejb.Local.class, li);
         return rtn;
      }
      

      // Check to ensure @Local and @Remote are not defined on the same interface
      // JIRA EJBTHREE-751
      if(ri != null)
      {
         for (Class remoteInterface : ri)
         {
            for (Class localInterface : li.value())
            {
               if (localInterface.equals(remoteInterface))
               {
                  throw new RuntimeException("@Remote and @Local may not both be specified on the same interface \""
                        + remoteInterface.toString() + "\" per EJB3 Spec 4.6.7, Bullet 5.4");
               }
            }
         }
      }
      
      return li.value();
   }

   public static ArrayList<Class> getBusinessInterfaces(Class beanClass)
   {
      ArrayList<Class> interfaces = new ArrayList<Class>(Arrays.asList(beanClass.getInterfaces()));
      interfaces.remove(java.io.Serializable.class);
      interfaces.remove(java.io.Externalizable.class);
      interfaces.remove(javax.ejb.SessionSynchronization.class);
      interfaces.remove(javax.ejb.TimedObject.class);
      Iterator<Class> it = interfaces.iterator();
      while (it.hasNext())
      {
         if (it.next().getName().startsWith("javax.ejb")) it.remove();
      }
      return interfaces;
   }

   public static Class getLocalHomeInterface(Container container)
   {
      Class beanClass = container.getBeanClass();
      LocalHome li = (javax.ejb.LocalHome) ((EJBContainer) container).resolveAnnotation(javax.ejb.LocalHome.class);
      if (li != null) return li.value();
      return null;
   }

   public static Class getRemoteHomeInterface(Container container)
   {
      Class beanClass = container.getBeanClass();
      RemoteHome li = (javax.ejb.RemoteHome) ((EJBContainer) container).resolveAnnotation(javax.ejb.RemoteHome.class);
      if (li != null) return li.value();
      return null;
   }

   public static boolean publishesInterface(Container container, Class businessInterface)
   {
      if (!(container instanceof SessionContainer)) return false;
      Class[] remotes = getRemoteInterfaces(container);
      if (remotes != null)
      {
         for (Class intf : remotes)
         {
            if (intf.getName().equals(businessInterface.getName())) return true;
         }
      }

      Class remoteHome = getRemoteHomeInterface(container);
      if (remoteHome != null)
      {
         if (businessInterface.getName().equals(remoteHome.getName()))
         {
            return true;
         }
      }
      Class[] locals = getLocalInterfaces(container);
      if (locals != null)
      {
         for (Class clazz : locals)
         {
            if (clazz.getName().equals(businessInterface.getName()))
            {
               return true;
            }
         }
      }
      Class localHome = getLocalHomeInterface(container);
      if (localHome != null)
      {
         if (businessInterface.getName().equals(localHome.getName()))
         {
            return true;
         }
      }

      return false;
   }
   
   public static String getHomeJndiName(Container container)
   {
      Advisor advisor = (Advisor) container;
      RemoteHomeBinding binding = (RemoteHomeBinding)advisor.resolveAnnotation(RemoteHomeBinding.class);
      if (binding != null)
         return binding.jndiBinding();
      
      return container.getEjbName() + "/home";
   }
   
   public static String getLocalHomeJndiName(Container container)
   {
      Advisor advisor = (Advisor) container;
      LocalHomeBinding binding = (LocalHomeBinding)advisor.resolveAnnotation(LocalHomeBinding.class);
      if (binding != null)
         return binding.jndiBinding();
      
      return container.getEjbName() + "/localHome";
   }

   public static String getJndiName(Container container, Class businessInterface)
   {
      if (!(container instanceof SessionContainer)) return null;
      Advisor advisor = (Advisor) container;
      Class[] remotes = getRemoteInterfaces(container);
      if (remotes != null)
      {
         for (Class clazz : remotes)
         {
            if (clazz.getName().equals(businessInterface.getName()))
            {
               RemoteBindings bindings = (RemoteBindings) advisor.resolveAnnotation(RemoteBindings.class);
               if (bindings == null)
               {
                  RemoteBinding binding = (RemoteBinding) advisor.resolveAnnotation(RemoteBinding.class);
                  if (binding == null)
                     throw new RuntimeException("RemoteBindings should not be null: " + container.getEjbName());

                  return getRemoteJndiName(container, binding);
               }
               return getRemoteJndiName(container, bindings.value()[0]);
            }
         }
      }
      Class remoteHome = getRemoteHomeInterface(container);
      if (remoteHome != null)
      {
         if (businessInterface.getName().equals(remoteHome.getName()))
         {
        	 return getHomeJndiName(container);
         }
      }
      Class[] locals = getLocalInterfaces(container);
      if (locals != null)
      {
         for (Class clazz : locals)
         {
            if (clazz.getName().equals(businessInterface.getName()))
            {
               return getLocalJndiName(container);
            }
         }
      }
      Class localHome = getLocalHomeInterface(container);
      if (localHome != null)
      {
         if (businessInterface.getName().equals(localHome.getName()))
         {
        	 return getLocalHomeJndiName(container);
         }
      }

      return null;
   }

   public static String getLocalJndiName(Container container)
   {
      return getLocalJndiName(container, true);
   }

   public static String getLocalJndiName(Container container, boolean conflictCheck)
   {
      Advisor advisor = (Advisor) container;
      LocalBinding localBinding = (LocalBinding) advisor
              .resolveAnnotation(LocalBinding.class);
      if (localBinding == null)
      {
         String name = container.getEjbName() + "/local";
         DeploymentScope deploymentScope = ((EJBContainer) container).getDeployment().getEar();
         if (deploymentScope != null) return deploymentScope.getBaseName() + "/" + name;

         if (conflictCheck)
            checkForRemoteJndiConflict(container);

         return name;
      }
      else
      {
         return localBinding.jndiBinding();
      }
   }

   private static void checkForRemoteJndiConflict(Container container)
   {
      if (((Advisor) container).resolveAnnotation(Remote.class) != null)
      {
         String remoteJndiName = getRemoteJndiName(container, false);
         String ejbName = container.getEjbName();
         if ((remoteJndiName.equals(ejbName) || remoteJndiName.startsWith(ejbName + "/")) && (!remoteJndiName.equals(ejbName + "/remote")))
            throw new javax.ejb.EJBException("Conflict between default local jndi name " + ejbName + "/local and remote jndi name " + remoteJndiName + " for ejb-name:" + ejbName + ", bean class=" + container.getBeanClass());
      }
   }

   public static Class[] getRemoteInterfaces(Container container)
   {
      Remote ri = (Remote) ((Advisor) container).resolveAnnotation(Remote.class);
      if (ri == null)
      {
         Class beanClass = container.getBeanClass();
         Class[] intfs = beanClass.getInterfaces();
         ArrayList<Class> remotes = new ArrayList<Class>();
         for (Class clazz : intfs)
         {
            if (clazz.isAnnotationPresent(Remote.class))
            {
               remotes.add(clazz);
            }
         }
         if (remotes.size() > 0)
         {
            intfs = remotes.toArray(new Class[remotes.size()]);
            ri = new RemoteImpl(intfs);
            ((Advisor) container).getAnnotations().addClassAnnotation(Remote.class, ri);
            return ri.value();
         }

         return null;
      }

      if (ri.value().length > 0) return ri.value();

      // We have an emtpy @Remote annotated bean class

      ArrayList list = getBusinessInterfaces(container.getBeanClass());
      if (list.size() == 0)
         throw new RuntimeException("Use of empty @Remote on bean class and there are no valid business interfaces: " + container.getEjbName());
      if (list.size() > 1)
         throw new RuntimeException("Use of empty @Remote on bean class and there are more than one default interface: " + container.getEjbName());
      Class[] rtn = {(Class) list.get(0)};
      ri = new RemoteImpl(rtn);
      ((EJBContainer) container).getAnnotations().addClassAnnotation(javax.ejb.Remote.class, ri);
      return rtn;
   }

   public static String getRemoteJndiName(Container container)
   {
      return getRemoteJndiName(container, true);
   }

   public static String getRemoteJndiName(Container container, boolean check)
   {
      Advisor advisor = (Advisor) container;
      RemoteBinding binding = (RemoteBinding) advisor
              .resolveAnnotation(RemoteBinding.class);

      return getRemoteJndiName(container, binding);
   }

   private static void checkForLocalJndiConflict(Container container)
   {
      if (((Advisor) container).resolveAnnotation(Local.class) != null)
      {
         String localJndiName = getLocalJndiName(container, false);
         String ejbName = container.getEjbName();
         if ((localJndiName.equals(ejbName) || localJndiName.startsWith(ejbName + "/")) && (!localJndiName.equals(ejbName + "/local")))
            throw new javax.ejb.EJBException("Conflict between default remote jndi name " + ejbName + "/remote and local jndi name " + localJndiName + " for ejb-name:" + ejbName + ", bean class=" + container.getBeanClass());

      }
   }

   public static String getRemoteJndiName(Container container, RemoteBinding binding)
   {
      return getRemoteJndiName(container, binding, true);
   }

   public static String getRemoteJndiName(Container container, RemoteBinding binding, boolean conflictCheck)
   {
      String jndiName = null;
      if (binding == null || binding.jndiBinding() == null || binding.jndiBinding().equals(""))
      {
         jndiName = getDefaultRemoteJndiName(container);

         if (conflictCheck)
            checkForLocalJndiConflict(container);
      }
      else
      {
         jndiName = binding.jndiBinding();
      }

      return jndiName;
   }

   public static String getDefaultRemoteJndiName(Container container)
   {
      String name = container.getEjbName() + "/remote";
      DeploymentScope deploymentScope = ((EJBContainer) container).getDeployment().getEar();
      if (deploymentScope != null) return deploymentScope.getBaseName() + "/" + name;
      return name;
   }
   
   public static String getClientBindUrl(RemoteBinding binding) throws Exception
   {
      String clientBindUrl = binding.clientBindUrl();
      if (clientBindUrl.trim().length() == 0)
      {
         ObjectName connectionON = new ObjectName("jboss.remoting:type=Connector,name=DefaultEjb3Connector,handler=ejb3");
         KernelAbstraction kernelAbstraction = KernelAbstractionFactory.getInstance();
         try
         {
            clientBindUrl = (String)kernelAbstraction.getAttribute(connectionON, "InvokerLocator");
         }
         catch (Exception e)
         {
            clientBindUrl = RemoteProxyFactory.DEFAULT_CLIENT_BINDING;
         }
      }
      
      return clientBindUrl;
   }
}
