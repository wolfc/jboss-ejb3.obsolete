/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.ejb3.metadata.jpa.spec;

import java.net.URL;
import java.util.List;

import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.jboss.metadata.javaee.support.AbstractMappedMetaData;

/**
 * The persistence xml meta data.
 * 
 * Currently a wrapper around the Hibernate metadata.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 67604 $
 */
public class PersistenceUnitsMetaData extends AbstractMappedMetaData<PersistenceUnitMetaData>
{
   private static final long serialVersionUID = 1L;
   
   /**
    * The persistence unit root is shared by all persistence units defined
    * in this persistence xml.
    */
   private URL persistenceUnitRootUrl;

   public PersistenceUnitsMetaData(URL persistenceUnitRootUrl, List<PersistenceMetadata> list)
   {
      super("persistence unit meta data");
      
      assert persistenceUnitRootUrl != null : "persistenceUnitRootUrl is null";
      
      this.persistenceUnitRootUrl = persistenceUnitRootUrl;
      
      for(PersistenceMetadata old : list)
      {
         add(new PersistenceUnitMetaData(this, old));
      }
   }
   
   /**
    * The URL to the root of the persistence units defined. (JPA 6.2)
    * @see javax.persistence.PersistenceUnitInfo#getPersistenceUnitRootUrl
    */
   public URL getPersistenceUnitRootUrl()
   {
      return persistenceUnitRootUrl;
   }
}
