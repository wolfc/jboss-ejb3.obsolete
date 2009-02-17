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
package org.jboss.ejb3.test.interceptors2.unit;

import java.util.ArrayList;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb3.mdb.ProducerManager;
import org.jboss.ejb3.mdb.ProducerObject;
import org.jboss.ejb3.test.common.EJB3TestCase;
import org.jboss.ejb3.test.interceptors2.AnnotatedClassInterceptor;
import org.jboss.ejb3.test.interceptors2.AnnotatedClassInterceptor3;
import org.jboss.ejb3.test.interceptors2.AnnotatedMethodInterceptor;
import org.jboss.ejb3.test.interceptors2.AnnotatedMethodInterceptor4;
import org.jboss.ejb3.test.interceptors2.AnnotatedOnlySLSB;
import org.jboss.ejb3.test.interceptors2.AnnotatedOnlySLSBRemote;
import org.jboss.ejb3.test.interceptors2.DefaultInterceptor;
import org.jboss.ejb3.test.interceptors2.DefaultOnlyProducer;
import org.jboss.ejb3.test.interceptors2.DefaultOnlySLSBRemote;
import org.jboss.ejb3.test.interceptors2.DefaultOnlyServiceRemote;
import org.jboss.ejb3.test.interceptors2.InheritingSFSB;
import org.jboss.ejb3.test.interceptors2.InheritingSFSBRemote;
import org.jboss.ejb3.test.interceptors2.Interception;
import org.jboss.ejb3.test.interceptors2.MethodOnlyInterceptedSLSBRemote;
import org.jboss.ejb3.test.interceptors2.MixedClassInterceptor;
import org.jboss.ejb3.test.interceptors2.MixedConfigSFSB;
import org.jboss.ejb3.test.interceptors2.MixedConfigSFSBRemote;
import org.jboss.ejb3.test.interceptors2.MixedMethodInterceptor;
import org.jboss.ejb3.test.interceptors2.MixedProducer;
import org.jboss.ejb3.test.interceptors2.MixedServiceRemote;
import org.jboss.ejb3.test.interceptors2.NoInterceptorsSLSBRemote;
import org.jboss.ejb3.test.interceptors2.OrderedSLSBRemote;
import org.jboss.ejb3.test.interceptors2.StatusRemote;
import org.jboss.ejb3.test.interceptors2.XMLClassInterceptor;
import org.jboss.ejb3.test.interceptors2.XMLClassInterceptor2;
import org.jboss.ejb3.test.interceptors2.XMLClassInterceptor3;
import org.jboss.ejb3.test.interceptors2.XMLMethodInterceptor;
import org.jboss.ejb3.test.interceptors2.XMLOnlySLSB;
import org.jboss.ejb3.test.interceptors2.XMLOnlySLSBRemote;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision$
 */
public class InterceptorsTestCase extends EJB3TestCase
{
   org.jboss.logging.Logger log = getLog();

   public InterceptorsTestCase(String name)
   {
      super(name);
   }

   public void testAnnotatedOnlySLSB() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      AnnotatedOnlySLSBRemote slsb = (AnnotatedOnlySLSBRemote)ctx.lookup("AnnotatedOnlySLSB/remote");
      
