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
package org.jboss.ejb3.test.interceptors.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.interceptor.Interceptors;

import org.jboss.ejb3.interceptors.ManagedObject;

/**
 * Keep track of the interceptor chain invoked.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@ManagedObject
@Interceptors({InterceptorA.class, InterceptorB.class})
public class InterceptorChainBean
{
   private List<String> postConstructs = new ArrayList<String>();
   
   protected boolean addPostConstruct(String name)
   {
      return postConstructs.add(name);
   }
   
   /**
    * Add the called instance to the chain and return
    * the post constructs invoked.
    * 
    * @param chain
    * @return a list of post constructed instances
    */
   public List<String> createInterceptorChain(List<String> chain)
   {
      chain.add("BEAN");
      return Collections.unmodifiableList(postConstructs);
   }
   
   @PostConstruct
   public void postConstruct()
   {
      addPostConstruct("BEAN");
   }
}
