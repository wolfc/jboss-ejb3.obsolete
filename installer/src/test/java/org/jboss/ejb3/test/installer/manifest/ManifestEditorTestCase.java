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
package org.jboss.ejb3.test.installer.manifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

import org.jboss.ejb3.installer.manifest.ManifestEditor;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * ManifestEditorTestCase
 * 
 * Test Cases for the ManifestEditor
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ManifestEditorTestCase
{

   //------------------------------------------------------------------------||
   // Class Members ---------------------------------------------------------||
   //------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(ManifestEditorTestCase.class);

   private static final String FILE_NAME_JAR_PARENT = "target/test";

   private static final String FILE_NAME_JAR_FILE = "ManifestEditorTest.jar";

   private static final String FILE_NAME_MANIFEST_TEMPLATE = "manifest_template";

   private static final String MANIFEST_ENTRY_CLASS_PATH = "Class-Path";

   private static final String DELIMTER_CLASS_PATH = " ";

   private static final String CLASSPATH_ENTRY_1 = "default1.jar";

   private static final String CLASSPATH_ENTRY_2 = "default2.jar";

   private static final String CLASSPATH_ENTRY_3 = "default3.jar";

   private static final String NEW_CLASSPATH_ENTRY_1 = "added1.jar";

   private static final String NEW_CLASSPATH_ENTRY_2 = "added2.jar";

   //------------------------------------------------------------------------||
   // Instance Members ------------------------------------------------------||
   //------------------------------------------------------------------------||

   private JarFile jarFile;

   //------------------------------------------------------------------------||
   // Lifecycle Methods -----------------------------------------------------||
   //------------------------------------------------------------------------||

   @Before
   public void beforeTest() throws Throwable
   {
      /*
       * Create a new JAR file for testing
       */

      // Make the parent directory
      File parent = new File(FILE_NAME_JAR_PARENT);
      assert parent.mkdir() || parent.exists() : "Could not create " + FILE_NAME_JAR_PARENT;

      // Create the new ZIP file
      File newFile = new File(FILE_NAME_JAR_PARENT, FILE_NAME_JAR_FILE);
      createZipWithManifest(newFile, FILE_NAME_MANIFEST_TEMPLATE);

      // Set the JAR File
      this.jarFile = new JarFile(newFile);
   }

   @After
   public void afterTest() throws Throwable
   {
      this.jarFile = null;
   }

   //------------------------------------------------------------------------||
   // Internal Helper Methods -----------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Creates a new ZIP file with a manifest, using contents from the specified
    * manifest template
    */
   private static void createZipWithManifest(File file, String manifestTemplateFilename) throws Throwable
   {
      // Initialize
      OutputStream fos = null;
      ZipOutputStream zos = null;

      try
      {
         // Log
         log.info("Writing new Manifest-only JAR: " + file.getAbsolutePath());

         // Open up the Streams
         fos = new FileOutputStream(file);
         zos = new ZipOutputStream(fos);

         // Put a new Manifest entry in
         String manifestTarget = "META-INF/MANIFEST.MF";
         final ZipEntry empty = new ZipEntry(manifestTarget);
         zos.putNextEntry(empty);
         log.info("Put manifest: " + manifestTarget);

         // Get the template manifest
         final URL manifestTemplateUrl = Thread.currentThread().getContextClassLoader().getResource(
               manifestTemplateFilename);
         final File manifestTemplate = new File(manifestTemplateUrl.toURI());
         final InputStream fis = new FileInputStream(manifestTemplate);

         // Write to the manifest entry from the template
         int len;
         byte[] buf = new byte[1024];
         while ((len = fis.read(buf)) > 0)
         {
            zos.write(buf, 0, len);
         }

         // Close the entry
         zos.closeEntry();

         // Log
         log.info("Wrote " + manifestTarget + " from template " + manifestTemplateFilename);

      }
      catch (IOException ioe)
      {
         log.error("Error in creating new JAR File w/ Manifest", ioe);
         throw ioe;
      }
      /*
       * Close up everything
       */
      finally
      {
         try
         {
            if (zos != null)
            {
               zos.close();
            }
         }
         catch (IOException ioe)
         {
            // Swallow
         }
         try
         {
            if (fos != null)
            {
               fos.close();
            }
         }
         catch (IOException ioe)
         {
            // Swallow
         }
      }
   }

   //------------------------------------------------------------------------||
   // Tests -----------------------------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * The control test.  Ensures that the Class-Path element of the JAR
    * has expected values
    */
   @Test
   public void testJarManifestControl() throws Throwable
   {
      // Read the Class-Path out 
      final Collection<String> classPathEntries = this.getClassPathFromManifest();

      // Ensure all entries are there
      TestCase.assertEquals("CP Entries in control was not expected size", 3, classPathEntries.size());
      String errorNotContainsExpected = "CP Entries in control does not contain all expected elements";
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(CLASSPATH_ENTRY_1));
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(CLASSPATH_ENTRY_2));
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(CLASSPATH_ENTRY_3));
   }

   /**
    * Ensures that entries may be removed from the Manifest CP 
    * 
    * @throws Throwable
    */
   @Test
   public void testEntriesRemovedFromClassPath() throws Throwable
   {
      // Get the JAR
      JarFile jar = jarFile;

      // Make an editor
      ManifestEditor editor = new ManifestEditor(jar);

      // Declare elements to remove
      Set<String> elementsToRemove = new HashSet<String>();
      elementsToRemove.add(CLASSPATH_ENTRY_1);
      elementsToRemove.add(CLASSPATH_ENTRY_3);

      // Remove
      editor.removeEntriesFromClassPath(elementsToRemove);

      // Read the Class-Path out 
      final Collection<String> classPathEntries = this.getClassPathFromManifest();

      // Ensure all entries are there
      TestCase.assertEquals("CP Entries after removal was not expected size", 1, classPathEntries.size());
      TestCase.assertTrue("CP Entries after removal did not contain " + CLASSPATH_ENTRY_2, classPathEntries
            .contains(CLASSPATH_ENTRY_2));
   }

   /**
    * Ensures that entries may be added to the manifest CP
    * 
    * @throws Throwable
    */
   @Test
   public void testEntriesAddedToClassPath() throws Throwable
   {
      // Get the JAR
      JarFile jar = jarFile;

      // Make an editor
      ManifestEditor editor = new ManifestEditor(jar);

      // Declare elements to add
      Set<String> elementsToAdd = new HashSet<String>();
      elementsToAdd.add(NEW_CLASSPATH_ENTRY_1);
      elementsToAdd.add(NEW_CLASSPATH_ENTRY_2);

      // Remove
      editor.addEntriesToClassPath(elementsToAdd);

      // Read the Class-Path out 
      final Collection<String> classPathEntries = this.getClassPathFromManifest();

      // Ensure all entries are there
      TestCase.assertEquals("CP Entries after insertions was not expected size", 5, classPathEntries.size());
      String errorNotContainsExpected = "CP Entries after insertions does not contain all expected elements";
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(NEW_CLASSPATH_ENTRY_1));
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(NEW_CLASSPATH_ENTRY_2));
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(CLASSPATH_ENTRY_1));
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(CLASSPATH_ENTRY_2));
      TestCase.assertTrue(errorNotContainsExpected, classPathEntries.contains(CLASSPATH_ENTRY_3));
   }

   //------------------------------------------------------------------------||
   // Internal Helper Methods -----------------------------------------------||
   //------------------------------------------------------------------------||

   /**
    * Obtains an array of the CP entries from the Manifest
    */
   private Collection<String> getClassPathFromManifest() throws Throwable
   {
      // Get the JAR
      final JarFile jar = this.jarFile;

      // Get the Manifest
      final Manifest manifest = jar.getManifest();
      TestCase.assertNotNull("The manifest in the test JAR could not be found", manifest);

      // Read the Class-Path out 
      final Attributes attributes = manifest.getMainAttributes();
      final String classPath = attributes.getValue(MANIFEST_ENTRY_CLASS_PATH);
      TestCase.assertNotNull(classPath);
      log.info("Got " + MANIFEST_ENTRY_CLASS_PATH + ": " + classPath);

      // Split
      final String[] classPathEntries = classPath.trim().split(DELIMTER_CLASS_PATH);

      // Add to new Collection
      Collection<String> cp = new ArrayList<String>();
      for (String classPathEntry : classPathEntries)
      {
         cp.add(classPathEntry);
      }

      // Return
      return cp;
   }

}