      status.clear();
      slsb.methodWithClassLevel();
      ArrayList<Interception> interceptions = status.getInterceptions();
      ArrayList<Interception> postConstructs = status.getPostConstructs();
      compare(new Interception[] {
            new Interception(AnnotatedClassInterceptor.class, "intercept3"), 
            new Interception(AnnotatedClassInterceptor.class, "intercept"), 
            new Interception(AnnotatedOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, true, false, false, false);
      // Note: AnnotatedMethodInterceptor postConstruct is not called, because it's
      // a business method interceptor (EJB 3.0 12.7 footnote 57)
      compareLifecycle(new Interception[] {
            new Interception(AnnotatedClassInterceptor.class, "postConstruct3"),
            new Interception(AnnotatedClassInterceptor.class, "postConstruct"),
            new Interception(AnnotatedOnlySLSB.class, "postConstruct")}, postConstructs);
      

      status.clear();
      slsb.methodExcludingClassInterceptors();
      interceptions = status.getInterceptions();
      compare(new Interception[] {new Interception(AnnotatedOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, false, false, false, false);
      
      status.clear();
      slsb.methodWithOwnInterceptors();
      interceptions = status.getInterceptions();
      compare(new Interception[] {
            new Interception(AnnotatedClassInterceptor.class, "intercept3"), 
            new Interception(AnnotatedClassInterceptor.class, "intercept"), 
            new Interception(AnnotatedMethodInterceptor.class, "intercept4"), 
            new Interception(AnnotatedMethodInterceptor.class, "intercept2"), 
            new Interception(AnnotatedMethodInterceptor.class, "intercept"), 
            new Interception(AnnotatedOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, false, false, false, false);
      
      status.clear();
      slsb.methodWithOwnInterceptorsExcludeClass();
      interceptions = status.getInterceptions();
      compare(new Interception[] {
            new Interception(AnnotatedMethodInterceptor.class, "intercept4"), 
            new Interception(AnnotatedMethodInterceptor.class, "intercept2"), 
            new Interception(AnnotatedMethodInterceptor.class, "intercept"), 
            new Interception(AnnotatedOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, false, false, false, false);
      
   }

   public void testXmlOnlySLSB() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      XMLOnlySLSBRemote slsb = (XMLOnlySLSBRemote)ctx.lookup("org.jboss.ejb3.test.interceptors2.XMLOnlySLSBRemote");
      
      status.clear();
      slsb.methodWithClassLevel();
      ArrayList<Interception> interceptions = status.getInterceptions();
      ArrayList<Interception> postConstructs = status.getPostConstructs();
      compare(new Interception[] {
            new Interception(DefaultInterceptor.class, "intercept"), 
      // FIXME: multiple around-invoke interceptors in XML
      //      new Interception(XMLClassInterceptor2.class, "intercept3"), 
            new Interception(XMLClassInterceptor2.class, "intercept2"), 
            new Interception(XMLOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, true, false, false, false);
      // Note: XMLMethodInterceptor postConstruct is not called, because it's
      // a business method interceptor (EJB 3.0 12.7 footnote 57)
      compareLifecycle(new Interception[] {
            new Interception(DefaultInterceptor.class, "postConstruct"),
      // FIXME: multiple lifecycle interceptors in XML
      //      new Interception(XMLClassInterceptor2.class, "postConstruct3"),
            new Interception(XMLClassInterceptor2.class, "postConstruct2"),
            new Interception(XMLOnlySLSB.class, "postConstruct")}, postConstructs);
      
      status.clear();
      long l = 10;
      slsb.overloadedMethod(l);
      interceptions = status.getInterceptions();
      compare(new Interception[] {
      // FIXME: multiple around-invoke interceptors in XML
      //      new Interception(XMLClassInterceptor2.class, "intercept3"), 
            new Interception(XMLClassInterceptor2.class, "intercept2"), 
            new Interception(XMLOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, false, false, false, false);

      status.clear();
      slsb.overloadedMethod(l, new String[][] {{"Hello"}, {"Whatever"}});
      interceptions = status.getInterceptions();
      compare(new Interception[] {
            new Interception(DefaultInterceptor.class, "intercept"),
      // FIXME: multiple around-invoke interceptors in XML
      //      new Interception(XMLClassInterceptor2.class, "intercept3"),
            new Interception(XMLClassInterceptor2.class, "intercept2"),
            new Interception(XMLMethodInterceptor.class, "intercept"), 
            new Interception(XMLOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, false, false, false, false);
      
      status.clear();
      int i = 5;
      slsb.overloadedMethod(i);
      interceptions = status.getInterceptions();
      compare(new Interception[] {
            new Interception(DefaultInterceptor.class, "intercept"),
            new Interception(XMLMethodInterceptor.class, "intercept"),
            new Interception(XMLOnlySLSB.class, "intercept")}, interceptions);
      checkLifecycle(status, false, false, false, false);
      
      status.clear();
      slsb.overloadedMethod();
      interceptions = status.getInterceptions();
      compare(new Interception[] {
            new Interception(XMLMethodInterceptor.class, "intercept"),
            new Interception(XMLOnlySLSB.class, "intercept")}, interceptions);
   }
   
   public void testDefaultOnlySLSB() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      DefaultOnlySLSBRemote slsb = (DefaultOnlySLSBRemote)ctx.lookup("DefaultOnlySLSB/remote");
      
      status.clear();
      slsb.test();
      ArrayList<Interception> interceptions = status.getInterceptions();
      compare(new Interception[] {new Interception(DefaultInterceptor.class, "intercept")}, interceptions);
      checkLifecycle(status, true, false, false, false);
      compareLifecycle(new Interception[] {new Interception(DefaultInterceptor.class, "postConstruct")}, status.getPostConstructs());
   }
   
   public void testNoInterceptorsSLSB() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      NoInterceptorsSLSBRemote slsb = (NoInterceptorsSLSBRemote)ctx.lookup("NoInterceptorsSLSB/remote");
      
      status.clear();
      slsb.test();
      ArrayList<Interception> interceptions = status.getInterceptions();
      compare(new Interception[0], interceptions);
      checkLifecycle(status, false, false, false, false);
   }

   public void testMixedConfigSFSB() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      
      // Note: XMLMethodInterceptor & AnnotatedMethodInterceptor postConstruct are 
      // not called, because they are business method interceptors (EJB 3.0 12.7 footnote 57)
      
      final Interception[] expectedTestInterceptions =
         new Interception[] {
            new Interception(DefaultInterceptor.class, "intercept"),
            new Interception(MixedClassInterceptor.class, "intercept"),
      // FIXME: multiple around-invoke interceptors in XML
      //      new Interception(XMLClassInterceptor.class, "intercept3"),
            new Interception(XMLClassInterceptor.class, "intercept"),
            new Interception(AnnotatedClassInterceptor.class, "intercept3"), 
            new Interception(AnnotatedClassInterceptor.class, "intercept"), 
            new Interception(MixedConfigSFSB.class, "intercept")};
      
      final Interception[] expectedTestWithMethodLevelInterceptions =
         new Interception[] {
            new Interception(DefaultInterceptor.class, "intercept"),
            new Interception(MixedClassInterceptor.class, "intercept"),
      // FIXME: multiple around-invoke interceptors in XML
      //      new Interception(XMLClassInterceptor.class, "intercept3"),
            new Interception(XMLClassInterceptor.class, "intercept"),
            new Interception(AnnotatedClassInterceptor.class, "intercept3"), 
            new Interception(AnnotatedClassInterceptor.class, "intercept"), 
            new Interception(MixedMethodInterceptor.class, "intercept"),
            new Interception(XMLMethodInterceptor.class, "intercept"),
            new Interception(AnnotatedMethodInterceptor.class, "intercept4"), 
            new Interception(AnnotatedMethodInterceptor.class, "intercept2"), 
            new Interception(AnnotatedMethodInterceptor.class, "intercept"),
            new Interception(MixedConfigSFSB.class, "intercept")};
      
      final Interception[] expectedPostConstructInterceptors =
         new Interception[] {
            new Interception(DefaultInterceptor.class, "postConstruct"),
      // FIXME: multiple lifecycle interceptors in XML
      //      new Interception(XMLClassInterceptor.class, "postConstruct3"),
            new Interception(XMLClassInterceptor.class, "postConstruct"),
            new Interception(AnnotatedClassInterceptor.class, "postConstruct3"),
            new Interception(AnnotatedClassInterceptor.class, "postConstruct")};
      
      final Interception[] expectedPostActivateInterceptors =
         new Interception[] {
            new Interception(DefaultInterceptor.class, "postActivate"),
      // FIXME: multiple lifecycle interceptors in XML
      //      new Interception(XMLClassInterceptor.class, "postActivate3"),
            new Interception(XMLClassInterceptor.class, "postActivate"),
            new Interception(AnnotatedClassInterceptor.class, "postActivate3"),
            new Interception(AnnotatedClassInterceptor.class, "postActivate")};
      
      final Interception[] expectedPrePassivateInterceptors =
         new Interception[] {
            new Interception(DefaultInterceptor.class, "prePassivate"),
      // FIXME: multiple lifecycle interceptors in XML
      //      new Interception(XMLClassInterceptor.class, "prePassivate3"),
            new Interception(XMLClassInterceptor.class, "prePassivate"),
            new Interception(AnnotatedClassInterceptor.class, "prePassivate3"),
            new Interception(AnnotatedClassInterceptor.class, "prePassivate")};
      
      final Interception[] expectedPreDestroyInterceptors =
         new Interception[] {
            new Interception(DefaultInterceptor.class, "preDestroy"),
      // FIXME: multiple lifecycle interceptors in XML
      //      new Interception(XMLClassInterceptor.class, "preDestroy3"),
            new Interception(XMLClassInterceptor.class, "preDestroy"),
            new Interception(AnnotatedClassInterceptor.class, "preDestroy3"),
            new Interception(AnnotatedClassInterceptor.class, "preDestroy")};
      
      status.clear();
      MixedConfigSFSBRemote sfsb1 = (MixedConfigSFSBRemote)ctx.lookup("MixedConfigSFSB/remote");
      sfsb1.test();
      ArrayList<Interception> bean1test = status.getInterceptions();
      ArrayList<Interception> bean1pc = status.getPostConstructs();
      compare(expectedTestInterceptions, bean1test);
      checkLifecycle(status, true, false, false, false);
      compareLifecycle(expectedPostConstructInterceptors, bean1pc);
      
      status.clear();
      sfsb1.testWithMethodLevel();
      ArrayList<Interception> bean1testml = status.getInterceptions();
      compare(expectedTestWithMethodLevelInterceptions, bean1testml);
      checkLifecycle(status, false, false, false, false);
      
      //Cache size is 1, so sfsb1 should get passivated and sfsb2 constructed
      status.clear();
      MixedConfigSFSBRemote sfsb2 = (MixedConfigSFSBRemote)ctx.lookup("MixedConfigSFSB/remote");
      sfsb2.test();
      ArrayList<Interception> bean2test = status.getInterceptions(); 
      ArrayList<Interception> bean2pc = status.getPostConstructs(); 
      ArrayList<Interception> bean2pp = status.getPrePassivates(); 
      compare(expectedTestInterceptions, bean2test);
      checkLifecycle(status, true, false, true, false);
      compareLifecycle(expectedPostConstructInterceptors, bean2pc);
      compareLifecycle(expectedPrePassivateInterceptors, bean2pp);
      
      status.clear();
      sfsb2.testWithMethodLevel();
      ArrayList<Interception> bean2testml = status.getInterceptions();
      compare(expectedTestWithMethodLevelInterceptions, bean2testml);
      checkLifecycle(status, false, false, false, false);
      
      status.clear();
      sfsb2.testWithMethodLevelB();
      ArrayList<Interception> bean2testmlb = status.getInterceptions();
      compare(expectedTestWithMethodLevelInterceptions, bean2testmlb);
      checkLifecycle(status, false, false, false, false);
      
      //Cache size is 1, so sfsb2 should get passivated and sfsb1 activated
      status.clear();
      sfsb1.test();
      ArrayList<Interception> bean1test_2 = status.getInterceptions();
      ArrayList<Interception> bean1pp_2 = status.getPrePassivates();
      ArrayList<Interception> bean1pa_2 = status.getPostActivates();
      compare(expectedTestInterceptions, bean1test_2);
      checkLifecycle(status, false, true, true, false);
      compareLifecycle(expectedPrePassivateInterceptors, bean1pp_2);
      compareLifecycle(expectedPostActivateInterceptors, bean1pa_2);
      
      status.clear();
      sfsb1.testWithMethodLevel();
      ArrayList<Interception> bean1testml_2 = status.getInterceptions();
      compare(expectedTestWithMethodLevelInterceptions, bean1testml_2);
      checkLifecycle(status, false, false, false, false);
      
      status.clear();
      sfsb1.testWithMethodLevelB();
      ArrayList<Interception> bean1testmlb_2 = status.getInterceptions();
      compare(expectedTestWithMethodLevelInterceptions, bean1testmlb_2);
      checkLifecycle(status, false, false, false, false);
      
      //interceptor instances used for bean 1 should all be the same, and different from those for bean 2
      //Need to check that we can remove
      status.clear();
      sfsb1.kill();
      ArrayList<Interception> preDestroys = status.getPreDestroys();
      checkLifecycle(status, false, false, false, true);
      compareLifecycle(expectedPreDestroyInterceptors, preDestroys);

      checkInstances(bean1test, bean1test_2, true);
      checkInstances(bean1testml, bean1testml_2, true);
      checkInstances(bean1testml, bean1testmlb_2, true);
      checkInstances(bean2testml, bean2testmlb, true);
      checkInstances(bean1test, bean2test, false);
      checkInstances(bean1testml, bean2testml, false);
      
      
      
   }
   
   public void testBeanHierarchy() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      
      status.clear();
      InheritingSFSBRemote bean1 = (InheritingSFSBRemote)ctx.lookup("InheritingSFSB/remote");
      bean1.method();
      ArrayList<Interception> interceptions = status.getInterceptions();
      ArrayList<Interception> postConstructs = status.getPostConstructs();
      checkLifecycle(status, true, false, false, false);
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept"),
                  new Interception(InheritingSFSB.class, "intercept3"),
                  new Interception(InheritingSFSB.class, "intercept")}, interceptions);
      compareLifecycle(            
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "postConstruct"),
                  new Interception(InheritingSFSB.class, "postConstruct3"),
                  new Interception(InheritingSFSB.class, "postConstruct")}, postConstructs);

      status.clear();
      InheritingSFSBRemote bean2 = (InheritingSFSBRemote)ctx.lookup("InheritingSFSB/remote");
      bean2.methodNoDefault();
      interceptions = status.getInterceptions();
      postConstructs = status.getPostConstructs();
      ArrayList<Interception> prePassivates = status.getPrePassivates();
      checkLifecycle(status, true, false, true, false);
      compare(
            new Interception[] {
                  new Interception(InheritingSFSB.class, "intercept3"),
                  new Interception(InheritingSFSB.class, "intercept")}, interceptions);
      compareLifecycle(            
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "postConstruct"),
                  new Interception(InheritingSFSB.class, "postConstruct3"),
                  new Interception(InheritingSFSB.class, "postConstruct")}, postConstructs);
      compareLifecycle(            
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "prePassivate"),
                  new Interception(InheritingSFSB.class, "prePassivate3"),
                  new Interception(InheritingSFSB.class, "prePassivate")}, prePassivates);
      
      status.clear();
      bean1.methodNoDefault();
      interceptions = status.getInterceptions();
      prePassivates = status.getPrePassivates();
      ArrayList<Interception> postActivates = status.getPostActivates();
      checkLifecycle(status, false, true, true, false);
      compare(
            new Interception[] {
                  new Interception(InheritingSFSB.class, "intercept3"),
                  new Interception(InheritingSFSB.class, "intercept")}, interceptions);
      compareLifecycle(            
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "postActivate"),
                  new Interception(InheritingSFSB.class, "postActivate3"),
                  new Interception(InheritingSFSB.class, "postActivate")}, postActivates);
      compareLifecycle(            
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "prePassivate"),
                  new Interception(InheritingSFSB.class, "prePassivate3"),
                  new Interception(InheritingSFSB.class, "prePassivate")}, prePassivates);
      
      
      //Need to check that we can remove
      status.clear();
      bean1.kill();
      ArrayList<Interception> preDestroys = status.getPreDestroys();
      checkLifecycle(status, false, false, false, true);
      compareLifecycle(            
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "preDestroy"),
                  new Interception(InheritingSFSB.class, "preDestroy3"),
                  new Interception(InheritingSFSB.class, "preDestroy")}, preDestroys);
   }
   
   public void testOverrideOrdering() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      OrderedSLSBRemote slsb = (OrderedSLSBRemote)ctx.lookup("OrderedSLSB/remote");
      
      status.clear();
      slsb.methodWithClassLevel();
      checkLifecycle(status, true, false, false, false);
      ArrayList<Interception> actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception( AnnotatedClassInterceptor3.class, "intercept3"),
                  new Interception( XMLClassInterceptor3.class, "intercept3"),
                  new Interception( DefaultInterceptor.class, "intercept")}, actual);
      actual = status.getPostConstructs();
      compareLifecycle(new Interception[] {
            new Interception( AnnotatedClassInterceptor3.class, "postConstruct3"),
            new Interception( XMLClassInterceptor3.class, "postConstruct3"),
            new Interception( DefaultInterceptor.class, "postConstruct")}, actual);

      status.clear();
      slsb.methodWithOwn("l", 5);
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception( XMLMethodInterceptor.class,"intercept"),
                  new Interception( DefaultInterceptor.class,"intercept"), // listed in the descriptor
                  new Interception( AnnotatedMethodInterceptor.class,"intercept4"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept2"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept")}, actual);
      
      status.clear();
      slsb.overLoadedMethod("x");
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception( AnnotatedClassInterceptor3.class, "intercept3"),
                  new Interception( DefaultInterceptor.class, "intercept"),
                  new Interception( XMLClassInterceptor3.class, "intercept3"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept4"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept2"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept"),
                  new Interception( MixedMethodInterceptor.class,"intercept"),
                  new Interception( XMLMethodInterceptor.class,"intercept")}, actual);
      
      status.clear();
      slsb.overLoadedMethod(5);
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception( XMLClassInterceptor3.class, "intercept3"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept4"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept2"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept"),
                  new Interception( AnnotatedClassInterceptor3.class, "intercept3"),
                  new Interception( DefaultInterceptor.class, "intercept"),
                  new Interception( MixedMethodInterceptor.class,"intercept"),
                  new Interception( XMLMethodInterceptor.class,"intercept"),
            }, actual);
      
      status.clear();
      slsb.overLoadedMethod();
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception( MixedMethodInterceptor.class,"intercept"),
                  new Interception( DefaultInterceptor.class, "intercept")
            }, actual);
      
      status.clear();
      slsb.methodNotSpecifyingAll();
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception( XMLClassInterceptor3.class, "intercept3"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept4"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept2"),
                  new Interception( AnnotatedMethodInterceptor.class,"intercept"),
                  new Interception( AnnotatedClassInterceptor3.class, "intercept3"),
                  new Interception( DefaultInterceptor.class, "intercept"),
                  new Interception( XMLMethodInterceptor.class,"intercept"),
                  new Interception( MixedMethodInterceptor.class,"intercept")}, actual);
      
   }
   
   public void testService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      MixedServiceRemote srv = (MixedServiceRemote)ctx.lookup("MixedService/remote");
      
      status.clear();
      srv.defaultMethod();
      ArrayList<Interception> actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept"),
                  new Interception(MixedClassInterceptor.class, "intercept"),
                  new Interception(MixedMethodInterceptor.class, "intercept")}, actual);

      
      status.clear();
      srv.remoteMethod();
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept"),
                  new Interception(MixedMethodInterceptor.class, "intercept")}, actual);
      
      status.clear();
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("test:service=mixed");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "managementMethod", params, sig);
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept"),
                  new Interception(MixedClassInterceptor.class, "intercept"),
                  new Interception(MixedMethodInterceptor.class, "intercept")}, actual);
   }
   
   public void testDefaultOnlyService() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      DefaultOnlyServiceRemote srv = (DefaultOnlyServiceRemote)ctx.lookup("DefaultOnlyService/remote");
      
      status.clear();
      srv.method();
      ArrayList<Interception> actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept")}, actual);
      
      status.clear();
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("test:service=DefaultOnly");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "method", params, sig);
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept")}, actual);
   }
   
   public void testMixedConsumer() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      MixedProducer prod = (MixedProducer)ctx.lookup("org.jboss.ejb3.test.interceptors2.MixedProducer");
      ProducerManager manager = ((ProducerObject) prod).getProducerManager();
      manager.connect();

      status.clear();
      prod.method();
      Thread.sleep(1000); //Give receiving end enough time to pick up interceptions
      ArrayList<Interception> actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept"),
                  new Interception(MixedClassInterceptor.class, "intercept"),
                  new Interception(XMLMethodInterceptor.class, "intercept")}, actual);
      
      status.clear();
      prod.methodWithOwnOnly();
      Thread.sleep(1000); //Give receiving end enough time to pick up interceptions
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(MixedMethodInterceptor.class, "intercept")}, actual);
   }
   
   public void testDefaultOnlyConsumer() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      DefaultOnlyProducer prod = (DefaultOnlyProducer)ctx.lookup("org.jboss.ejb3.test.interceptors2.DefaultOnlyProducer");
      ProducerManager manager = ((ProducerObject) prod).getProducerManager();
      manager.connect();

      status.clear();
      prod.method();
      Thread.sleep(1000); //Give receiving end enough time to pick up interceptions
      ArrayList<Interception> actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept")}, actual);
   }
   
   public void testMethodOnlySLSB() throws Exception
   {
      InitialContext ctx = new InitialContext();
      StatusRemote status = (StatusRemote)ctx.lookup("StatusBean/remote");
      MethodOnlyInterceptedSLSBRemote remote = (MethodOnlyInterceptedSLSBRemote)ctx.lookup("MethodOnlyInterceptedSLSB/remote");

      status.clear();
      remote.intercepted();
      ArrayList<Interception> actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept"), 
                  new Interception(AnnotatedMethodInterceptor4.class, "intercept4")}, actual);
      
      status.clear();
      remote.notintercepted();
      actual = status.getInterceptions();
      compare(
            new Interception[] {
                  new Interception(DefaultInterceptor.class, "intercept")}, actual);
   }
   
   private void compare(Interception[] expected, ArrayList<Interception> interceptions)
   {
      assertEquals("Bad interceptions: " + interceptions, expected.length, interceptions.size());
      
      for (int i = 0 ; i < expected.length ; i++)
      {
         assertEquals(
               "index " + i, 
               expected[i].getClassname() + "." + expected[i].getMethod(), 
               interceptions.get(i).getClassname() + "." + interceptions.get(i).getMethod());
      }
   }
   
   
   private void checkLifecycle(StatusRemote status, 
         boolean expectPostConstruct,
         boolean expectPostActivate,
         boolean expectPrePassivate,
         boolean expectPreDestroy)
   {
      if (expectPostConstruct)
      {
         assertNotSame(0, status.getPostConstructs().size());
      }
      else
      {
         assertEquals("Number of post constructs", 0, status.getPostConstructs().size());
      }

      if (expectPostActivate)
      {
         assertNotSame(0, status.getPostActivates().size());
      }
      else
      {
         assertEquals(0, status.getPostActivates().size());
      }

      if (expectPrePassivate)
      {
         assertNotSame(0, status.getPrePassivates().size());
      }
      else
      {
         assertEquals(0, status.getPrePassivates().size());
      }

      if (expectPreDestroy)
      {
         assertNotSame(0, status.getPreDestroys().size());
      }
      else
      {
         assertEquals(0, status.getPreDestroys().size());
      }
}
   
   private void compareLifecycle(Interception[] expected, ArrayList<Interception> interceptions)
   {
      assertEquals("\nExpected:\n" + toString(expected) + "\nHad interceptions\n" + interceptions, expected.length, interceptions.size());
      
      for (int i = 0 ; i < expected.length ; i++)
      {
         assertEquals(
               "index " + i, 
               expected[i].getClassname() + "." + expected[i].getMethod(), 
               interceptions.get(i).getClassname() + "." + interceptions.get(i).getMethod());
      }
   }
   
   private void checkInstances(ArrayList<Interception> interceptionsA, ArrayList<Interception> interceptionsB, boolean same)
   {
      for (int i = 0 ; i < interceptionsA.size() ; i++)
      {
         if (interceptionsA.get(i).getInstance() >= 0 || interceptionsB.get(i).getInstance() >= 0)
         if (same)
         {
            assertEquals("Instances should be the same (" + i + ")", interceptionsA.get(i).getInstance(), interceptionsB.get(i).getInstance());
         }
         else
         {
            assertNotSame("Instances should not be the same (" + i + ")", interceptionsA.get(i).getInstance(), interceptionsB.get(i).getInstance());
         }
      }
   }
   
   private String toString(Object[] array)
   {
      StringBuffer buf = new StringBuffer("[");
      
      for (int i = 0 ; i < array.length ; i++)
      {
         if (i > 0) buf.append(", ");
         
         buf.append(array[i]);
      }
      
      buf.append("]");
      return buf.toString();
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(InterceptorsTestCase.class, "interceptors2test-service.xml, interceptors2-test.jar");
   }

}