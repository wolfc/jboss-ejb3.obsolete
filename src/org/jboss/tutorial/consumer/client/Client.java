/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.consumer.client;

import org.jboss.tutorial.consumer.bean.ExampleProducerRemote;
import org.jboss.tutorial.consumer.bean.Tester;
import org.jboss.ejb3.mdb.ProducerManager;
import org.jboss.ejb3.mdb.ProducerObject;

import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Map;

public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      ExampleProducerRemote remote = (ExampleProducerRemote) ctx.lookup(ExampleProducerRemote.class.getName());
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
         manager.close();
      }


      // Try out local producers by interfacing with Session bean
      Tester tester = (Tester) ctx.lookup(Tester.class.getName());
      tester.testLocal();
      tester.testXA();
   }
}
