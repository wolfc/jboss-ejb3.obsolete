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
package org.jboss.ejb;


/**
 * // *
 *
 * @author <a href="mailto:bill@jboss.org">William DeCoste</a>
 * @version $Revision$
 */
public class ResourceImpl implements javax.annotation.Resource
{
   private boolean shareable;
   private AuthenticationType authenticationType;
   private String name;
   private Class type = Object.class;
   private String description;
   private String mappedName;

   public ResourceImpl()
   {
   }

   public String mappedName() { return mappedName; }



   public String description()
   {
      return this.description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public AuthenticationType authenticationType()
   {
      return authenticationType;
   }

   public void setAuthenticationType(AuthenticationType authorizationType)
   {
      this.authenticationType = authorizationType;
   }

   public Class type()
   {
      return type;
   }

   public void setType(Class type)
   {
      this.type = type;
   }

   public String name()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public boolean shareable()
   {
      return shareable;
   }

   public void setShareable(boolean shareable)
   {
      this.shareable = shareable;
   }

   public Class annotationType()
   {
      return javax.annotation.Resource.class;
   }
}
