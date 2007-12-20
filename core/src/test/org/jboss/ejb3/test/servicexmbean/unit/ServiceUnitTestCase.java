/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.test.servicexmbean.unit;

import java.util.ArrayList;
import java.util.Iterator;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.ejb3.test.service.ServiceOneRemote;
import org.jboss.ejb3.test.service.ServiceSevenRemote;
import org.jboss.ejb3.test.service.ServiceSixRemote;
import org.jboss.ejb3.test.service.ServiceTwoRemote;
import org.jboss.ejb3.test.service.SessionRemote;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: ServiceUnitTestCase.java 57005 2006-09-20 13:10:12Z wolfc $
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
   
   public void testXMBean() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName service = new ObjectName("jboss.ejb3.test:service=ServiceOne");
      server.setAttribute(service, new Attribute("StringAttribute", "test value"));
      String stringAttribute = (String)server.getAttribute(service, "StringAttribute");
      assertEquals("test value", stringAttribute);
      server.invoke(service, "testOperation", new Object[0], new String[0]);
      stringAttribute = (String)server.getAttribute(service, "StringAttribute");
      assertEquals("reset", stringAttribute);
   }
   
   public void testXMBeanDeploymentDescriptor() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName service = new ObjectName("jboss.ejb3.test:service=ServiceOneDeploymentDescriptor");
      server.setAttribute(service, new Attribute("StringAttribute", "test value"));
      String stringAttribute = (String)server.getAttribute(service, "StringAttribute");
      assertEquals("test value", stringAttribute);
      server.invoke(service, "testOperation", new Object[0], new String[0]);
      stringAttribute = (String)server.getAttribute(service, "StringAttribute");
      assertEquals("reset", stringAttribute);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceUnitTestCase.class, "service-xmbean-test.jar");
   }

}
