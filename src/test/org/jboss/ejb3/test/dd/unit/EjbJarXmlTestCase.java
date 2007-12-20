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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jboss.ejb3.metamodel.AssemblyDescriptor;
import org.jboss.ejb3.metamodel.CmrField;
import org.jboss.ejb3.metamodel.ContainerTransaction;
import org.jboss.ejb3.metamodel.EjbJarDD;
import org.jboss.ejb3.metamodel.EjbJarDDObjectFactory;
import org.jboss.ejb3.metamodel.EjbRelation;
import org.jboss.ejb3.metamodel.EjbRelationshipRole;
import org.jboss.ejb3.metamodel.EnterpriseBean;
import org.jboss.ejb3.metamodel.EnterpriseBeans;
import org.jboss.ejb3.metamodel.EntityEnterpriseBean;
import org.jboss.ejb3.metamodel.JBossDDObjectFactory;
import org.jboss.ejb3.metamodel.MessageDrivenBean;
import org.jboss.ejb3.metamodel.MessageDrivenDestination;
import org.jboss.ejb3.metamodel.Method;
import org.jboss.ejb3.metamodel.MethodPermission;
import org.jboss.ejb3.metamodel.RelationshipRoleSource;
import org.jboss.ejb3.metamodel.Relationships;
import org.jboss.ejb3.metamodel.SecurityIdentity;
import org.jboss.ejb3.metamodel.Service;
import org.jboss.ejb3.metamodel.SessionEnterpriseBean;
import org.jboss.logging.Logger;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.metamodel.descriptor.EjbLocalRef;
import org.jboss.metamodel.descriptor.NameValuePair;
import org.jboss.metamodel.descriptor.ResourceRef;
import org.jboss.metamodel.descriptor.SecurityRole;

/**
 * JUnit TestCase for JbossXB usage for ejb-jar.xml deployment descriptor for
 * version 1.4 schema
 * 
 * @version <tt>$Revision: 61136 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */

public class EjbJarXmlTestCase extends TestCase
{

   private static final Logger log = Logger.getLogger(EjbJarXmlTestCase.class);

   public EjbJarXmlTestCase(String name)
   {

      super(name);

   }

   // Tests
   public void testUnmarshalDDXsd() throws Exception
   {
      // create an object model factory
      ObjectModelFactory factory = new EjbJarDDObjectFactory();
      URL xmlUrl = getResourceUrl("dd/ejb-jar.xml");
      assertNotNull(xmlUrl);
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
            .newUnmarshaller();
      EjbJarDD dd = (EjbJarDD) unmarshaller.unmarshal(xmlUrl.openStream(),
            factory, null);
      assertNotNull(dd);

      factory = new JBossDDObjectFactory(dd);
      xmlUrl = getResourceUrl("dd/jboss.xml");
      assertNotNull(xmlUrl);
      unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      dd = (EjbJarDD) unmarshaller
            .unmarshal(xmlUrl.openStream(), factory, null);

      checkUnmarshalledDD(dd);
   }
   
   public void testUnmarshalMdb() throws Exception
   {
      // create an object model factory
      ObjectModelFactory factory = new EjbJarDDObjectFactory();
      URL xmlUrl = getResourceUrl("dd/mdb/META-INF/ejb-jar.xml");
      assertNotNull(xmlUrl);
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance()
            .newUnmarshaller();
      EjbJarDD dd = (EjbJarDD) unmarshaller.unmarshal(xmlUrl.openStream(),
            factory, null);
      assertNotNull(dd);

      factory = new JBossDDObjectFactory(dd);
      xmlUrl = getResourceUrl("dd/mdb/META-INF/jboss.xml");
      assertNotNull(xmlUrl);
      unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      dd = (EjbJarDD) unmarshaller
            .unmarshal(xmlUrl.openStream(), factory, null);

      checkUnmarshalledMdb(dd);
   }

