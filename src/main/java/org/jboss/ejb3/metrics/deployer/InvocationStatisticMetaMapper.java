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
package org.jboss.ejb3.metrics.deployer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.ejb3.statistics.InvocationStatistics;
import org.jboss.ejb3.statistics.InvocationStatistics.TimeStatistic;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.spi.values.MetaMapper;

/**
 * InvocationStatisticMetaMapper
 *
 * {@link MetaMapper} for detyped EJB3 invocation statistics.  This
 * is to avoid dependence upon the EJB3 internal implementation
 * classes in possible remote JVMs or outside ClassLoaders.
 *
 * @author Jason T. Greene
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class InvocationStatisticMetaMapper extends MetaMapper<InvocationStatistics>
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   public static final CompositeMetaType TYPE;

   public static final CompositeMetaType METHOD_STATS_TYPE;

   private static String[] metaTypePropertyNames;

   /**
    * The composite type of the method stats; the value for the 
    * {@link InvocationStatisticMetaMapper#PROP_NAME_ROOT_METHOD_STATS}
    * property
    */
   private static MapCompositeMetaType VALUE_METHOD_STATS_MAP_TYPE;

   /**
    * Name of the "lastResetTime" property of the returned MetaValue
    */
   private static final String PROP_NAME_ROOT_LAST_RESET_TIME = "lastResetTime";

   /**
    * Name of the "methodStats" property of the returned MetaValue
    */
   private static final String PROP_NAME_ROOT_METHOD_STATS = "methodStats";

   /**
    * Name of the "count" property of the root MetaValue's "methodStats" property
    */
   private static final String PROP_NAME_METHODSTATS_COUNT = "count";

   /**
    * Name of the "minTime" property of the root MetaValue's "methodStats" property
    */
   private static final String PROP_NAME_METHODSTATS_MINTIME = "minTime";

   /**
    * Name of the "maxTime" property of the root MetaValue's "methodStats" property
    */
   private static final String PROP_NAME_METHODSTATS_MAXTIME = "maxTime";

   /**
    * Name of the "totalTime" property of the root MetaValue's "methodStats" property
    */
   private static final String PROP_NAME_METHODSTATS_TOTALTIME = "totalTime";

   static
   {
      final String[] methodItemNames =
      {PROP_NAME_METHODSTATS_COUNT, PROP_NAME_METHODSTATS_MINTIME, PROP_NAME_METHODSTATS_MAXTIME,
            PROP_NAME_METHODSTATS_TOTALTIME};
      final String[] methodItemDescriptions =
      {"the number of invocations", "the minimum invocation time", "the maximum invocation time",
            "the total invocation time",};
      final MetaType[] methodItemTypes =
      {SimpleMetaType.LONG, SimpleMetaType.LONG, SimpleMetaType.LONG, SimpleMetaType.LONG,};
      METHOD_STATS_TYPE = new ImmutableCompositeMetaType("MethodStatistics", "Method invocation statistics",
            methodItemNames, methodItemDescriptions, methodItemTypes);

      VALUE_METHOD_STATS_MAP_TYPE = new MapCompositeMetaType(METHOD_STATS_TYPE);

      metaTypePropertyNames = new String[]
      {PROP_NAME_ROOT_LAST_RESET_TIME, PROP_NAME_ROOT_METHOD_STATS};

      final String[] rootItemDescriptions =
      {"last time statistics were reset", "method statistics",};
      final MetaType[] rootItemTypes =
      {SimpleMetaType.LONG, VALUE_METHOD_STATS_MAP_TYPE};

      TYPE = new ImmutableCompositeMetaType("InvocationStatistics", "EJB3 invocation statistics",
            metaTypePropertyNames, rootItemDescriptions, rootItemTypes);
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.metatype.spi.values.MetaMapper#getMetaType()
    */
   @Override
   public MetaType getMetaType()
   {
      return TYPE;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.metatype.spi.values.MetaMapper#mapToType()
    */
   @Override
   public Type mapToType()
   {
      return InvocationStatistics.class;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.metatype.spi.values.MetaMapper#createMetaValue(org.jboss.metatype.api.types.MetaType, java.lang.Object)
    */
   @Override
   public MetaValue createMetaValue(MetaType metaType, InvocationStatistics object)
   {
      // Make the method map from the stats
      final Map<String, MetaValue> methodMap = new HashMap<String, MetaValue>();
      @SuppressWarnings("unchecked")
      final Map<Method, TimeStatistic> stats = (Map<Method, TimeStatistic>) object.getStats();
      if (stats != null)
      {
         final Set<Method> methods = stats.keySet();
         for (final Method method : methods)
         {
            // Get the underlying time stat for this method
            final TimeStatistic stat = stats.get(method);

            // Create a composite view of the stat's state
            final MapCompositeValueSupport cvs = new MapCompositeValueSupport(METHOD_STATS_TYPE);
            cvs.put(PROP_NAME_METHODSTATS_COUNT, SimpleValueSupport.wrap(stat.count));
            cvs.put(PROP_NAME_METHODSTATS_MAXTIME, SimpleValueSupport.wrap(stat.maxTime));
            cvs.put(PROP_NAME_METHODSTATS_MINTIME, SimpleValueSupport.wrap(stat.minTime));
            cvs.put(PROP_NAME_METHODSTATS_TOTALTIME, SimpleValueSupport.wrap(stat.totalTime));

            // Add the stat to the method map
            final String methodName = method.getName();
            methodMap.put(methodName, cvs);
         }
      }

      // Make a composite value for the returned MetaType
      final MapCompositeValueSupport root = new MapCompositeValueSupport(TYPE);

      // Set the properties
      root.put(PROP_NAME_ROOT_LAST_RESET_TIME, SimpleValueSupport.wrap(object.lastResetTime));
      root.put(PROP_NAME_ROOT_METHOD_STATS, new MapCompositeValueSupport(methodMap, VALUE_METHOD_STATS_MAP_TYPE));

      // Return
      return root;
   }

   /*
    * (non-Javadoc)
    * @see org.jboss.metatype.spi.values.MetaMapper#unwrapMetaValue(org.jboss.metatype.api.values.MetaValue)
    */
   @Override
   public InvocationStatistics unwrapMetaValue(MetaValue metaValue)
   {
      throw new UnsupportedOperationException(InvocationStatistics.class.getSimpleName() + " is a read-only property");
   }
}
