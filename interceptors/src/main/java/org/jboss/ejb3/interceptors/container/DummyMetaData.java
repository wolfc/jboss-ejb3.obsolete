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
package org.jboss.ejb3.interceptors.container;

import java.lang.annotation.Annotation;

import org.jboss.metadata.spi.MetaData;
import org.jboss.metadata.spi.scope.ScopeLevel;
import org.jboss.metadata.spi.signature.Signature;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
class DummyMetaData implements MetaData
{

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getAnnotation(java.lang.Class)
    */
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getAnnotations()
    */
   public Annotation[] getAnnotations()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getComponentMetaData(org.jboss.metadata.spi.signature.Signature)
    */
   public MetaData getComponentMetaData(Signature signature)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getLocalAnnotations()
    */
   public Annotation[] getLocalAnnotations()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getLocalMetaData()
    */
   public Object[] getLocalMetaData()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getMetaData()
    */
   public Object[] getMetaData()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getMetaData(java.lang.Class)
    */
   public <T> T getMetaData(Class<T> type)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getMetaData(java.lang.String)
    */
   public Object getMetaData(String name)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getMetaData(java.lang.String, java.lang.Class)
    */
   public <T> T getMetaData(String name, Class<T> type)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getScopeMetaData(org.jboss.metadata.spi.scope.ScopeLevel)
    */
   public MetaData getScopeMetaData(ScopeLevel level)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#getValidTime()
    */
   public long getValidTime()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#isAnnotationPresent(java.lang.Class)
    */
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#isEmpty()
    */
   public boolean isEmpty()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#isMetaDataPresent(java.lang.Class)
    */
   public boolean isMetaDataPresent(Class<?> type)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#isMetaDataPresent(java.lang.String)
    */
   public boolean isMetaDataPresent(String name)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.MetaData#isMetaDataPresent(java.lang.String, java.lang.Class)
    */
   public boolean isMetaDataPresent(String name, Class<?> type)
   {
      // TODO Auto-generated method stub
      return false;
   }

}
