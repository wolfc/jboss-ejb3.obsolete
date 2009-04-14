/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.embedded.test.datasource;

import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DataSourceDefinitionMetaData implements DataSourceDefinition
{
   public Class<? extends Annotation> annotationType()
   {
      throw new RuntimeException("NYI");
   }
   
   public String className()
   {
      throw new RuntimeException("NYI");
   }

   public String databaseName()
   {
      throw new RuntimeException("NYI");
   }

   public String description()
   {
      throw new RuntimeException("NYI");
   }

   public int initialPoolSize()
   {
      throw new RuntimeException("NYI");
   }

   public int isolationLevel()
   {
      throw new RuntimeException("NYI");
   }

   public int loginTimeout()
   {
      throw new RuntimeException("NYI");
   }

   public int maxIdleTime()
   {
      throw new RuntimeException("NYI");
   }

   public int maxPoolSize()
   {
      throw new RuntimeException("NYI");
   }

   public int maxStatements()
   {
      throw new RuntimeException("NYI");
   }

   public int minPoolSize()
   {
      throw new RuntimeException("NYI");
   }

   public String name()
   {
      throw new RuntimeException("NYI");
   }

   public String password()
   {
      throw new RuntimeException("NYI");
   }

   public int portNumber()
   {
      throw new RuntimeException("NYI");
   }

   public String[] properties()
   {
      throw new RuntimeException("NYI");
   }

   public String serverName()
   {
      throw new RuntimeException("NYI");
   }

   public boolean transactional()
   {
      throw new RuntimeException("NYI");
   }

   public String url()
   {
      throw new RuntimeException("NYI");
   }

   public String user()
   {
      throw new RuntimeException("NYI");
   }
}
