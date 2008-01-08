/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.interceptors.container;

import java.lang.reflect.Method;

import org.jboss.aop.AspectManager;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.Domain;
import org.jboss.aop.DomainDefinition;
import org.jboss.aop.MethodInfo;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.util.MethodHashing;
import org.jboss.ejb3.interceptors.lang.ClassHelper;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractContainer<T, C extends AbstractContainer<T, C>>
{
   private static final Logger log = Logger.getLogger(AbstractContainer.class);
   
   private ManagedObjectAdvisor<T, C> advisor;
   
   public AbstractContainer(String name, Domain domain, Class<? extends T> beanClass)
   {
      assert name != null : "name is null";
      assert domain != null : "domain is null";
      assert beanClass != null : "beanClass is null";
      
      this.advisor = new ManagedObjectAdvisor<T, C>((C) this, name, domain, beanClass);
   }
   
   public AbstractContainer(String name, String domainName, Class<? extends T> beanClass)
   {
      this(name, getDomain(domainName), beanClass);
   }
   
   protected final ClassAdvisor getAdvisor()
   {
      return advisor;
   }
   
   /*
    * TODO: this should not be here, it's an AspectManager helper function.
    */
   private static final Domain getDomain(String domainName)
   {
      DomainDefinition domainDefinition = AspectManager.instance().getContainer(domainName);
      if(domainDefinition == null)
         throw new IllegalArgumentException("Domain definition '" + domainName + "' can not be found");
      
      final Domain domain = (Domain) domainDefinition.getManager();
      return domain;
   }
   
   /**
    * Call a method upon a target object with all interceptors in place.
    * 
    * @param target     the target to invoke upon
    * @param method     the method to invoke
    * @param arguments  arguments to the method
    * @return           return value of the method
    * @throws Throwable if anything goes wrong
    */
   public Object invoke(Object target, Method method, Object arguments[]) throws Throwable
   {
      long methodHash = MethodHashing.calculateHash(method);
      MethodInfo info = advisor.getMethodInfo(methodHash);
      if(info == null)
         throw new IllegalArgumentException("method " + method + " is not under advisement by " + this);
      MethodInvocation invocation = new MethodInvocation(info, info.getInterceptors());
      invocation.setArguments(arguments);
      invocation.setTargetObject(target);
      return invocation.invokeNext();
   }
   
   /**
    * A convenient, but unchecked and slow method to call a method upon a target.
    * 
    * (Slow method)
    * 
    * @param <R>        the return type
    * @param target     the target to invoke upon
    * @param methodName the method name to invoke
    * @param args       the arguments to the method
    * @return           the return value
    * @throws Throwable if anything goes wrong
    */
   @SuppressWarnings("unchecked")
   public <R> R invoke(Object target, String methodName, Object ... args) throws Throwable
   {
      Method method = ClassHelper.getMethod(target.getClass(), methodName);
      return (R) invoke(target, method, args);
   }
}
