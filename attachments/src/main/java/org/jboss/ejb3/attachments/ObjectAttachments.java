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
package org.jboss.ejb3.attachments;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allow for objects to be attached to another object (called the master). This will
 * ensure that once the master is garbage collected all attachments are garbage
 * collected as wel. Note that care must be taken that the attachment in no way
 * has a strong reference to the master.
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class ObjectAttachments
{
   private static WeakHashMap<Object, Map<Object, Object>> globalAttachments = new WeakHashMap<Object, Map<Object,Object>>();
   
   /**
    * For debugging purposes.
    * 
    * Once a master is GCed the entry is put on a queue in WeakHashMap. To
    * really cleanup the entry it must be expunged.
    */
   public static void expungeStaleEntries()
   {
      // internal knowledge of WeakHashMap
      globalAttachments.size();
   }
   
   /**
    * Get a given attachment from an object.
    * 
    * @param <T>
    * @param master         the master object
    * @param identifier     the identifier of the attachment
    * @param expectedType   the class of the attachment
    * @return               the attachment
    * @throws AttachmentNotFoundException   if no attachment could be found
    * @throws ClassCastException            if the attachment is of a different class
    */
   public static <T> T getAttachment(Object master, Object identifier, Class<T> expectedType) throws AttachmentNotFoundException
   {
      if(master == null) throw new NullPointerException("master is null");
      if(identifier == null) throw new NullPointerException("identifier is null");
      
      Map<Object, ?> attachments = getAttachments(master);
      Object attachment = attachments.get(identifier);
      if(attachment == null)
         throw new AttachmentNotFoundException(master, identifier, expectedType);
      Class<?> attachmentClass = attachment.getClass();
      if(expectedType.isAssignableFrom(attachmentClass))
         return expectedType.cast(attachment);
      throw new ClassCastException("Expected type " + expectedType + " for attachment '" + identifier + "' on object '" + master + "', but was " + attachmentClass);
   }
   
   private static Map<Object, Object> getAttachments(Object master)
   {
      Map<Object, Object> attachments = globalAttachments.get(master);
      if(attachments == null)
      {
         synchronized (master)
         {
            attachments = globalAttachments.get(master);
            if(attachments == null)
            {
               attachments = new ConcurrentHashMap<Object, Object>();
               globalAttachments.put(master, attachments);
            }
         }
      }
      return attachments;
   }
   
   /**
    * Attach an object to a master object.
    * 
    * @param master         the master object
    * @param identifier     the identifier of the attachment
    * @param attachment     the attachment
    */
   public static void setAttachment(Object master, Object identifier, Object attachment)
   {
      if(master == null) throw new NullPointerException("master is null");
      if(identifier == null) throw new NullPointerException("identifier is null");
      if(attachment == null) throw new NullPointerException("attachment is null");
      
      Map<Object, Object> attachments = getAttachments(master);
      attachments.put(identifier, attachment);
   }
}
