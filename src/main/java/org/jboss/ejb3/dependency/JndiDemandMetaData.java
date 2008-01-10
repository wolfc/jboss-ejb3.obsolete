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

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.beans.metadata.spi.DemandMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;

/**
 * A DemandMetaData/DependencyItem implementation for a jndiName/ClassLoader
 * pair. This is a simple lookup into the default IntialContext with the
 * JndiDemandMetaData class loader set as the TCL.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class JndiDemandMetaData extends JBossObject
   implements DemandMetaData, Serializable
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger log = Logger.getLogger(JndiDemandMetaData.class);
   
   /** The demand jndi name */
   private String jndiName;
   /** The jndi name lookup value */
   private Object demand;
   /** The container class loader to use during lookup */
   private ClassLoader loader;
   private ControllerState whenRequired = ControllerState.INSTALLED;

   /**
    * Create a demand for a jndi name lookup using the given class loader.
    * 
    * @param jndiName - the name to lookup
    * @param loader - the ClassLoader to use as the TCL during lookup.
    */
   public JndiDemandMetaData(String jndiName, ClassLoader loader)
   {
      this.jndiName = jndiName;
      this.loader = loader;
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
      DependencyItem item = new JndiDemandDependencyItem(context.getName());
      visitor.addDependency(item);
      visitor.initialVisit(this);
   }
   
   @Override
   public void toString(JBossStringBuilder buffer)
   {
      buffer.append("demand=").append(jndiName);
      buffer.append(" whenRequired=").append(whenRequired.getStateString());
   }
   
   @Override
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(jndiName);
   }

   private class JndiDemandDependencyItem extends AbstractDependencyItem
   {
      public JndiDemandDependencyItem(Object name)
      {
         super(name, null, whenRequired, null);
      }
      
      @Override
      public boolean resolve(Controller controller)
      {
         ClassLoader tcl = Thread.currentThread().getContextClassLoader();
         try
         {
            Thread.currentThread().setContextClassLoader(loader);
            InitialContext ctx = new InitialContext();
            demand = ctx.lookup(jndiName);
            setResolved(true);
            return isResolved();
         }
         catch(NameNotFoundException e)
         {
            // ignore
         }
         catch(Throwable ignored)
         {
            if (log.isTraceEnabled())
               log.trace("Unexpected error", ignored);
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(tcl);            
         }
         setResolved(false);
         return isResolved();
      }
      
      @Override
      public void toString(JBossStringBuilder buffer)
      {
         super.toString(buffer);
         buffer.append(" demand=").append(jndiName);
      }
      
      @Override
      public void toShortString(JBossStringBuilder buffer)
      {
         buffer.append(getName()).append(" demands ").append(jndiName);
      }

      @Override
      public String toHumanReadableString()
      {
         StringBuilder builder = new StringBuilder();
         builder.append("Demands '").append(jndiName);
         return builder.toString();
      }
   }
}
