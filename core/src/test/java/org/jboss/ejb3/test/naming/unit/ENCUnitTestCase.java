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
package org.jboss.ejb3.test.naming.unit;

import org.jboss.ejb3.test.mdb.unit.MDBUnitTestCase;
import org.jboss.ejb3.test.naming.TestENC;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/**
 * Tests of the secure access to EJBs.
 *
 * @author   Scott.Stark@jboss.org
 * @author   <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision$
 */
public class ENCUnitTestCase extends JBossTestCase
{   
   /**
    * Constructor for the ENCUnitTestCase object
    *
    * @param name  Testcase name
    */
   public ENCUnitTestCase(String name)
   {
      super(name);
   }

   /** Tests of accessing the various types of java:comp entries
    *
    * @exception Exception  Description of Exception
    */
   public void testENC() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("jduke", "theduke");
      client.login();
      
      TestENC bean = (TestENC)getInitialContext().lookup("ENCBean");
      getLog().debug("Created ENCBean");
      bean.accessENC();
      bean.remove();
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testENC2() throws Exception
   {
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple("jduke", "theduke");
      client.login();
      
      TestENC bean = (TestENC)getInitialContext().lookup("ENCBean0");
      getLog().debug("Created ENCBean0");
      bean.accessENC();
      bean.remove();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ENCUnitTestCase.class, "naming.jar");
   }

}
