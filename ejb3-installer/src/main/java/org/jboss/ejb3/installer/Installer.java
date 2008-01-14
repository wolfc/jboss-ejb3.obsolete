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
package org.jboss.ejb3.installer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JBoss AS EJB3 Plugin Installer
 * 
 * A script to copy all requisite libraries to the local
 * filesystem, launch an ant build script to patch
 * an existing JBoss AS installation, perform
 * cleanup operations, and exit.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $$
 */
public class Installer
{

   // Class Members

   /*
    * Local temporary Directory
    */
   private static final String SYSTEM_PROPERTY_TMP_DIRECTORY = "java.io.tmpdir";

   /*
    * System-independent path separator
    */
   private static final String SYSTEM_PROPERTY_FILE_SEPARATOR = "file.separator";

   /*
    * Current Classpath
    */
   private static final String SYSTEM_PROPERTY_CLASS_PATH = "java.class.path";

   /*
    * Environment Property key for JBOSS_HOME
    */
   private static final String ENV_PROPERTY_JBOSS_HOME = "JBOSS_HOME";

   /*
    * Namespace of the installer
    */
   private static final String NAMESPACE_DIRECTORY_INSTALLER = "jbossas-ejb3-plugin-installer";

   /*
    * Location of the EJB3 Libraries
    */
   private static final String FILENAME_LIB_DIRECTORY = "lib";

   /*
    * Apache Ant executable
    */
   private static final String COMMAND_ANT = "ant";

   /*
    * Switch to set buildfile for Ant
    */
   private static final String SWITCH_ANT_BUILDFILE = "-f";

   /*
    * Filename of Ant Buildfile
    */
   private static final String FILENAME_BUILDFILE = "build-install-ejb3-plugin.xml";

   // Instance Members

   /*
    * Location of the JBoss AS 5.0.x Installation
    */
   private File jbossAsInstallationDirectory;

   /*
    * Location of the installation directory
    */
   private File installationDirectory;

   /*
    * Pointer to the installer JAR file
    */
   private JarFile installerJarFile;

   // Main

   /**
    * Main
    */
   public static void main(String... args)
   {
      // Obtain Arguments
      String jbossDir = null;
      try
      {
         jbossDir = args[0];
      }
      catch (ArrayIndexOutOfBoundsException aioobe)
      {
         throw new RuntimeException("Location of JBossAS 5.0.x Installation Directory must be first argument");
      }

      // Create Installer
      Installer installer = new Installer(jbossDir);

      // Install
      installer.install();
   }

   // Constructor
   public Installer(String jbossAsInstallationDirectory)
   {
      // Set JBoss AS Install Location
      this.setJbossAsInstallationDirectory(new File(jbossAsInstallationDirectory));
   }

   /**
    * Runs the installation process
    */
   public void install()
   {
      // Log
      System.out.println("\n*****************************************");
      System.out.println("|| JBossAS 5.0.x EJB3 Plugin Installer ||");
      System.out.println("*****************************************\n");
      System.out.println("Installing EJB3 Libraries to Temp Directory...");
      
      // Add Shutdown Hook
      Runtime.getRuntime().addShutdownHook(new Shutdown());

      // Ensure Installation is clean
      this.cleanup();

      // Ensure JBOSS_HOME exists
      this.ensureJbossHomeExists();

      // For each EJB3 Library
      for (JarEntry library : this.getAllEjb3Libraries())
      {
         // Copy to the installer temp directory
         this.copyFileFromJarToDirectory(this.getInstallerJarFile(), library, this.getInstallationDirectory());
      }

      // Copy the buildfile to the installer temp directory
      this.copyFileFromJarToDirectory(this.getInstallerJarFile(), this.getInstallerJarFile().getJarEntry(
            Installer.FILENAME_BUILDFILE), this.getInstallationDirectory());

      // Run Ant
      this.runAnt();
   }

   // Accessors / Mutators

   private File getJbossAsInstallationDirectory()
   {
      return jbossAsInstallationDirectory;
   }

