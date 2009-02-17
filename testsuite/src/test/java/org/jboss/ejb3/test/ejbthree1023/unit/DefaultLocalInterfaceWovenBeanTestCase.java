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
package org.jboss.ejb3.test.ejbthree1023.unit;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.ejb3.test.ejbthree1023.FacadeRemote;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class DefaultLocalInterfaceWovenBeanTestCase extends JBossTestCase
{

   public DefaultLocalInterfaceWovenBeanTestCase(String name)
   {
      super(name);
   }

   public void testAnnotatedLocalInterfaceWovenBean() throws Exception
   {
      //Just a sanity test
      InitialContext ctx = getInitialContext();
      FacadeRemote facade = (FacadeRemote)ctx.lookup("FacadeBean/remote");
      try
      {
         facade.callWovenBeanWithAnnotatedLocal();
      }
      catch(RuntimeException e)
      {
         fail(e.getMessage());
      }
   }
   
   public void testDefaultLocalInterfaceWovenBean() throws Exception
   {
      //The real test for this issue
      InitialContext ctx = getInitialContext();
      FacadeRemote facade = (FacadeRemote)ctx.lookup("FacadeBean/remote");
      try
      {
         facade.callWovenBeanWithDefaultLocal();
      }
      catch(RuntimeException e)
      {
         fail(e.getMessage());
      }
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(DefaultLocalInterfaceWovenBeanTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "ejbthree1023.jar");
      return setup; 
   }

   static class AOPTestSetup extends JBossTestSetup
   {
      public static String ASPECT_MANAGER_NAME = "jboss.aop:service=AspectManager";

      private String jar;
      private String originalInclude;

      // Create an initializer for the test suite
      AOPTestSetup(TestSuite suite, String jar) throws Exception
      {
         super(suite);
         this.jar = jar;
      }

      protected void setUp() throws Exception
      {
         super.setUp();
         ObjectName aspectManager = new ObjectName(ASPECT_MANAGER_NAME);
        
         originalInclude = (String)getServer().getAttribute(aspectManager, "Include");
         Attribute include = new Attribute("Include", originalInclude + ", org.jboss.ejb3.test.");
         getServer().setAttribute(aspectManager, include);
         
         Attribute enableTransformer = new Attribute("EnableTransformer", Boolean.TRUE);
         getServer().setAttribute(aspectManager, enableTransformer);
         try
         {
            redeploy(jar);
         }
         catch(Exception e)
         {
            // Reset the EnableTransformer to false
            try
            {
               enableTransformer = new Attribute("EnableTransformer", Boolean.FALSE);
               getServer().setAttribute(aspectManager, enableTransformer);
            }
            catch(Exception ex)
            {
               getLog().error("Failed to set EnableTransformer to false", ex);
            }
            throw e;
         }
      }
      protected void tearDown() throws Exception
      {
         Exception undeployException = null;
         try
         {
            undeploy(jar);
         }
         catch(Exception e)
         {
            undeployException = e;
         }
         ObjectName aspectManager = new ObjectName(ASPECT_MANAGER_NAME);
         
         Attribute include = new Attribute("Include", originalInclude);
         getServer().setAttribute(aspectManager, include);
         
         
         Attribute enableTransformer = new Attribute("EnableTransformer", Boolean.FALSE);
         getServer().setAttribute(aspectManager, enableTransformer);
         if( undeployException != null )
            throw undeployException;
         super.tearDown();
      }
   }
}
