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

import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 67628 $
 */
public class EJB3Util
{
   public static boolean isStateless(AnnotationsAttribute group)
   {
      return group.getAnnotation(javax.ejb.Stateless.class.getName()) != null;
   }

   public static boolean isStatefulSession(AnnotationsAttribute group)
   {
      return group.getAnnotation(javax.ejb.Stateful.class.getName()) != null;
   }

   public static boolean isEntity(AnnotationsAttribute group)
   {
      return group.getAnnotation(javax.persistence.Entity.class.getName()) != null;
   }

   public static boolean isMessageDriven(AnnotationsAttribute group)
   {
      return group.getAnnotation(javax.ejb.MessageDriven.class.getName()) != null;
   }

   public static boolean isService(AnnotationsAttribute group)
   {
      return group.getAnnotation(org.jboss.ejb3.annotation.Service.class.getName()) != null;
   }

   public static boolean isConsumer(AnnotationsAttribute group)
   {
      return group.getAnnotation(org.jboss.ejb3.annotation.Consumer.class.getName()) != null;
   }

   public static String getAspectDomain(AnnotationsAttribute visible, String defaultContainerName)
   {
      if (visible != null )
      {
         Annotation dinfo = visible.getAnnotation(org.jboss.ejb3.annotation.AspectDomain.class
                                      .getName());
         if (dinfo != null)
         {
            StringMemberValue dmv = (StringMemberValue) dinfo
                    .getMemberValue("value");
            if (dmv != null)
            {
               return dmv.getValue();
            }
         }
      }
      
      return defaultContainerName;
   }
}


