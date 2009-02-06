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
package org.jboss.ejb3.test.service.unit;

import java.util.ArrayList;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.ejb3.test.service.ServiceOneRemote;
import org.jboss.ejb3.test.service.ServiceSevenRemote;
import org.jboss.ejb3.test.service.ServiceSixRemote;
import org.jboss.ejb3.test.service.ServiceTwoRemote;
import org.jboss.ejb3.test.service.SessionRemote;
import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id$
 */

public class ServiceUnitTestCase
extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(ServiceUnitTestCase.class);

   static boolean deployed = false;
   static int test = 0;

   public ServiceUnitTestCase(String name)
   {

      super(name);

   }
   
   public void testEjbJar() throws Exception
   {
      SessionRemote session = (SessionRemote)getInitialContext().lookup("SessionBean/remote");
      assertNotNull(session);
   }
   
   public void testEjbInjection() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
      
      ServiceOneRemote test = (ServiceOneRemote) getInitialContext().lookup("ServiceOne/remote");
      test.testEjbInjection();
   }
   
   public void testSecurityDomain() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("invalid", "invalid");
      client.login();
      
      try
      {
         ServiceOneRemote test = (ServiceOneRemote) getInitialContext().lookup("ServiceOne/remote");
         test.testEjbInjection();
         fail("Should have thrown EJBAccessException");
      }
      catch (javax.ejb.EJBAccessException e)
      {
         
      }
      finally
      {
         client.logout();
      }
   }

   public void testServiceWithDefaultRemoteJNDIName() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
      
      ServiceOneRemote test = (ServiceOneRemote) getInitialContext().lookup("ServiceOne/remote");
      test.setRemoteMethodCalls(0);
      final int count = 15;
      Thread[] threads = new Thread[count];
      for (int i = 0 ; i < count ; i++)
      {
         final int outer = i;
         threads[i] = new Thread(
               new Runnable()
               {
                  public void run()
                  {
                     try
                     {
                        SecurityClient client = SecurityClientFactory.getSecurityClient();
                        client.setSimple("somebody", "password");
                        client.login();
                        ServiceOneRemote test = (ServiceOneRemote) getInitialContext().lookup("ServiceOne/remote");
                        for (int j = 0 ; j < count ; j++)
                        {
                           String s = outer + "_" + j;
                           //System.out.println(s);
                           test.remoteMethod(s);
                        }
                     }
                     catch(Exception e)
                     {
                        throw new RuntimeException(e);
                     }
                  }
               }
            );
         threads[i].start();
      }

      Thread.sleep(5000);
      for (int i = 0 ; i < count ; i++)
      {
         threads[i].join();
      }
      assertEquals("There should only ever be one instance of the service", 1, test.getInstances());
      assertEquals("Wrong number of remote method calls", count * count, test.getRemoteMethodCalls());
   }
   
   /**
    * Was injection successful after start?
    */
   public void testEJB3_587() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3.bugs:service=TestResourceInjectionService");
      boolean success = (Boolean)server.getAttribute(testerName, "TestedSuccessful");
      assertTrue(success);
   }

   /**
    * Is injection successful when getting a management attribute?
    */
   public void testEJB3_587_2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3.bugs:service=TestResourceInjectionService");
      boolean success = (Boolean)server.getAttribute(testerName, "TestedSuccessfulNow");
      assertTrue(success);
   }

   public void testServiceWithDefaultLocalJNDIName() throws Exception
   {
	  MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=service");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testServiceWithDefaultLocalJNDIName", params, sig);
   }

   private class RemoteTest extends Thread
   {
      private int count;
      private int outer;

      public Exception exception;

      public RemoteTest(int outer, int count)
      {
         this.outer = outer;
         this.count = count;
      }

      public void run()
      {
         try
         {
            ServiceSevenRemote test = (ServiceSevenRemote) getInitialContext().lookup("ServiceSeven/remote");
            for (int j = 0 ; j < count ; j++)
            {
               String s = outer + "_" + j;
               //System.out.println(s);
               test.remoteMethod(s);
            }
         }
         catch(Exception e)
         {
            exception = e;
         }
      }
   }
   
   public void testRemoteServiceWithInterfaceAnnotations() throws Exception
   {
      ServiceSevenRemote test = (ServiceSevenRemote) getInitialContext().lookup("ServiceSeven/remote");
      test.setRemoteMethodCalls(0);
      final int count = 15;
      RemoteTest[] threads = new RemoteTest[count];
      for (int i = 0 ; i < count ; i++)
      {
         final int outer = i;
         threads[i] = new RemoteTest(outer, count);
         threads[i].start();
      }

      Thread.sleep(5000);
      for (int i = 0 ; i < count ; i++)
      {
         threads[i].join();
         if (threads[i].exception != null) throw threads[i].exception;
      }
      assertEquals("There should only ever be one instance of the service", 1, test.getInstances());
      assertEquals("Wrong number of remote method calls", count * count, test.getRemoteMethodCalls());
   }

   public void testLocalServiceWithInterfaceAnnotation() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=service");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testLocalServiceWithInterfaceAnnotation", params, sig);
   }
   
   public void testServiceWithRemoteBinding() throws Exception
   {
      ServiceTwoRemote test = (ServiceTwoRemote) getInitialContext().lookup("serviceTwo/remote");
      test.setCalled(false);
      assertFalse("Called should be false", test.getCalled());
      test.remoteMethod();
      assertTrue("Called should be true", test.getCalled());
   }

   public void testServiceWithLocalBinding() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=service");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testServiceWithLocalBinding", params, sig);
   }
   
   public void testDeploymentDescriptorServiceInjection() throws Exception
   {
      ServiceSixRemote test = (ServiceSixRemote) getInitialContext().lookup("serviceSix/remote");
      assertNotNull(test);
      test.testInjection();
   }

   public void testDeploymentDescriptorServiceWithRemoteBinding() throws Exception
   {
      ServiceSixRemote test = (ServiceSixRemote) getInitialContext().lookup("serviceSix/remote");
      test.setCalled(false);
      assertFalse("Called should be false", test.getCalled());
      test.remoteMethod();
      assertTrue("Called should be true", test.getCalled());
   }

   public void testDeploymentDescriptorServiceWithLocalBinding() throws Exception
   {
	  MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=service");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testDeploymentDescriptorServiceWithLocalBinding", params, sig);
   }
   
   public void testDeploymentDescriptorManagementServiceWithDefaultName() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:service=EJB3,jar=service-test.jar,name=ServiceSix,type=ManagementInterface");
      server.setAttribute(testerName, new Attribute("Attribute", new Integer(1234)));
      int ret = ((Integer)server.getAttribute(testerName, "Attribute"));
      assertEquals("wrong value for Attribute", 1234, ret);

      String s = (String)server.invoke(testerName, "jmxOperation", new Object[]{"1"}, new String[]{String.class.getName()});
      assertEquals("wrong return value", "x1x", s);

      String[] sa = (String[])server.invoke(testerName, "jmxOperation", new Object[]{new String[]{"0", "1"}}, new String[]{String[].class.getName()});
      assertEquals("wrong number of return values", 2, sa.length);
      for (int i = 0 ; i < sa.length ; i++)
      {
         assertEquals("Wrong return value index " + i, "x" + i + "x", sa[i]);
      }

      AttributeList attributes = new AttributeList();
      attributes.add(new Attribute("SomeAttr", new Integer(72)));
      attributes.add(new Attribute("OtherAttr", new Integer(99)));
      attributes = server.setAttributes(testerName, attributes);

      String[] attrs = new String[]{"Attribute", "SomeAttr", "OtherAttr"};
      AttributeList list = server.getAttributes(testerName, attrs);

      assertEquals("Wrong number of attributes returned", 3, list.size());
      for (Iterator it = list.iterator() ; it.hasNext() ; )
      {
         Attribute attr = (Attribute)it.next();
         String name = attr.getName();
         if (name.equals("Attribute"))
         {
            assertEquals("Wrong number for Attribute", 1234, attr.getValue());
         }
         else if (name.equals("SomeAttr"))
         {
            assertEquals("Wrong number for SomeAttr", 72, attr.getValue());
         }
         else if (name.equals("OtherAttr"))
         {
            assertEquals("Wrong number for OtherAttr", 99, attr.getValue());
         }
         else
         {
            throw new RuntimeException("Unknown attribute returned: " + name);
         }
      }

      server.setAttribute(testerName, new Attribute("WriteOnly", new Integer(2525)));
      assertEquals("Wrong read only value", 2525, ((Integer)server.getAttribute(testerName, "ReadOnly")).intValue());
   }

   public void testManagementServiceWithDefaultName() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("somebody", "password");
      client.login();
      
	  MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:service=EJB3,jar=service-test.jar,name=ServiceOne,type=ManagementInterface");
      server.setAttribute(testerName, new Attribute("Attribute", new Integer(1234)));
      int ret = ((Integer)server.getAttribute(testerName, "Attribute"));
      assertEquals("wrong value for Attribute", 1234, ret);

      String s = (String)server.invoke(testerName, "jmxOperation", new Object[]{"1"}, new String[]{String.class.getName()});
      assertEquals("wrong return value", "x1x", s);

      String[] sa = (String[])server.invoke(testerName, "jmxOperation", new Object[]{new String[]{"0", "1"}}, new String[]{String[].class.getName()});
      assertEquals("wrong number of return values", 2, sa.length);
      for (int i = 0 ; i < sa.length ; i++)
      {
         assertEquals("Wrong return value index " + i, "x" + i + "x", sa[i]);
      }

      AttributeList attributes = new AttributeList();
      attributes.add(new Attribute("SomeAttr", new Integer(72)));
      attributes.add(new Attribute("OtherAttr", new Integer(99)));
      attributes = server.setAttributes(testerName, attributes);

      String[] attrs = new String[]{"Attribute", "SomeAttr", "OtherAttr"};
      AttributeList list = server.getAttributes(testerName, attrs);

      assertEquals("Wrong number of attributes returned", 3, list.size());
      for (Iterator it = list.iterator() ; it.hasNext() ; )
      {
         Attribute attr = (Attribute)it.next();
         String name = attr.getName();
         if (name.equals("Attribute"))
         {
            assertEquals("Wrong number for Attribute", 1234, attr.getValue());
         }
         else if (name.equals("SomeAttr"))
         {
            assertEquals("Wrong number for SomeAttr", 72, attr.getValue());
         }
         else if (name.equals("OtherAttr"))
         {
            assertEquals("Wrong number for OtherAttr", 99, attr.getValue());
         }
         else
         {
            throw new RuntimeException("Unknown attribute returned: " + name);
         }
      }

      server.setAttribute(testerName, new Attribute("WriteOnly", new Integer(2525)));
      assertEquals("Wrong read only value", 2525, ((Integer)server.getAttribute(testerName, "ReadOnly")).intValue());
   }
   
   public void testManagementServiceWithInterfaceAnnotations() throws Exception
   {
       MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:service=EJB3,jar=service-test.jar,name=ServiceSeven,type=ManagementInterface");
      server.setAttribute(testerName, new Attribute("Attribute", new Integer(1234)));
      int ret = ((Integer)server.getAttribute(testerName, "Attribute"));
      assertEquals("wrong value for Attribute", 1234, ret);

      String s = (String)server.invoke(testerName, "jmxOperation", new Object[]{"1"}, new String[]{String.class.getName()});
      assertEquals("wrong return value", "x1x", s);

      String[] sa = (String[])server.invoke(testerName, "jmxOperation", new Object[]{new String[]{"0", "1"}}, new String[]{String[].class.getName()});
      assertEquals("wrong number of return values", 2, sa.length);
      for (int i = 0 ; i < sa.length ; i++)
      {
         assertEquals("Wrong return value index " + i, "x" + i + "x", sa[i]);
      }

      AttributeList attributes = new AttributeList();
      attributes.add(new Attribute("SomeAttr", new Integer(72)));
      attributes.add(new Attribute("OtherAttr", new Integer(99)));
      attributes = server.setAttributes(testerName, attributes);

      String[] attrs = new String[]{"Attribute", "SomeAttr", "OtherAttr"};
      AttributeList list = server.getAttributes(testerName, attrs);

      assertEquals("Wrong number of attributes returned", 3, list.size());
      for (Iterator it = list.iterator() ; it.hasNext() ; )
      {
         Attribute attr = (Attribute)it.next();
         String name = attr.getName();
         if (name.equals("Attribute"))
         {
            assertEquals("Wrong number for Attribute", 1234, attr.getValue());
         }
         else if (name.equals("SomeAttr"))
         {
            assertEquals("Wrong number for SomeAttr", 72, attr.getValue());
         }
         else if (name.equals("OtherAttr"))
         {
            assertEquals("Wrong number for OtherAttr", 99, attr.getValue());
         }
         else
         {
            throw new RuntimeException("Unknown attribute returned: " + name);
         }
      }

      server.setAttribute(testerName, new Attribute("WriteOnly", new Integer(2525)));
      assertEquals("Wrong read only value", 2525, ((Integer)server.getAttribute(testerName, "ReadOnly")).intValue());
   }

   public void testServiceDependencyInjectionAndInterception() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:management=interface,with=customName,is=serviceThree");
      Boolean injected = (Boolean)server.getAttribute(testerName, "Injected");
      assertTrue("Injected should have been true", injected);

      Integer intercepted = (Integer)server.getAttribute(testerName, "Intercepted");;
      assertEquals("Wrong number of interceptions", 2, intercepted.intValue());
   }

   public void testCreationOrder() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.ejb3:service=Tester,test=service");
      ArrayList creates = (ArrayList)server.getAttribute(testerName, "Creates");
      ArrayList starts = (ArrayList)server.getAttribute(testerName, "Starts");

      //ServiceTwo does not implement any lifecycle so it won't appear
      int expectedLength = 4;
      assertEquals("Wrong length of created MBeans", expectedLength, creates.size());
      assertEquals("Wrong length of started MBeans", expectedLength, starts.size());

      String createAll = " ";
      String startAll = " ";
      for (int i = 0; i < expectedLength; i++)
      {
         createAll += creates.get(i) + " ";
         startAll += starts.get(i) + " ";
      }

      for (int i = 0 ; i < expectedLength ; i++)
      {
         // EJBTHREE-655: it's no use checking the creation order
//         int create = Integer.parseInt((String)creates.get(i));
         int start = Integer.parseInt((String)starts.get(i));
         int expected = (i == 0) ? i + 1 : i + 2;
//         assertEquals("Creation of Service " + create + " appears at the wrong place " + createAll, expected, create);
         assertEquals("Start of Service " + start + " appears at the wrong place " + startAll, expected, start);
      }
   }

   public void testDependsInjection() throws Exception
   {
	   MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.j2ee:service=EJB3,jar=service-test.jar,name=ServiceFive,type=ManagementInterface");
      assertTrue("Proxy field was not injected", (Boolean)server.getAttribute(testerName, "InjectedProxyField"));
      assertTrue("Proxy method was not injected", (Boolean)server.getAttribute(testerName, "InjectedProxyMethod"));
      assertTrue("ObjectName field was not injected", (Boolean)server.getAttribute(testerName, "InjectedObjectNameField"));
      assertTrue("ObjectName method was not injected", (Boolean)server.getAttribute(testerName, "InjectedObjectNameMethod"));
      SessionRemote test = (SessionRemote) getInitialContext().lookup("Session/remote");
      assertTrue("StatelessBean @Depends was not injected", test.injectedDepends());
   }


   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceUnitTestCase.class, "service-test.sar, service-test.jar");
   }

}
