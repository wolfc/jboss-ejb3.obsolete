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
package org.jboss.ejb3.test.interceptors2;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.ExcludeDefaultInterceptors;

import org.jboss.ejb3.annotation.Service;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
@Service
@ExcludeDefaultInterceptors
public class StatusBean implements StatusRemote
{
   static ArrayList<Interception> interceptions = new ArrayList<Interception>();
   static ArrayList<Interception> postConstructs = new ArrayList<Interception>();
   static ArrayList<Interception> postActivates = new ArrayList<Interception>();
   static ArrayList<Interception> prePassivates = new ArrayList<Interception>();
   static ArrayList<Interception> preDestroys = new ArrayList<Interception>();
   
   public void clear()
   {
      System.out.println("Clearing interceptions");
      interceptions.clear();
      postConstructs.clear();
      postActivates.clear();
      prePassivates.clear();
      preDestroys.clear();
   }
   
   public ArrayList<Interception> getInterceptions()
   {
      System.out.println("Getting interceptions " + interceptions.size());
      return interceptions;
   }
   
   
   public ArrayList<Interception> getPostActivates()
   {
      return postActivates;
   }

   public ArrayList<Interception> getPostConstructs()
   {
      return postConstructs;
   }

   public ArrayList<Interception> getPreDestroys()
   {
      return preDestroys;
   }

   public ArrayList<Interception> getPrePassivates()
   {
      return prePassivates;
   }

   public void addInterception(Interception intercepted)
   {
      interceptions.add(intercepted);
      System.out.println("Adding interception (" + interceptions.size() + ")" + intercepted.getClassname() + "." + intercepted.getMethod() + " - " + intercepted.getInstance());
   }
   
   public static void addInterceptionStatic(Interception intercepted)
   {
      interceptions.add(intercepted);
      System.out.println("Adding interception (" + interceptions.size() + ")" + intercepted.getClassname() + "." + intercepted.getMethod()  + " - " + intercepted.getInstance());
   }
   
   public void addLifecycle(Class type, Interception intercepted)
   {
      if (type == PostConstruct.class)
      {
         addPostConstruct(intercepted);
      }
      else if (type == PostActivate.class)
      {
         addPostActivate(intercepted);
      }
      else if (type == PrePassivate.class)
      {
         addPrePassivate(intercepted);
      }
      else if (type == PreDestroy.class)
      {
         addPreDestroy(intercepted);
      }
   }
   
   public static void addPostConstruct(Interception intercepted)
   {
      postConstructs.add(intercepted);
      System.out.println("Adding PostConstruct (" + postConstructs.size() + ")" + intercepted.getClassname() + "." + intercepted.getMethod() + " - " + intercepted.getInstance());
   }

   public static void addPostActivate(Interception intercepted)
   {
      postActivates.add(intercepted);
      System.out.println("Adding PostActivate (" + postConstructs.size() + ")" + intercepted.getClassname() + "." + intercepted.getMethod() + " - " + intercepted.getInstance());
   }

   public static void addPrePassivate(Interception intercepted)
   {
      prePassivates.add(intercepted);
      System.out.println("Adding PrePassivate (" + postConstructs.size() + ")" + intercepted.getClassname() + "." + intercepted.getMethod() + " - " + intercepted.getInstance());
   }

   public static void addPreDestroy(Interception intercepted)
   {
      preDestroys.add(intercepted);
      System.out.println("Adding PreDestroy (" + postConstructs.size() + ")" + intercepted.getClassname() + "." + intercepted.getMethod() + " - " + intercepted.getInstance());
   }

   
}