   private void checkUnmarshalledDD(EjbJarDD dd)
   {
      log.debug("unmarshalled DD: " + dd);

      assertEquals("2.1", dd.getVersion());
      assertEquals("DukesBankEJBJAR", dd.getDisplayName());
      
      assertEquals("java:/jaas/dukesbank", dd.getSecurityDomain());

      Iterator ejbs = dd.getEnterpriseBeans().getEnterpriseBeans().iterator();
      assertNotNull(ejbs);
      assertEquals(8, dd.getEnterpriseBeans().getEnterpriseBeans().size());
      
      EnterpriseBean ejb = null;
      
      ejb = (EnterpriseBean) ejbs.next();
      assertEquals("ServiceSix", ejb.getEjbName());
      assertEquals("org.jboss.ejb3.test.service.ServiceSixLocal", ejb.getLocal());
      assertEquals("org.jboss.ejb3.test.service.ServiceSixRemote", ejb.getRemote());
      assertTrue(ejb instanceof Service);
      Service service = (Service)ejb;
      assertEquals("org.jboss.ejb3.test.service.ServiceSixManagement", service.getManagement());
      
      ejbs.next();
      ejb = (EnterpriseBean) ejbs.next();
      assertEquals("CustomerBean", ejb.getEjbName());
      assertTrue(ejb instanceof EntityEnterpriseBean);
      assertEquals("Container", ((EntityEnterpriseBean) ejb)
            .getPersistenceType());

      Relationships relationships = dd.getRelationships();
      assertNotNull(relationships);
      List relations = relationships.getEjbRelations();
      assertEquals(2, relations.size());
      EjbRelation relation = (EjbRelation) relations.get(0);
      assertEquals("account-customer", relation.getEjbRelationName());
      List roles = relation.getEjbRelationshipRoles();
      assertEquals(2, roles.size());
      EjbRelationshipRole role = (EjbRelationshipRole) roles.get(0);
      assertEquals("customer-belongs-to-account", role
            .getEjbRelationshipRoleName());
      assertEquals("Many", role.getMultiplicity());
      assertFalse(role.isCascadeDelete());
      RelationshipRoleSource source = role.getRelationshipRoleSource();
      assertNotNull(source);
      assertEquals("CustomerBean", source.getEjbName());
      CmrField field = role.getCmrField();
      assertNotNull(field);
      assertEquals("accounts", field.getCmrFieldName());
      assertEquals("java.util.Collection", field.getCmrFieldType());
      
      ejbs.next();
      ejbs.next();
      ejbs.next();
      ejb = (EnterpriseBean) ejbs.next();
      assertEquals("AccountControllerBean", ejb.getEjbName());
      assertEquals("java:/jaas/override", ejb.getSecurityDomain());
     
      assertEquals("com.sun.ebank.ejb.account.AccountControllerHome", ejb
            .getHome());
      assertEquals("com.sun.ebank.ejb.account.AccountController", ejb
            .getRemote());
      assertEquals("com.sun.ebank.ejb.account.AccountControllerBean", ejb
            .getEjbClass());
      log.info("ejb.getLocal() " + ejb.getLocal());
      assertNull(ejb.getLocal());
      assertNull(ejb.getLocalHome());
      assertTrue(ejb instanceof SessionEnterpriseBean);
      SessionEnterpriseBean session = (SessionEnterpriseBean) ejb;
      assertEquals("Stateful", session.getSessionType());
      assertEquals(javax.ejb.TransactionManagementType.CONTAINER, session.getTransactionManagementType());
      assertEquals("ebankAccountController", session.getJndiName());
      SecurityIdentity si = session.getSecurityIdentity();
      assertNotNull(si);
      assertTrue(si.isUseCallerIdentity());
      Collection ejblocalRefs = session.getEjbLocalRefs();
      assertEquals(3, ejblocalRefs.size());
      EjbLocalRef ejbLocalRef = (EjbLocalRef) ejblocalRefs.iterator().next();
      assertEquals("ejb/customer", ejbLocalRef.getEjbRefName());
      assertEquals("Entity", ejbLocalRef.getEjbRefType());
      assertEquals("com.sun.ebank.ejb.customer.LocalCustomerHome", ejbLocalRef
            .getLocalHome());
      assertEquals("com.sun.ebank.ejb.customer.LocalCustomer", ejbLocalRef
            .getLocal());
      assertEquals("CustomerBean", ejbLocalRef.getEjbLink());
      Collection resourceRefs = session.getResourceRefs();
      assertEquals(1, resourceRefs.size());
      ResourceRef resourceEnvRef = (ResourceRef) resourceRefs.iterator().next();
      assertEquals("jdbc/BankDB", resourceEnvRef.getResRefName());
      assertEquals("javax.sql.DataSource", resourceEnvRef.getResType());
      assertEquals("Container", resourceEnvRef.getResAuth());
      assertEquals("Shareable", resourceEnvRef.getResSharingScope());

      AssemblyDescriptor descriptor = dd.getAssemblyDescriptor();
      assertNotNull(descriptor);
      List securityRoles = descriptor.getSecurityRoles();
      assertEquals(2, securityRoles.size());
      SecurityRole securityRole = (SecurityRole) securityRoles.get(0);
      assertEquals("bankCustomer", securityRole.getRoleName());
      List methodPermissions = descriptor.getMethodPermissions();
      assertEquals(7, methodPermissions.size());
      MethodPermission methodPermission = (MethodPermission) methodPermissions
            .get(0);
      List roleNames = methodPermission.getRoleNames();
      assertNotNull(roleNames);
      assertEquals(1, roleNames.size());
      assertEquals("bankCustomer", roleNames.get(0));
      List<Method> methods = methodPermission.getMethods();
      assertNotNull(methods);
      assertEquals(methods.size(), 1);
      Method method = methods.get(0);
      assertEquals("CustomerBean", method.getEjbName());
      assertEquals("*", method.getMethodName());
      List containerTransactions = descriptor.getContainerTransactions();
      assertEquals(7, containerTransactions.size());
      ContainerTransaction containerTransaction = (ContainerTransaction) containerTransactions
            .get(0);
      assertEquals("Required", containerTransaction.getTransAttribute());
      method = containerTransaction.getMethod();
      assertNotNull(method);
      assertEquals("AccountControllerBean", method.getEjbName());
      assertEquals("*", method.getMethodName());
   }
   
