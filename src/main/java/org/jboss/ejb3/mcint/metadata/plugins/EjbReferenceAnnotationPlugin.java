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
package org.jboss.ejb3.mcint.metadata.plugins;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.ejb3.common.resolvers.spi.EjbReference;
import org.jboss.ejb3.common.resolvers.spi.EjbReferenceResolver;
import org.jboss.kernel.plugins.annotations.FieldAnnotationPlugin;
import org.jboss.logging.Logger;
import org.jboss.reflect.spi.FieldInfo;

/**
 * EjbReferenceAnnotationPlugin
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EjbReferenceAnnotationPlugin extends FieldAnnotationPlugin<EJB>
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(EjbReferenceAnnotationPlugin.class);

   /**
    * The default naming context to use, if none is specified
    */
   private static final Context defaultNamingContext;
   static
   {
      try
      {
         defaultNamingContext = new InitialContext();
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Could not create the default naming context", e);
      }
   }

   // --------------------------------------------------------------------------------||
   // Instance Members ---------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * The resolver to use
    */
   private EjbReferenceResolver resolver;

   /**
    * The Naming Context for Lookups
    */
   private Context namingContext;

   // --------------------------------------------------------------------------------||
   // Constructor --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Sole Constructor
    */
   public EjbReferenceAnnotationPlugin(EjbReferenceResolver resolver)
   {
      this(resolver, defaultNamingContext);
   }

   /**
    * Sole Constructor
    */
   public EjbReferenceAnnotationPlugin(EjbReferenceResolver resolver, Context context)
   {
      // Call super
      super(EJB.class);

      // Precondition check
      assert resolver != null : "Resolver is required, but not specified.";
      assert context != null : "Naming context is required, but not specified.";

      // Set properties
      this.setResolver(resolver);
      this.setNamingContext(context);
   }

   // --------------------------------------------------------------------------------||
   // Overridden Implementations -----------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   /**
    * Create @EJB value meta data.
    *
    * @param annotation 
    * @return @EJB metadata
    */
   @Override
   protected ValueMetaData createValueMetaData(FieldInfo propInfo, EJB annotation)
   {
      // Get properties from the annotation
      String beanName = annotation.beanName();
      String beanInterface = annotation.beanInterface().getName();
      String mappedName = annotation.mappedName();

      // Supply beanInterface from reflection if not explicitly-defined
      if (beanInterface == null || beanInterface.equals(Object.class.getName()))
      {
         String reflectType = propInfo.getType().getName();
         beanInterface = reflectType;
      }

      // Create a reference
      EjbReference reference = new EjbReference(beanName, beanInterface, mappedName);
      log.debug("Found @EJB reference " + reference);

      // Return a new ValueMetaData w/ the reference
      return new AbstractEjbReferenceValueMetadata(this.getResolver(), reference, this.getNamingContext());
   }

   // --------------------------------------------------------------------------------||
   // Accessors / Mutators -----------------------------------------------------------||
   // --------------------------------------------------------------------------------||  

   public EjbReferenceResolver getResolver()
   {
      return resolver;
   }

   public void setResolver(EjbReferenceResolver resolver)
   {
      this.resolver = resolver;
   }

   protected Context getNamingContext()
   {
      return namingContext;
   }

   protected void setNamingContext(Context namingContext)
   {
      this.namingContext = namingContext;
   }

}
