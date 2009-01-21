/*
* JBoss, Home of Professional Open Source
* Copyright 2005, Red Hat Middleware LLC., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import java.util.Properties;
import java.net.URL;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 55144 $
 */
public class DefaultPersistenceProperties
{
   private Properties properties;

   public DefaultPersistenceProperties() throws Exception
   {
      URL propsUrl = this.getClass().getClassLoader().getResource("META-INF/persistence.properties");
      properties = new Properties();
      properties.load(propsUrl.openStream());
      /* Current hack to establish the hibernate bytecode provider from the
      externalized persistence.properties
      */
      String bcprovider = properties.getProperty("hibernate.bytecode.provider", "javassist");
      System.setProperty("hibernate.bytecode.provider", bcprovider);

   }

   public Properties getProperties()
   {
      return properties;
   }

}
