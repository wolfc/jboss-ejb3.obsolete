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
package org.jboss.ejb3.metadata;

import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.scope.ScopeKey;
import org.jboss.metadata.spi.signature.Signature;

/**
 * The EJBMetaDataLoader visits all ComponentMetaDataLoaderFactories
 * until it finds one that will create a MetaDataRetrieval for the given
 * signature.
 * 
 * For example it could ask for a retrieval given the signature
 * of an interceptor class. In that case an interceptor meta data loader must
 * be instantiated using the interceptor meta data from the bean meta data.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public interface ComponentMetaDataLoaderFactory<M>
{
   /**
    * @param metaData       meta data to find the component in
    * @param signature      the signature of the sub-component
    * @param key
    * @param classLoader
    * @return               the retrieval for the sub-component or null if nothing sensible is found
    */
   MetaDataRetrieval createComponentMetaDataRetrieval(M metaData, Signature signature, ScopeKey key, ClassLoader classLoader);
}
