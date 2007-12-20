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
package org.jboss.ejb3.interceptor;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;

import org.jboss.metadata.ejb.spec.InterceptorMetaData;

/**
 * We cannot use annotation overrides for the interceptor stuff since they do not have a 
 * container associated with them
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 66558 $
 */
public class InterceptorInfo
{
   Class<?> clazz;
   InterceptorMetaData xml;
   
   //interceptor methods defined by this class
   protected Method aroundInvoke;
   protected Method postConstruct;
   protected Method postActivate;
   protected Method preDestroy;
   protected Method prePassivate;
   
   //Interceptor methods defined by this class and all superclasses
   protected Method[] aroundInvokeHierarchy;
   protected Method[] postConstructHierarchy;
   protected Method[] postActivateHierarchy;
   protected Method[] preDestroyHierarchy;
   protected Method[] prePassivateHierarchy;
   
   boolean haveCalculatedHierarchy;
 
   protected InterceptorInfo()
   {
   }

   public InterceptorInfo(Class<?> clazz)
   {
      this.clazz = clazz;
   }
   
   public InterceptorInfo(InterceptorInfo interceptorInfo)
   {
      this.clazz = interceptorInfo.clazz;
      this.aroundInvoke = interceptorInfo.aroundInvoke;
      this.postConstruct = interceptorInfo.postConstruct;
      this.postActivate = interceptorInfo.postActivate;
      this.preDestroy = interceptorInfo.preDestroy;
      this.prePassivate = interceptorInfo.prePassivate;
      this.aroundInvokeHierarchy = interceptorInfo.aroundInvokeHierarchy;
      this.postConstructHierarchy = interceptorInfo.postConstructHierarchy;
      this.postActivateHierarchy = interceptorInfo.postActivateHierarchy;
      this.preDestroyHierarchy = interceptorInfo.preDestroyHierarchy;
      this.prePassivateHierarchy = interceptorInfo.prePassivateHierarchy;
   }

   protected void setXml(InterceptorMetaData xml)
   {
      this.xml = xml;
   }
   
   public InterceptorMetaData getXml()
   {
      return xml;
   }
   
   public Method getAroundInvoke()
   {
      return aroundInvoke;
   }

   protected void setAroundInvoke(Method aroundInvoke)
   {
      if (aroundInvoke == null) return;
      
      if (this.aroundInvoke != null && !this.aroundInvoke.equals(aroundInvoke))
      {
         throw new RuntimeException("Interceptors can only have one around-invoke/@AroundInvoke method - " + clazz.getName());
      }
      this.aroundInvoke = makeAccessible(aroundInvoke);
   }

   public Class getClazz()
   {
      return clazz;
   }

   public boolean haveCalculatedHierarchy()
   {
      return haveCalculatedHierarchy;
   }
   
   public Method getPostActivate()
   {
      return postActivate;
   }

   protected void setPostActivate(Method postActivate)
   {
      if (postActivate == null) return;
      
      if (this.postActivate != null && !this.postActivate.equals(postActivate))
      {
         throw new RuntimeException("Interceptors can only have one post-activate/@PostActivate method - " + clazz.getName());
      }
      this.postActivate = makeAccessible(postActivate);
   }

   public Method getPostConstruct()
   {
      return postConstruct;
   }

   protected void setPostConstruct(Method postConstruct)
   {
      if (postConstruct == null) return;
      
      if (this.postConstruct != null && !this.postConstruct.equals(postConstruct))
      {
         throw new RuntimeException("Interceptors can only have one post-construct/@PostConstruct method - " + clazz.getName());
      }
      this.postConstruct = makeAccessible(postConstruct);
   }

   public Method getPreDestroy()
   {
      return preDestroy;
   }

   protected void setPreDestroy(Method preDestroy)
   {
      if (preDestroy == null) return;
      
      if (this.preDestroy != null && !this.preDestroy.equals(preDestroy))
      {
         throw new RuntimeException("Interceptors can only have one pre-destroy/@PreDestroy method - " + clazz.getName());
      }
      this.preDestroy = makeAccessible(preDestroy);
   }

