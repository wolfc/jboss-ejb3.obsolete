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
package org.jboss.ejb3.test.schema.unit;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Test;
import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * 
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class SchemaTestCase extends JBossTestCase implements ErrorHandler
{
   private static final Logger log = Logger.getLogger(SchemaTestCase.class);

   private static final String LOCATION_RESOURCES_TEST = "../src/test/resources/test";

   public SchemaTestCase(String name)
   {
      super(name);
   }

   public void testEjbClassOptionalEjbJar() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();

      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jarC.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jarD.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/enventry/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/mail/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/reference21_30/META-INF/ejb-jar3.xml", builder);
   }

   public void testTestEjbJar() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();

      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/bank/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/bmt/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/clusteredsession/islocal/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/clusteredsession/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/mdb/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jarA.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jarB.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jarC.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jarD.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/override/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/web/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/web/websubdir/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/descriptortypo/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree1060/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree1066/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree712/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree957/one/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree957/two/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree959/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree985/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/enventry/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/factoryxml/session1/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/factoryxml/session2/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/interceptors2/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/interceptors3/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/interceptors/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/interceptors2/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/jca/inflow/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/jms/managed/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/libdeployment/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/mail/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/mdb/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/mdbtransactions/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/microbench/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/naming-errors/ejb-jar-method-field.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/naming/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/reference21_30/META-INF/ejb-jar2.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/reference21_30/META-INF/ejb-jar3.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/regression/ejbthree454/a/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/regression/ejbthree454/b/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/regression/ejbthree625/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/seam/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/securitydomain/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/security/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/service/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/stateful/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/txexceptions/META-INF/ejb-jar.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/unauthenticatedprincipal/META-INF/ejb-jar.xml", builder);
   }

   /*
    * This test has been removed as the documentation is
    * now in Freezone on JBoss Labs, and outside of EJB3 Core
    */
//   public void testTutorialEjbJar() throws Exception
//   {
//      DocumentBuilder builder = getDocumentBuilder();
//
//      validateFile("../docs/tutorial/ejb21_client_adaptors/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/interceptor/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/jboss_deployment_descriptor/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/jboss_resource_ref/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/jca/inflow/quartz/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/jca/inflow/swiftmq/resources/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/mdb_deployment_descriptor/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/reference21_30/META-INF/ejb-jar2.xml", builder);
//      validateFile("../docs/tutorial/reference21_30/META-INF/ejb-jar3.xml", builder);
//      validateFile("../docs/tutorial/stateful_deployment_descriptor/META-INF/ejb-jar.xml", builder);
//      validateFile("../docs/tutorial/stateless_deployment_descriptor/META-INF/ejb-jar.xml", builder);
//   }

   public void testTestJBoss() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();

      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/aspectdomain/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/bank/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/circulardependency/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/classloader/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/clusteredsession/islocal/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/clusteredsession/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/clusteredsession/scoped/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/consumer/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/mdb/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dd/web/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/dependency/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree936/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree939/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree957/one/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree957/two/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree959/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree963/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ejbthree989/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/factoryxml/session1/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/factoryxml/session2/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/homeinterface/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/interceptors2/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/jca/inflow/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/jms/managed/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/libdeployment/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/mail/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/mdb/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/mdbtransactions/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/microbench/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/naming/META-INF/jboss1.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/naming/META-INF/jboss2.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/propertyreplacement/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/reference21_30/META-INF/jboss2.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/reference21_30/META-INF/jboss3.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/reference21_30/META-INF/jboss-reference2.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/reference21_30/META-INF/jboss-reference.xml", builder);
      // BOGUS? validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/schema/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/security5/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/securitydomain/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/service/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/servicexmbean/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/servlet/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/ssladvanced/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/stateful/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/strictpool/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/tck5sec/META-INF/jboss.xml", builder);
      validateFile(SchemaTestCase.LOCATION_RESOURCES_TEST + "/unauthenticatedprincipal/META-INF/jboss.xml", builder);

   }

   private void validateFile(String filename, DocumentBuilder builder) throws Exception
   {
      File xmlFile = new File(filename);
      //log.info("Parsing and validating " + filename);
      try
      {
         builder.parse(xmlFile);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Exception parsing " + filename, e);
      }
      
      //log.info("Success parsing " + filename);
   }

   private DocumentBuilder getDocumentBuilder() throws Exception
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setAttribute("http://apache.org/xml/features/validation/schema", true);

      factory.setValidating(true);
      factory.setNamespaceAware(true);

      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(this);

      JBossEntityResolver entityResolver = new JBossEntityResolver();
      builder.setEntityResolver(entityResolver);

      return builder;
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(SchemaTestCase.class, "");
   }

   public void fatalError(SAXParseException e)
   {
      this.error(e);
   }

   public void error(SAXParseException e)
   {
      String id = e.getSystemId() != null ? e.getSystemId() : e.getPublicId();
      log.error("Failed to parse: " + id);
      log.error("Error at [" + e.getLineNumber() + ',' + e.getColumnNumber() + "]: ");
      log.error(e.getMessage());
      TestCase.fail(e.toString());
   }

   public void warning(SAXParseException e)
   {
      log.info("Warning: " + e);
      TestCase.fail(e.toString());
   }

}
