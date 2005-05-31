/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.bean;

import java.util.HashMap;
import java.util.Map;
import org.jboss.annotation.JndiInject;
import javax.ejb.RemoteInterface;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.jboss.ejb3.mdb.ProducerManager;
import org.jboss.ejb3.mdb.ProducerObject;

/**
 * Show injecting in producers
 * Show how to interact with local producers
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
@Stateless
@RemoteInterface(Tester.class)
public class TesterBean implements Tester
{
   private ExampleProducerXA xa;
   private ProducerManager xaManager;

   @JndiInject(jndiName="org.jboss.tutorial.consumer.bean.ExampleProducerXA")
   public void setXa(ExampleProducerXA xa)
   {
      this.xa = xa;
      this.xaManager = ((ProducerObject)xa).getProducerManager();
   }

   private ExampleProducer local;
   private ProducerManager localManager;

   @JndiInject(jndiName="org.jboss.tutorial.consumer.bean.ExampleProducerLocal")
   public void setLocal(ExampleProducer local)
   {
      this.local = local;
      this.localManager = ((ProducerObject)local).getProducerManager();
   }

   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public void testXA() throws Exception
   {

      xaManager.connect();
      xa.method1("testXA", 1);
      Map<String, String> map = new HashMap<String, String>();
      map.put("hello", "world");
      map.put("great", "ejb3");
      xa.method2("testXA2", map);
      System.out.println("end TESTXA **");
      xaManager.close();
   }

   public void testLocal() throws Exception
   {

      localManager.connect();
      local.method1("testLocal", 1);
      Map<String, String> map = new HashMap<String, String>();
      map.put("hello", "world");
      map.put("great", "ejb3");
      local.method2("testLocal2", map);
      localManager.close();
   }

}
