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
package org.jboss.ejb3.nointerface.test.deployers;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.jboss.ejb3.DeploymentUnit;
import org.jboss.ejb3.interceptor.InterceptorInfoRepository;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * EJB3DeploymentUnit
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class EJB3DeploymentUnit implements DeploymentUnit
{

   private org.jboss.deployers.structure.spi.DeploymentUnit unit;

   public EJB3DeploymentUnit(org.jboss.deployers.structure.spi.DeploymentUnit unit)
   {
      this.unit = unit;
   }

   public Object addAttachment(String name, Object attachment)
   {
      return unit.addAttachment(name, attachment);
   }

   public Object getAttachment(String name)
   {
      return unit.getAttachment(name);
   }

   public ClassLoader getClassLoader()
   {
      return unit.getClassLoader();
   }

   public List<Class> getClasses()
   {
      throw new RuntimeException("Not yet implemented");
   }

   public String getDefaultEntityManagerName()
   {
      throw new RuntimeException("Not yet implemented");
   }

   public Map getDefaultPersistenceProperties()
   {
      throw new RuntimeException("Not yet implemented");
   }

   public URL getEjbJarXml()
   {
      // TODO Is there a better way to do this? Maybe traverse through the deployment unit
      return unit.getClassLoader().getResource("ejb-jar.xml");

   }

   public InterceptorInfoRepository getInterceptorInfoRepository()
   {
      throw new RuntimeException("Not yet implemented");
   }

   public URL getJbossXml()
   {
      // TODO Is there a better way to do this? Maybe traverse through the deployment unit
      return unit.getClassLoader().getResource("jboss.xml");
   }

   public Hashtable getJndiProperties()
   {
      return null;
   }

   public VirtualFile getMetaDataFile(String string)
   {
      throw new RuntimeException("Not yet implemented");
   }

   public URL getPersistenceXml()
   {
      // TODO Is there a better way to do this? Maybe traverse through the deployment unit
      return unit.getClassLoader().getResource("persistence.xml");
   }

   public String getRelativePath()
   {
      return unit.getRelativePath();
   }

   public URL getRelativeURL(String path)
   {
      throw new RuntimeException("Not yet implemented");
   }

   public ClassLoader getResourceLoader()
   {
      throw new RuntimeException("Not yet implemented");
   }

   public List<VirtualFile> getResources(VirtualFileFilter filter)
   {
      throw new RuntimeException("Not yet implemented");
   }

   public VirtualFile getRootFile()
   {
      throw new RuntimeException("Not yet implemented");
   }

   public String getShortName()
   {
      return unit.getSimpleName();
   }

   public URL getUrl()
   {
      throw new RuntimeException("Not yet implemented");
   }

   public Object removeAttachment(String name)
   {
      return unit.removeAttachment(name);
   }

}
