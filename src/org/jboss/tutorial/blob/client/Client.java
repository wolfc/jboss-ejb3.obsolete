/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.blob.client;

import org.jboss.tutorial.blob.bean.LobTester;

import javax.naming.InitialContext;

import java.util.HashMap;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class Client
{
   public static void main(String[] args) throws Exception
   {
      InitialContext ctx = new InitialContext();
      LobTester test = (LobTester) ctx.lookup(LobTester.class.getName());
      long blobId = test.create();
      HashMap map = test.findBlob(blobId);
      System.out.println("is hello in map: " + map.get("hello"));
      System.out.println(test.findClob(blobId));
   }
}
