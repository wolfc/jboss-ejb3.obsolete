/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.test.ejbthree724.unit;

import junit.framework.Test;

import org.jboss.ejb3.test.ejbthree724.MyStateless;
import org.jboss.ejb3.test.ejbthree724.Person;
import org.jboss.test.JBossTestCase;

/**
 * Jar files should be found relative of the root of the persistence unit. (EJB3 Persistence 6.2.1.6)
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class RelativeJarFileUnitTestCase extends JBossTestCase
{

   public RelativeJarFileUnitTestCase(String name)
   {
      super(name);
   }

   public void testRelativeJarFileBeanAccess() throws Exception
   {
      Person p = new Person("Heiko");
      
      MyStateless bean = (MyStateless) getInitialContext().lookup("ejbthree724/MyStatelessBean/remote");
      
      bean.save(p);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(RelativeJarFileUnitTestCase.class, "ejbthree724.ear");
   }
}
