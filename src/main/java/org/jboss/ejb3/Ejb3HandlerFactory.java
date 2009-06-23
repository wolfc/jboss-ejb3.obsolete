/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3;

import java.net.URL;

import javassist.bytecode.ClassFile;

import org.jboss.metadata.ejb.jboss.JBossMetaData;

public abstract class Ejb3HandlerFactory
{
   public abstract Ejb3Handler createHandler(ClassFile cf) throws Exception;

   private static class DDFactory extends Ejb3HandlerFactory
   {
      private JBossMetaData dd;
      private Ejb3Deployment di;

      public DDFactory(Ejb3Deployment di) throws Exception
      {
         this.di = di;
         this.dd = di.getMetaData();
      }
      
      public Ejb3Handler createHandler(ClassFile cf) throws Exception
      {
         return new Ejb3DescriptorHandler(di, cf, dd);
      }
   }

   private static class AnnotationFactory extends Ejb3HandlerFactory
   {
      private Ejb3Deployment di;

      public AnnotationFactory(Ejb3Deployment di) throws Exception
      {
         this.di = di;
      }


      public Ejb3Handler createHandler(ClassFile cf) throws Exception
      {
         return new Ejb3AnnotationHandler(di, cf);
      }
   }

   public static Ejb3HandlerFactory getInstance(Ejb3Deployment di) throws Exception
   {
      URL ddResource = di.getDeploymentUnit().getEjbJarXml();
      
      URL jbossDdResource = di.getDeploymentUnit().getJbossXml();

      if (ddResource == null && jbossDdResource == null)
         return new AnnotationFactory(di);
      else
         return new DDFactory(di);
   }
}
