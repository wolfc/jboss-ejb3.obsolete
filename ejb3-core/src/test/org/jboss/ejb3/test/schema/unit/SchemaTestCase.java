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

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.util.xml.JBossEntityResolver;

import junit.framework.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * 
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class SchemaTestCase
    extends JBossTestCase
    implements ErrorHandler
{
   Exception caughtException = null;
   
   private static final Logger log = Logger
         .getLogger(SchemaTestCase.class);

   public SchemaTestCase(String name)
   {
      super(name);
   }
   
   public void testEjbClassOptionalEjbJar() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();
      
      validateFile("../src/resources/test/dd/override/META-INF/ejb-jarC.xml", builder);
      validateFile("../src/resources/test/dd/override/META-INF/ejb-jarD.xml", builder);
      validateFile("../src/resources/test/enventry/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/mail/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/reference21_30/META-INF/ejb-jar3.xml", builder);
   }
   
   public void testTestEjbJar() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();
      
      validateFile("../src/resources/test/bank/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/bmt/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/dd/mdb/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/dd/override/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/dd/override/META-INF/ejb-jarA.xml", builder);
      validateFile("../src/resources/test/dd/override/META-INF/ejb-jarB.xml", builder);
      validateFile("../src/resources/test/factoryxml/session1/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/factoryxml/session2/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/interceptors/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/interceptors2/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/jca/inflow/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/jms/managed/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/microbench/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/naming/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/securitydomain/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/service/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/stateful/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/txexceptions/META-INF/ejb-jar.xml", builder);
      validateFile("../src/resources/test/webservices/META-INF/ejb-jar.xml", builder);
   }
   
   public void testTutorialEjbJar() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();
      
      validateFile("../docs/tutorial/ejb21_client_adaptors/META-INF/ejb-jar.xml", builder);
      validateFile("../docs/tutorial/interceptor/META-INF/ejb-jar.xml", builder);
      validateFile("../docs/tutorial/jboss_deployment_descriptor/META-INF/ejb-jar.xml", builder);
      validateFile("../docs/tutorial/jboss_resource_ref/META-INF/ejb-jar.xml", builder);
      validateFile("../docs/tutorial/jca/inflow/swiftmq/resources/META-INF/ejb-jar.xml", builder);
      validateFile("../docs/tutorial/mdb_deployment_descriptor/META-INF/ejb-jar.xml", builder);
      validateFile("../docs/tutorial/stateful_deployment_descriptor/META-INF/ejb-jar.xml", builder);
      validateFile("../docs/tutorial/stateless_deployment_descriptor/META-INF/ejb-jar.xml", builder);
   }
   
   public void testTestJBoss() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();
      
      validateFile("../src/resources/test/bank/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/clusteredsession/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/consumer/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/dd/mdb/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/dd/web/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/dependency/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/interceptors2/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/jca/inflow/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/mail/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/microbench/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/naming/META-INF/jboss1.xml", builder);
      validateFile("../src/resources/test/naming/META-INF/jboss1.xml", builder);
      validateFile("../src/resources/test/reference21_30/META-INF/jboss3.xml", builder);
      validateFile("../src/resources/test/reference21_30/META-INF/jboss-reference.xml", builder);
      validateFile("../src/resources/test/securitydomain/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/service/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/ssladvanced/META-INF/jboss.xml", builder);
      validateFile("../src/resources/test/strictpool/META-INF/jboss.xml", builder);
   }
   
   public void testTutorialJBoss() throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();
      
      validateFile("../docs/tutorial/consumer_deployment_descriptor/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/dependency/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/ejb21_client_adaptors/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/jboss_deployment_descriptor/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/jboss_resource_ref/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/jca/inflow/swiftmq/resources/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/mdb_deployment_descriptor/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/service_deployment_descriptor/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/stateful_deployment_descriptor/META-INF/jboss.xml", builder);
      validateFile("../docs/tutorial/stateless_deployment_descriptor/META-INF/jboss.xml", builder);
   }
   
   private void validateFile(String filename, DocumentBuilder builder) throws Exception
   {
      File xmlFile = new File(filename);
      System.out.println("Parsing and validating " + filename);
      Document dom = builder.parse(xmlFile);
      
      if (caughtException != null)
         throw caughtException;
      
      System.out.println("Success parsing " + filename);
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
      System.out.println("fatalError " + e);
      caughtException = e;
   }
   
   public void error(SAXParseException e)
   {
      System.out.println("Error " + e);
      caughtException = e;
   }
   
   public void warning(SAXParseException e)
   {
      System.out.println("Warning " + e);
   }

}
 