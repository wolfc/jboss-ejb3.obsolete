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

import java.rmi.dgc.VMID;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * Maintains an administration of all EJB3 container available.
 * 
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
public class Ejb3Registry
{
   private static final Logger log = Logger.getLogger(Ejb3Registry.class);

   private static Map<String, Container> containers = new HashMap<String, Container>();
   private static Map<String, Container> clusterContainers = new HashMap<String, Container>();
   
   private static final VMID vmid = new VMID();

   /**
    * Find a potential container.
    * 
    * @param oid    the canonical object name of the container
    * @return       the container or null if not found
    */
   public static Container findContainer(String oid)
   {
      return containers.get(oid);
   }
   
   public static VMID getVMID()
   {
      return vmid;
   }

   /**
    * Reports the existance of a container.
    * 
    * @param container  the container to check
    * @return           true if found, false otherwise
    */
   public static boolean hasContainer(Container container)
   {
      return hasContainer(oid(container));
   }
   
   /**
    * Reports the existance of a container.
    * 
    * @param oid    the canonical object name of the container
    * @return       true if found, false otherwise
    */
   private static boolean hasContainer(String oid)
   {
      return containers.containsKey(oid);
   }
   
   private static final String oid(Container container)
   {
      return container.getObjectName().getCanonicalName();
   }
   
   public static boolean hasClusterContainer(String oid)
   {
      return clusterContainers.containsKey(oid);
   }
   
   public static final String guid(Container container, VMID vmid)
   {
      return container.getObjectName().getCanonicalName() + ",VMID=" + vmid;
   }
   
   public static final String guid(Container container)
   {
      return guid(container, vmid);
   }
   
   public static final String clusterUid(Container container)
   {  
      if (container.isClustered())
        return container.getObjectName().getCanonicalName() + ",Partition=" + ((EJBContainer)container).getPartitionName();
     
      return container.getObjectName().getCanonicalName();
   }
   
   public static final String clusterUid(String oid, String partitionName)
   {
      return oid + ",Partition=" + partitionName;
   }
   
   /**
    * Registers a container.
    * 
    * @param container              the container to register
    * @throws IllegalStateException if the container is already registered
    */
   public static void register(Container container)
   {
      String guid = guid(container);
      if(hasContainer(guid))
         throw new IllegalStateException("Container " + guid + " + is already registered");
      containers.put(guid, container);
      
      if (container.isClustered())
         clusterContainers.put(clusterUid(container), container);
      
      log.debug("Registered container " + guid);
   }

   /**
    * Unregisters a container.
    * 
    * @param container              the container to unregister
    * @throws IllegalStateException if the container is not registered
    */
   public static void unregister(Container container)
   {
      String guid = guid(container);
      if(!hasContainer(guid))
         throw new IllegalStateException("Container " + guid + " + is not registered");
      containers.remove(guid);
      
      if (container.isClustered())
         clusterContainers.remove(clusterUid(container));
      
      log.debug("Unregistered container " + guid);
   }

   /**
    * Returns the container specified by the given GUID.
    * Never returns null.
    * 
    * @param guid                   the GUID
    * @return                       the container
    * @throws IllegalStateException if the container is not registered
    */
   public static Container getContainer(String guid)
   {
      if(!hasContainer(guid))
         throw new IllegalStateException("Container " + guid + " is not registered");
      
      return containers.get(guid);
   }
   
   /**
    * Returns the container specified by the given canocical object name.
    * Never returns null.
    * 
    * @param oid                    the canonical object name of the container
    * @return                       the container
    * @throws IllegalStateException if the container is not registered
    */
   public static Container getClusterContainer(String clusterUid)
   {
      Container container = clusterContainers.get(clusterUid);
      if(container == null)
         throw new IllegalStateException("Container " + clusterUid + " is not registered " + clusterContainers);
      
      return container;
   }

   /**
    * Returns the container specified by the given canonical object name
    * which is expected to be of the given type.
    * Never returns null.
    * 
    * @param oid                    the canonical object name of the container
    * @param type                   the container type
    * @return                       the container
    * @throws IllegalStateException if the container is not registered
    */
   @SuppressWarnings("unchecked")
   public static <T extends Container> T getContainer(String oid, Class<T> type)
   {
      return (T) getContainer(oid);
   }
   
   /**
    * Returns an unmodifiable collection of the registered containers.
    * 
    * @return   the containers
    */
   public static Collection<Container> getContainers()
   {
      return Collections.unmodifiableCollection(containers.values());
   }
}