   private void setJbossAsInstallationDirectory(File jbossAsInstallationDirectory)
   {
      this.jbossAsInstallationDirectory = jbossAsInstallationDirectory;
   }

   private File getInstallationDirectory()
   {
      // If installation directory has not yet been found
      if (this.installationDirectory == null)
      {
         // Obtain "tmp" directory
         File tempDir = new File(System.getProperty(Installer.SYSTEM_PROPERTY_TMP_DIRECTORY));
         // Obtain new namespace number temp directory
         File installerDir = new File(tempDir, Installer.NAMESPACE_DIRECTORY_INSTALLER);
         // Set as installation Directory
         this.setInstallationDirectory(installerDir);
         // Log
         System.out.println("JBoss AS 5.0.x Installation Directory: " + this.installationDirectory);
      }

      // Return
      return this.installationDirectory;
   }

   private void setInstallationDirectory(File installationDirectory)
   {
      this.installationDirectory = installationDirectory;
   }

   private void setInstallerJarFile(JarFile installerJarFile)
   {
      this.installerJarFile = installerJarFile;
   }

   private JarFile getInstallerJarFile()
   {
      // If not already specified
      if (this.installerJarFile == null)
      {
         // Obtain current JarFile
         JarFile jarFile = null;
         try
         {
            jarFile = new JarFile(System.getProperty(Installer.SYSTEM_PROPERTY_CLASS_PATH));
         }
         catch (IOException ioe)
         {
            throw new RuntimeException(ioe);
         }
         this.setInstallerJarFile(jarFile);
      }

      return this.installerJarFile;
   }

   // Internal Helper Methods

   /**
    * Returns all EJB3 Plugin libraries as references
    */
   private List<JarEntry> getAllEjb3Libraries()
   {
      // Initialize
      List<JarEntry> libraries = new ArrayList<JarEntry>();

      // Get Installer JAR File
      JarFile jarFile = this.getInstallerJarFile();

      // For each child in the "lib" directory, add to list of libraries
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements())
      {
         JarEntry entry = entries.nextElement();
         // Ensure it's in "lib" directory
         if (entry.getName().startsWith(Installer.FILENAME_LIB_DIRECTORY) && !entry.isDirectory())
         {
            libraries.add(entry);
         }
      }

