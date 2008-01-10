/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree971.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree971.CheckEJBContext;
import org.jboss.test.JBossTestCase;

/**
 * Test if an EJBContext can be properly located (EJB3 16.15).
 * Note that we also bind the EJBContext into java:comp/env space
 * if an annotation with a name is used (EJBTHREE-971).
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class CheckEJBContextTestCase extends JBossTestCase
{

   public CheckEJBContextTestCase(String name)
   {
      super(name);
   }

   private CheckEJBContext lookupBean() throws Exception
   {
      return (CheckEJBContext) getInitialContext().lookup("CheckEJBContextBean/remote");
   }
   
   public void testEnvironment() throws Exception
   {
      CheckEJBContext bean = lookupBean();
      bean.checkForEjbContextInEnv();
   }
   
   public void testInjection() throws Exception
   {
      CheckEJBContext bean = lookupBean();
      bean.checkForInjectedEjbContext();
   }
   
   public void testInjectionEnv() throws Exception
   {
      CheckEJBContext bean = lookupBean();
      bean.checkForInjectedEjbCtxInEnv();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(CheckEJBContextTestCase.class, "ejbthree971.jar");
   }
}
