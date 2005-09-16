/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.client;

import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import org.jboss.ejb3.mdb.ProducerManager;
import org.jboss.ejb3.mdb.ProducerObject;
import org.jboss.ejb3.mdb.ProducerConfig;
import org.jboss.tutorial.consumer.bean.ExampleProducerRemote;
import org.jboss.tutorial.consumer.bean.Tester;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      ExampleProducerRemote remote = (ExampleProducerRemote) ctx.lookup(ExampleProducerRemote.class.getName());

      // you can typecast the returned proxy to obtain a ProducerManager interface that allows you to manage
      // interaction with JMS.
      ProducerManager manager = ((ProducerObject) remote).getProducerManager();


      // connect
      manager.connect();

      try
      {
         // Call method1
         remote.method1("Remote method1 called", 1);

         // Call method2
         Map<String, String> map = new HashMap<String, String>();
         map.put("hello", "world");
         map.put("great", "ejb3");

         remote.method2("Remote method2 called", map);
      }
      finally
      {
         // instead of typecasting, you can use a helper class that does everything for you.
         ProducerConfig.close(remote);
      }


      // Try out local producers by interfacing with Session bean
      Tester tester = (Tester) ctx.lookup(Tester.class.getName());
      tester.testLocal();
      tester.testXA();
   }
}