   private void checkUnmarshalledMdb(EjbJarDD dd)
   {
      log.debug("unmarshalled DD: " + dd);

      EnterpriseBeans ejbs = dd.getEnterpriseBeans();
      assertNotNull(ejbs);
      assertEquals(9, ejbs.getEnterpriseBeans().size());
      Iterator ejbIterator = ejbs.getEnterpriseBeans().iterator();
     
      ejbIterator.next();
      ejbIterator.next();
      EnterpriseBean ejb = (EnterpriseBean)ejbIterator.next();
      assertEquals("ObjectMessageBean", ejb.getEjbName());
      assertEquals("org.jboss.ejb3.test.dd.mdb.ObjectMessageBean",ejb.getEjbClass());
      assertTrue(ejb instanceof MessageDrivenBean);
      MessageDrivenBean mdb = (MessageDrivenBean) ejb;
      NameValuePair property = (NameValuePair)mdb.getActivationConfig().getActivationConfigProperties().get(0);
      assertEquals("AUTO_ACKNOWLEDGE", property.getValue());
      assertEquals("Bean", mdb.getTransactionType());
      MessageDrivenDestination destination = mdb.getMessageDrivenDestination();
      assertNotNull(destination);
      assertEquals("javax.jms.Queue",destination.getDestinationType());
   }

   private static URL getResourceUrl(String name)
   {
      URL url = Thread.currentThread().getContextClassLoader()
            .getResource(name);
      if (url == null)
      {
         throw new IllegalStateException("Resource not found: " + name);
      }
      return url;
   }

   public static Test suite() throws Exception
   {
      return new TestSuite(EjbJarXmlTestCase.class);
   }

}
