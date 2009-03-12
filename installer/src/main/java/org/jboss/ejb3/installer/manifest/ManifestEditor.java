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
package org.jboss.ejb3.installer.manifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.logging.Logger;

/**
 * ManifestEditor
 * 
 * Configurable utility to alter the manifest of a given JAR
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ManifestEditor
{
   //------------------------------------------------------------------------||
   // Class Members ---------------------------------------------------------||
   //------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ManifestEditor.class);

   private static final String ENTRY_NAME_CLASS_PATH = "Class-Path";

   private static final String DELIMITER_CLASS_PATH_ENTRIES = " ";

   //------------------------------------------------------------------------||
   // Instance Members ------------------------------------------------------||
   //------------------------------------------------------------------------||

   private Manifest manifest;

   //------------------------------------------------------------------------||
   // Constructors ----------------------------------------------------------||
   //------------------------------------------------------------------------||

   public ManifestEditor(final JarFile jar)
   {
      // Precondition check
      assert jar != null : "JAR must be specified";

      // Get the manifest for the JAR
      Manifest manifest = null;
      try
      {
         manifest = jar.getManifest();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not get manifest from JAR: " + jar, e);
      }

      // If there is not Manifest, make a new one
      if (manifest == null)
      {
         log.debug("Created new empty manifest for JAR: " + jar);
         manifest = new Manifest();
      }

      // Set state
      this.setManifest(manifest);
   }

   //------------------------------------------------------------------------||
   // Functional Methods ----------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Adds the specified entries to the Class-Path of the manifest, 
    * additionally creating "Class-Path" if it does not exist
    */
   public void addEntriesToClassPath(final Set<String> entriesToAdd)
   {
      // Get the Manifest
      final Manifest manifest = this.getManifest();

      // Get the Class-Path
      final String classPath = manifest.getMainAttributes().getValue(ENTRY_NAME_CLASS_PATH);

      // Get the Class-Path entries
      final String[] classPathEntries = classPath == null ? new String[]
      {} : classPath.trim().split(DELIMITER_CLASS_PATH_ENTRIES);

      // Add existing entries to the ones we'll add (and strip out duplicates)
      for (String classPathEntry : classPathEntries)
      {
         entriesToAdd.add(classPathEntry);
      }

      // Get the new CP
      final String newCp = this.flattenClassPathEntries(entriesToAdd);
      log.info("Setting the " + ENTRY_NAME_CLASS_PATH + " to: " + newCp);
      manifest.getMainAttributes().putValue(ENTRY_NAME_CLASS_PATH, newCp);
   }

   /**
    * Removes, if they exist, attributes from a Class-Path entry 
    */
   public void removeEntriesFromClassPath(final Set<String> entriesToRemove)
   {
      // Get the Manifest
      final Manifest manifest = this.getManifest();

      // Get the Class-Path
      final String classPath = manifest.getMainAttributes().getValue(ENTRY_NAME_CLASS_PATH);

      // If not supplied, there's nothing to remove, so quit
      if (classPath == null || classPath.length() == 0)
      {
         return;
      }

      // Split
      final String[] classPathEntries = classPath.trim().split(DELIMITER_CLASS_PATH_ENTRIES);

      // Filter out exclusions from a new Collection
      final Collection<String> newClassPathEntries = new ArrayList<String>();
      for (final String classPathEntry : classPathEntries)
      {
         if (!entriesToRemove.contains((classPathEntry)))
         {
            newClassPathEntries.add(classPathEntry);
         }
         else
         {
            log.info("Removing from " + ENTRY_NAME_CLASS_PATH + ": " + classPathEntry);
         }
      }

      // Re-set the Class-Path
      final String newCp = this.flattenClassPathEntries(newClassPathEntries);
      log.info("Setting the " + ENTRY_NAME_CLASS_PATH + " to: " + newCp);
      manifest.getMainAttributes().putValue(ENTRY_NAME_CLASS_PATH, newCp);
   }

   //------------------------------------------------------------------------||
   // Internal Helper Methods -----------------------------------------------||
   //------------------------------------------------------------------------||

   private String flattenClassPathEntries(Collection<String> classPathEntries)
   {
      // Flatten
      final StringBuffer sb = new StringBuffer();
      for (final String newEntry : classPathEntries)
      {
         sb.append(newEntry.trim());
         sb.append(DELIMITER_CLASS_PATH_ENTRIES);
      }

      // Re-set the Class-Path
      final String newCp = sb.toString().trim();

      // Return
      return newCp;
   }

   //------------------------------------------------------------------------||
   // Accessors / Mutators --------------------------------------------------||
   //------------------------------------------------------------------------||

   public Manifest getManifest()
   {
      return manifest;
   }

   private void setManifest(final Manifest manifest)
   {
      this.manifest = manifest;
   }

}