   public Method getPrePassivate()
   {
      return prePassivate;
   }

   protected void setPrePassivate(Method prePassivate)
   {
      if (prePassivate == null) return;
      
      if (this.prePassivate != null && !this.prePassivate.equals(prePassivate))
      {
         throw new RuntimeException("Interceptors can only have one pre-passivate/@PrePassivate method - " + clazz.getName());
      }
      this.prePassivate = makeAccessible(prePassivate);
   }
   
   public Method[] getAroundInvokes()
   {
      return aroundInvokeHierarchy;
   }

   public Method[] getPostActivates()
   {
      return postActivateHierarchy;
   }

   public Method[] getPostConstructs()
   {
      return postConstructHierarchy;
   }

   public Method[] getPreDestroys()
   {
      return preDestroyHierarchy;
   }

   public Method[] getPrePassivates()
   {
      return prePassivateHierarchy;
   }

   private Method makeAccessible(final Method method)
   {
      try
      {
         AccessController.doPrivileged(new PrivilegedExceptionAction() {
            public Object run()
            {
               method.setAccessible(true);
               return null;
            }
         });
      }
      catch (PrivilegedActionException e)
      {
         throw new RuntimeException(e.getException());
      }
      
      return method;
   }
   
   public String toString()
   {
      StringBuffer sb = new StringBuffer("InterceptorInfo{class=" + clazz);
      appendMethods(sb);
      sb.append("}");
      return sb.toString();
   }

   protected void appendMethods(StringBuffer sb)
   {
      appendMethodString(sb, "aroundInvoke", aroundInvoke);
      appendMethodString(sb, "postConstruct", postConstruct);
      appendMethodString(sb, "postActivate", postActivate);
      appendMethodString(sb, "prePassivate", prePassivate);
      appendMethodString(sb, "preDestroy", preDestroy);
   }
   
   protected void appendMethodString(StringBuffer buf, String methodType, Method m)
   {
      if (m != null)
      {
         buf.append(", " + methodType + "=" + m.getName());
      }
   }
   
   public void calculateHierarchy(InterceptorInfo superInfo)
   {
      if (haveCalculatedHierarchy)
      {
         return;
      }
      
      postConstructHierarchy = initaliseMethods((superInfo != null) ? superInfo.postConstructHierarchy : null, postConstruct);
      postActivateHierarchy = initaliseMethods((superInfo != null) ? superInfo.postActivateHierarchy : null, postActivate);
      aroundInvokeHierarchy = initaliseMethods((superInfo != null) ? superInfo.aroundInvokeHierarchy : null, aroundInvoke);
      prePassivateHierarchy = initaliseMethods((superInfo != null) ? superInfo.prePassivateHierarchy : null, prePassivate);
      preDestroyHierarchy = initaliseMethods((superInfo != null) ? superInfo.preDestroyHierarchy : null, preDestroy);
      
      haveCalculatedHierarchy = true;
   }
   
   private Method[] initaliseMethods(Method[] superMethods, Method myMethod)
   {
      if (superMethods == null && myMethod == null)
      {
         return null;
      }
      ArrayList hierarchy = new ArrayList();
      if (superMethods != null)
      {
         //We only want to add superclass interceptor/lifecycle methods if we do not override them
         for (int i = 0 ; i < superMethods.length ; ++i)
         {
            if (!haveMethod(superMethods[i]))
            {
               hierarchy.add(superMethods[i]);
            }
         }
      }
      
      if (myMethod != null)
      {
         hierarchy.add(myMethod);
      }
      
      return (Method[])hierarchy.toArray(new Method[hierarchy.size()]);
   }
   
   private boolean haveMethod(Method method)
   {
      try
      {
         clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
         return true;
      }
      catch (NoSuchMethodException e)
      {
         return false;
      }
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof InterceptorInfo)
      {
         return clazz.equals(((InterceptorInfo)obj).getClazz());
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return clazz.getName().hashCode();
   }
   
   
}
