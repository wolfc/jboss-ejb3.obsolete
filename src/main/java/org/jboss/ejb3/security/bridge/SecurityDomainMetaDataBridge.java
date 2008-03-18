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
package org.jboss.ejb3.security.bridge;

import java.lang.annotation.Annotation;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ejb3.annotation.impl.SecurityDomainImpl;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;

/**
 * Meta data bridge to return the SecurityDomain Annotation 
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Anil.Saldhana@jboss.com
 * @version $Revision: 68904 $
 */
public class SecurityDomainMetaDataBridge implements MetaDataBridge<JBossEnterpriseBeanMetaData>
{
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader)
   {
      if (annotationClass == SecurityDomain.class)
      {
         String securityDomain = beanMetaData.getSecurityDomain();
         //TODO: How to get the merged meta data? Is the following line correct? 
         if(securityDomain == null)
        	 securityDomain = beanMetaData.getJBossMetaData().getSecurityDomain();
         if (securityDomain != null)
            return annotationClass.cast(new SecurityDomainImpl(securityDomain)); 
      }
      return null;
   }

   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData beanMetaData, ClassLoader classLoader, String methodName, String... parameterNames)
   {
      return null;
   }
}
