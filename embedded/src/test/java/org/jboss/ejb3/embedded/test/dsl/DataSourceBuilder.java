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
package org.jboss.ejb3.embedded.test.dsl;

import org.jboss.metadata.rar.jboss.mcf.LocalDataSourceDeploymentMetaData;
import org.jboss.metadata.rar.jboss.mcf.NonXADataSourceDeploymentMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DataSourceBuilder
{
   private NonXADataSourceDeploymentMetaData metaData;
   
   public static DataSourceBuilder localDataSource()
   {
      return new DataSourceBuilder(new LocalDataSourceDeploymentMetaData());
   }
   
   public static NonXADataSourceDeploymentMetaData nonXADataSource(DataSourceBuilder builder)
   {
      return builder.getMetaData();
   }
   
   protected DataSourceBuilder(NonXADataSourceDeploymentMetaData metaData)
   {
      this.metaData = metaData;
   }
   
   public DataSourceBuilder connectionURL(String spec)
   {
      metaData.setConnectionUrl(spec);
      return this;
   }
   
   public DataSourceBuilder driverClass(String driverClass)
   {
      metaData.setDriverClass(driverClass);
      return this;
   }
   
   public NonXADataSourceDeploymentMetaData getMetaData()
   {
      return metaData;
   }
   
   public DataSourceBuilder jndiName(String jndiName)
   {
      metaData.setJndiName(jndiName);
      return this;
   }
   
   public DataSourceBuilder password(String password)
   {
      metaData.setPassWord(password);
      return this;
   }
   
   public DataSourceBuilder user(String user)
   {
      metaData.setUserName(user);
      return this;
   }
}
