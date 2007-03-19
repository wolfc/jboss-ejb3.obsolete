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
package org.jboss.ejb3.test.dd.unit;

import java.net.URL;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.ejb3.metamodel.ApplicationClientDD;
import org.jboss.ejb3.metamodel.ApplicationClientDDObjectFactory;
import org.jboss.ejb3.metamodel.LifecycleCallback;
import org.jboss.metamodel.descriptor.EnvEntry;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ApplicationClientXmlTestCase extends TestCase
{
   public void testUnmarshalDDXsd() throws Exception
   {
      URL xmlUrl = getResourceUrl("dd/application-client-test1.xml");
      assertNotNull(xmlUrl);
      ApplicationClientDD dd = ApplicationClientDDObjectFactory.parse(xmlUrl);
      assertNotNull(dd);
      
      assertEquals("Test1", dd.getDisplayName());
      //assertEquals("application client dd test", dd.getDescription());
      
      assertEquals(1, dd.getEnvEntries().size());
      
      {
         Iterator<EnvEntry> i = dd.getEnvEntries().iterator();
         
         EnvEntry ee = i.next();
         assertEquals("envTest", ee.getEnvEntryName());
         assertEquals("java.lang.String", ee.getEnvEntryType());
         assertEquals("Hello world", ee.getEnvEntryValue());
      }
      
      assertEquals(1, dd.getPostConstructs().size());
      
      {
         Iterator<LifecycleCallback> i = dd.getPostConstructs().iterator();
         LifecycleCallback lc = i.next();
         assertNull(lc.getLifecycleCallbackClass());
         assertEquals("postConstruct", lc.getLifecycleCallbackMethod());
      }
      
      assertEquals(1, dd.getPreDestroys().size());
      
      {
         Iterator<LifecycleCallback> i = dd.getPreDestroys().iterator();
         LifecycleCallback lc = i.next();
         assertNull(lc.getLifecycleCallbackClass());
         assertEquals("preDestroy", lc.getLifecycleCallbackMethod());
      }
   }
   
   private static URL getResourceUrl(String name)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(name);
      if (url == null)
      {
         throw new IllegalStateException("Resource not found: " + name);
      }
      return url;
   }

   public static Test suite() throws Exception
   {
      return new TestSuite(ApplicationClientXmlTestCase.class);
   }
   
}
