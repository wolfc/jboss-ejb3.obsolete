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
package org.jboss.ejb3.dependency;

import java.io.Serializable;
import java.util.Iterator;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.ejb3.javaee.JavaEEComponent;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;

/**
 * Note that this one is only used for an ejb link which doesn't have a module
 * specified.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public class EjbLinkDemandMetaData extends JBossObject
   implements DemandMetaData, Serializable
{
   private static final long serialVersionUID = 1L;

   /** The demand */
   private ObjectName demand;

   /** When the dependency is required */
   private ControllerState whenRequired = ControllerState.DESCRIBED;

   /**
    *
    * @param component      the component which needs the enterprise bean
    * @param ejbName        the name of the enterprise bean to find
    */
   public EjbLinkDemandMetaData(JavaEEComponent component, String ejbName)
   {
      try
      {
         this.demand = new ObjectName(component.createObjectName(null, ejbName));
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Object getDemand()
   {
      return demand;
   }

   public ControllerState getWhenRequired()
   {
      return whenRequired;
   }

   public void describeVisit(MetaDataVisitor vistor)
   {
      vistor.describeVisit(this);
   }

   public Iterator<? extends MetaDataVisitorNode> getChildren()
   {
      return null;
   }

   public void initialVisit(MetaDataVisitor visitor)
   {
      KernelControllerContext context = visitor.getControllerContext();
      DependencyItem item = new EjbLinkDemandDependencyItem(context.getName());
      visitor.addDependency(item);
      visitor.initialVisit(this);
   }

   @Override
   public void toString(JBossStringBuilder buffer)
   {
      buffer.append("demand=").append(demand);
      if (whenRequired != null)
         buffer.append(" whenRequired=").append(whenRequired.getStateString());
   }

   @Override
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(demand);
   }

   private class EjbLinkDemandDependencyItem extends AbstractDependencyItem
   {
      public EjbLinkDemandDependencyItem(Object name)
      {
         super(name, null, whenRequired, null);
      }

      @Override
      public boolean resolve(Controller controller)
      {
         for(ControllerContext context : controller.getContextsByState(ControllerState.INSTALLED))
         {
            try
            {
               ObjectName otherName = new ObjectName(context.getName().toString());

               if(demand.apply(otherName))
               {
                  setIDependOn(context.getName());
                  addDependsOnMe(controller, context);
                  setResolved(true);
                  return isResolved();
               }
            }
            catch (MalformedObjectNameException e)
            {
               // ignore this context
            }
         }
         setResolved(false);
         return isResolved();
      }

      @Override
      public void toString(JBossStringBuilder buffer)
      {
         super.toString(buffer);
         buffer.append(" demand=").append(demand.getCanonicalName());
      }

      @Override
      public void toShortString(JBossStringBuilder buffer)
      {
         buffer.append(getName()).append(" demands ").append(demand.getCanonicalName());
      }

      @Override
      public String toHumanReadableString()
      {
         StringBuilder builder = new StringBuilder();
         builder.append("Demands '").append(demand.getCanonicalName());
         return builder.toString();
      }
   }

   /**
    * @see DemandMetaData#getTargetState()
    *
    * @return Return the default {@link ControllerState#INSTALLED}
    */
   public ControllerState getTargetState()
   {
      return ControllerState.INSTALLED;
   }
}