      // Return
      return libraries;
   }

   /**
    * Executes Ant to run the installer buildfile, blocking the
    * Thread until the build process is complete
    */
   private void runAnt()
   {
      // Initialize
      Process antProcess = null;
      String buildfile = this.getInstallationDirectory() + System.getProperty(Installer.SYSTEM_PROPERTY_FILE_SEPARATOR)
            + Installer.FILENAME_BUILDFILE;

      // Construct the Process
      ProcessBuilder antProcessBuilder = new ProcessBuilder(Installer.COMMAND_ANT, Installer.SWITCH_ANT_BUILDFILE,
            buildfile);
      antProcessBuilder.redirectErrorStream(true);
      antProcessBuilder.environment().put(Installer.ENV_PROPERTY_JBOSS_HOME,
            this.getJbossAsInstallationDirectory().getAbsolutePath());

      try
      {
         // Start the Process
         System.out.println("Starting Ant> " + Installer.COMMAND_ANT + " " + Installer.SWITCH_ANT_BUILDFILE + " "
               + buildfile);
         antProcess = antProcessBuilder.start();

         // Capture the output
         Thread captureProcess = new CaptureProcess(antProcess);
         captureProcess.start();

         // Ensure proper completion, block until done
         int exitValue = antProcess.waitFor();
         if (exitValue == 0)
         {
            System.out.println("Ant Build Completed");
         }
         else
         {
            throw new RuntimeException("Ant Process completed improperly with Exit Value " + exitValue);
         }
      }
      catch (IOException ioe)
      {
         // The command could not be found
         if (antProcess == null)
         {
            throw new RuntimeException("Ensure Apache Ant is properly installed and available on your system's PATH",
                  ioe);
         }

         // Other I/O Error
         throw new RuntimeException(ioe);
      }
      catch (InterruptedException ie)
      {
         throw new RuntimeException(ie);
      }

   }

   /**
    * Performs cleanup operations after execution has completed
    */
   private void cleanup()
   {
      // Log
      System.out.println("Starting Cleanup...");

      // Remove installation directory
      this.rmAndChildren(this.getInstallationDirectory());

      // Log
      System.out.println("Cleanup Complete.");
   }

   /**
    * Copies the specified file to a file of same name in the specified directory
    * 
    * @param fileToCopy
    * @param destinationDirectory
    */
   private void copyFileFromJarToDirectory(JarFile jar, JarEntry fileToCopy, File destinationDirectory)
   {
      // Initialize
      InputStream in = null;
      OutputStream out = null;
      File destinationFile = new File(destinationDirectory, fileToCopy.getName());

      try
      {
         // Ensure Destination Directory Exists
         if (!destinationFile.getParentFile().exists())
         {
            // Make Destination Directory
            this.mkdirAndParents(destinationFile.getParentFile());
         }

         // Get Streams
         in = new BufferedInputStream(jar.getInputStream(fileToCopy));
         out = new FileOutputStream(destinationFile);
      }
      catch (IOException ioe)
      {
         throw new RuntimeException(ioe);
      }

      // Copy
      byte[] buffer = new byte[1024];
      int bytesRead = 0;
      try
      {
         while ((bytesRead = in.read(buffer)) != -1)
         {
            out.write(buffer, 0, bytesRead);
         }
      }
      catch (IOException ioe)
      {
         throw new RuntimeException(ioe);
      }

      // Log
      System.out.println("Copied " + fileToCopy.getName() + " to " + destinationFile.getAbsolutePath());
   }

   /**
    * Creates the directory and all requisite parents
    * 
    * @param entry
    */
   private void mkdirAndParents(File directory)
   {
      // If parent doesn't exist
      if (!directory.getParentFile().exists())
      {
         // Create the parent
         this.mkdirAndParents(directory.getParentFile());
      }

      // If this directory doesn't exist
      if (!directory.exists())
      {
         // Make the directory
         directory.mkdir();
         System.out.println("Created Directory " + directory.getAbsolutePath());
      }
   }

   /**
    * Removes the specified file or directory, and 
    * all children as necessary 
    * 
    * @param directory
    */
   private void rmAndChildren(File file)
   {
      // For all children
      File[] children = file.listFiles();
      if (children != null)
      {
         for (File child : file.listFiles())
         {
            // Remove the child
            this.rmAndChildren(child);
         }
      }

      // Remove
      System.out.println(file.getAbsolutePath() + " Removed.");
      file.delete();

   }

   /**
    * Ensures that the specified argument for JBoss AS Home
    * exists
    */
   private void ensureJbossHomeExists()
   {
      if (!this.getJbossAsInstallationDirectory().exists())
      {
         throw new RuntimeException("Specified JBoss AS Installation Directory, '"
               + this.getJbossAsInstallationDirectory().getAbsolutePath() + "', does not exist. ");
      }
   }

   // Inner Classes

   /**
    * Shutdown Hook
    * 
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $$
    */
   private class Shutdown extends Thread implements Runnable
   {

      @Override
      public void run()
      {
         // Call Super
         super.run();

         // Log
         System.out.println("Shutdown Hook called...");

         // Cleanup
         cleanup();

         // Log
         System.out.println("");
      }
   }

   /**
    * 
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $$
    */
   private class CaptureProcess extends Thread implements Runnable
   {
      // Instance Members
      Process process;

      // Constructors
      public CaptureProcess(Process process)
      {
         this.process = process;
      }

      @Override
      public void run()
      {
         // Call Super
         super.run();

         // Initialize
         int bytesRead = 0;
         byte[] buffer = new byte[1024];

         // Obtain InputStream of process
         InputStream in = this.process.getInputStream();

         // Read in and direct to stdout
         try
         {
            while ((bytesRead = in.read(buffer)) != -1)
            {
               System.out.write(buffer, 0, bytesRead);
            }
         }
         catch (IOException ioe)
         {
            throw new RuntimeException(ioe);
         }
      }
   }
}
