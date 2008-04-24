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
package org.jboss.injection.test.common;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.test.AbstractTestDelegate;

/**
 * Make sure the proper aop.xml is deployed before running the test.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class AOPTestDelegate extends AbstractTestDelegate
{
   /** The deployed urls */
   private static final List<URL> urls = new CopyOnWriteArrayList<URL>();

   /**
    * @param clazz
    */
   public AOPTestDelegate(Class<?> clazz)
   {
      super(clazz);
   }

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      
      String aopXmlName = clazz.getName().replace(".", "/") + "-aop.xml";
      URL url = clazz.getClassLoader().getResource(aopXmlName);
      if(url == null)
         throw new IllegalStateException("Can't find resource " + aopXmlName);
      AspectXmlLoader.deployXML(url);
      urls.add(url);
   }
   
   @Override
   public void tearDown() throws Exception
   {
      for(URL url : urls)
         AspectXmlLoader.undeployXML(url);
      super.tearDown();
   }
}
