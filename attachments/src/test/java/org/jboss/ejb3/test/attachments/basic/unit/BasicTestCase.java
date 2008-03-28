/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.attachments.basic.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jboss.ejb3.attachments.AttachmentNotFoundException;
import org.jboss.ejb3.attachments.ObjectAttachments;
import org.jboss.ejb3.test.attachments.basic.FinalizableObject;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class BasicTestCase
{
   private static void gc()
   {
      gc1();
      ObjectAttachments.expungeStaleEntries();
      gc1();
   }
   
   private static void gc1()
   {
      for(int i = 0; i < 3; i++)
      {
         System.gc();
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException e)
         {
            // ignore
         }
         System.runFinalization();
      }
   }
   
   @Before
   public void before()
   {
      FinalizableObject.finalized = 0;
   }
   
   @Test
   public void testFinalizeAttachment() throws Exception
   {
      Object master = new FinalizableObject();
      
      FinalizableObject attachment = new FinalizableObject();
      
      Object identifier = "random key";
      
      ObjectAttachments.setAttachment(master, identifier, attachment);
      
      attachment = null;
      master = null;
      
      gc();
      
      assertEquals(2, FinalizableObject.finalized);
   }

   @Test
   public void testKeepAttachmentAlive() throws Exception
   {
      Object master = new FinalizableObject();
      
      FinalizableObject attachment = new FinalizableObject("Hello world");
      
      Object identifier = "random key";
      
      ObjectAttachments.setAttachment(master, identifier, attachment);
      attachment = null;
      
      gc();
      
      FinalizableObject actual = ObjectAttachments.getAttachment(master, identifier, FinalizableObject.class);
      
      assertEquals("Hello world", actual.getState());
      
      actual = null;
      master = null;
      
      gc();
      
      assertEquals(2, FinalizableObject.finalized);
   }

   @Test
   public void testNotFound() throws Exception
   {
      Object master = new Object();
      
      Object identifier = "not found";
      
      try
      {
         ObjectAttachments.getAttachment(master, identifier, Object.class);
         fail("Expected AttachmentNotFoundException");
      }
      catch(AttachmentNotFoundException e)
      {
         // okay
      }
   }
   
   @Test
   public void testSubclass() throws Exception
   {
      Object master = new Object();
      
      Object attachment = new Object();
      
      Object identifier = "random key";
      
      ObjectAttachments.setAttachment(master, identifier, attachment);
      
      try
      {
         ObjectAttachments.getAttachment(master, identifier, String.class);
         fail("Should have gotten a ClassCastException");
      }
      catch(ClassCastException e)
      {
         // okay
      }
   }
}
