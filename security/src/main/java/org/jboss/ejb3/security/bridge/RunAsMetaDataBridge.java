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
package org.jboss.ejb3.security.bridge;

import java.lang.annotation.Annotation;

import javax.annotation.security.RunAs;

import org.jboss.ejb3.annotation.impl.RunAsImpl;
import org.jboss.ejb3.metadata.MetaDataBridge;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.spec.SecurityIdentityMetaData;
import org.jboss.metadata.javaee.spec.RunAsMetaData;
import org.jboss.metadata.spi.signature.DeclaredMethodSignature;

/**
 * Convert 
 *   <security-identity>
 *      <run-as>
 *         <role-name>...</role-name>
 *      </run-as>
 *   </security-identity>
 * to an annotation.
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RunAsMetaDataBridge implements MetaDataBridge<JBossEnterpriseBeanMetaData>
{
   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData metaData, ClassLoader classLoader)
   {
      if(annotationClass != RunAs.class)
         return null;
      
      if(metaData == null)
         return null;
      
      SecurityIdentityMetaData securityIdentity = metaData.getSecurityIdentity();
      if(securityIdentity == null)
         return null;
      
      RunAsMetaData runAs = securityIdentity.getRunAs();
      if(runAs == null)
         return null;
      
      // role-name is mandated by the xsd
      return annotationClass.cast(new RunAsImpl(runAs.getRoleName()));
   }

   public <A extends Annotation> A retrieveAnnotation(Class<A> annotationClass, JBossEnterpriseBeanMetaData metaData, ClassLoader classLoader, DeclaredMethodSignature method)
   {
      // A RunAs can only appear on a class
      return null;
   }
}
