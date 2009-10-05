/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.metadata.impl.javaee;

import org.jboss.ejb3.metadata.spi.javaee.InjectionTargetMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * InjectionTargetMetadataImpl
 * 
 * Represents the metadata for an injection-target
 * 
 * @see InjectionTargetMetaData
 * 
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class InjectionTargetMetadataImpl implements InjectionTargetMetaData
{

   /**
    * Delegate from which this {@link InjectionTargetMetadataImpl} was constructed
    */
   private ResourceInjectionTargetMetaData delegate;

   /**
    * Fully qualified classname of the injection-target
    */
   private String injectionTargetClassname;

   /**
    * Name of the injection-target
    */
   private String injectionTargetName;

   /**
    * Constructs an {@link InjectionTargetMetadataImpl} from a {@link ResourceInjectionTargetMetaData}
    * 
    * @param injectionTarget
    */
   public InjectionTargetMetadataImpl(ResourceInjectionTargetMetaData injectionTarget)
   {
      this.initialize(injectionTarget);
   }

   /**
    * Initializes this {@link InjectionTargetMetadataImpl} from the state in {@link ResourceInjectionTargetMetaData}
    * 
    * @param injectionTarget
    */
   private void initialize(ResourceInjectionTargetMetaData injectionTarget)
   {
      //set the delegate
      this.delegate = injectionTarget;

      this.injectionTargetClassname = this.delegate.getInjectionTargetClass();
      this.injectionTargetName = this.delegate.getInjectionTargetName();
   }

   /**
    *  @see InjectionTargetMetaData#getInjectionTargetClassname() 
    */
   public String getInjectionTargetClassname()
   {
      return this.injectionTargetClassname;
   }

   /**
    * @see InjectionTargetMetaData#getInjectionTargetName()
    */
   public String getInjectionTargetName()
   {
      return this.injectionTargetName;
   }

   /**
    * @see InjectionTargetMetaData#setInjectionTargetClassname(String)
    */
   public void setInjectionTargetClassname(String classname)
   {
      this.injectionTargetClassname = classname;
   }

   /**
    * @see InjectionTargetMetaData#setInjectionTargetName(String)
    */
   public void setInjectionTargetName(String targetName)
   {
      this.injectionTargetName = targetName;
   }

}
