/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.metadata.plugins.loader;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.jboss.metadata.plugins.loader.BasicMetaDataLoader;
import org.jboss.metadata.spi.retrieval.AnnotationItem;
import org.jboss.metadata.spi.retrieval.AnnotationsItem;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.Signature;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class ClassMetaDataLoader extends BasicMetaDataLoader
{
   private static final Logger log = Logger.getLogger(ClassMetaDataLoader.class);

   /** Component cache */
   private Map<Signature, MetaDataRetrieval> cache = new ConcurrentHashMap<Signature, MetaDataRetrieval>();
   
   protected abstract MetaDataRetrieval createComponentMetaDataRetrieval(Signature signature);
   
   protected ClassMetaDataLoader(ScopeKey key)
   {
      super(key);
   }
   
   @Override
   public MetaDataRetrieval getComponentMetaDataRetrieval(Signature signature)
   {
      MetaDataRetrieval retrieval = cache.get(signature);
      if (retrieval != null)
         return retrieval;
      
      retrieval = createComponentMetaDataRetrieval(signature);
      
      if(retrieval != null)
         cache.put(signature, retrieval);
      
      return retrieval;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.retrieval.MetaDataRetrieval#isEmpty()
    */
   public boolean isEmpty()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.jboss.metadata.spi.retrieval.MetaDataRetrieval#retrieveAnnotations()
    */
   public AnnotationsItem retrieveAnnotations()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   @Override
   public <T extends Annotation> AnnotationItem<T> retrieveAnnotation(Class<T> annotationType)
   {
      // Resources, EJBs etc
      return null;
   }
}
