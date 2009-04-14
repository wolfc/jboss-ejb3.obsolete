/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.embedded.test.dsl;

import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.ejb3.embedded.dsl.Attachment;
import org.jboss.metadata.jpa.spec.PersistenceMetaData;
import org.jboss.metadata.jpa.spec.PersistenceUnitMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class PersistenceBuilder extends PersistenceMetaData
   implements Attachment<PersistenceMetaData>
{
   private static final long serialVersionUID = 1L;

   public static PersistenceBuilder persistence()
   {
      return new PersistenceBuilder();
   }
   
   public static PersistenceBuilder persistence(PersistenceUnitMetaData... units)
   {
      PersistenceBuilder builder = new PersistenceBuilder();
      builder.setPersistenceUnits(Arrays.asList(units));
      return builder;
   }

   public PersistenceMetaData getAttachment()
   {
      return this;
   }

   public Class<PersistenceMetaData> getAttachmentType()
   {
      return PersistenceMetaData.class;
   }
   
   public PersistenceBuilder unit(PersistenceUnitMetaData unit)
   {
      if(getPersistenceUnits() == null)
         setPersistenceUnits(new ArrayList<PersistenceUnitMetaData>());
      getPersistenceUnits().add(unit);
      return this;
   }
}
