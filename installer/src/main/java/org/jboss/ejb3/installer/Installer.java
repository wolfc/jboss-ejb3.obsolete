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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.ejb3.common.thread.RedirectProcessOutputToSystemOutThread;

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
    * Current Classpath
    */
   private static final String SYSTEM_PROPERTY_CLASS_PATH = "java.class.path";

   /*
    * Environment Property key for JBOSS_HOME
    */
   private static final String ENV_PROPERTY_JBOSS_HOME = "JBOSS_HOME";

   /*
    * Environment Property key for ANT_HOME 
    */
   private static final String ENV_PROPERTY_ANT_HOME = "ANT_HOME";

   /*
    * Environment Property key for ANT_CMD 
    */
   private static final String ENV_PROPERTY_ANT_CMD = "ANT_CMD";

   /*
    * Environment Property key for the Installation location
    */
   private static final String ENV_PROPERTY_INSTALL_LOCATION = "JBOSS_EJB3_PLUGIN_INSTALL_HOME";

   /*
    * Namespace of the installer
    */
   private static final String NAMESPACE_DIRECTORY_INSTALLER = "jbossas-ejb3-plugin-installer";

   /*
    * Location of the Libraries
    */
   private static final String FILENAME_LIB_DIRECTORY = "lib";

   /*
    * Location of the Configuration
    */
   private static final String FILENAME_CONF_DIRECTORY = "conf";

   /*
    * Apache Ant executable
    */
   private static final String COMMAND_ANT = "ant";

   /*
    * Extension to append to Windows-based systems
    */
   private static final String COMMAND_EXTENSION_BATCH = ".bat";

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
   
   private boolean cleanup;

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
      Installer installer = new Installer(jbossDir, args.length == 1);

      // Install
      installer.install();
   }

   // Constructor
   public Installer(String jbossAsInstallationDirectory, boolean cleanup)
   {
      // Set JBoss AS Install Location
      this.setJbossAsInstallationDirectory(new File(jbossAsInstallationDirectory));
      this.cleanup = cleanup;
   }

   /**
    * Runs the installation process
    */
   public void install()
   {
      // Log
      this.getPrintStream().println("\n*****************************************");
      this.getPrintStream().println("|| JBossAS 5.0.x EJB3 Plugin Installer ||");
      this.getPrintStream().println("*****************************************\n");
      this.getPrintStream().println("Installing EJB3 Libraries to Temp Directory...");

      // Add Shutdown Hook
      if(cleanup)
         Runtime.getRuntime().addShutdownHook(new Shutdown());

      // Ensure Installation is clean
      this.cleanup();

      // Ensure JBOSS_HOME exists
      this.ensureJbossHomeExists();

      // For each Library
      for (JarEntry library : this.getAllLibraries())
      {
         // Copy to the installer temp directory
         this.copyFileFromJarToDirectory(this.getInstallerJarFile(), library, this.getInstallationDirectory());
      }

      // For Configuration File
      for (JarEntry conf : this.getAllConfigurationFiles())
      {
         // Copy to the installer temp directory
         this.copyFileFromJarToDirectory(this.getInstallerJarFile(), conf, this.getInstallationDirectory());
      }

      for(JarEntry pkg : getAllJarEntriesInDirectory("packages"))
      {
         copyFileFromJarToDirectory(this.getInstallerJarFile(), pkg, this.getInstallationDirectory());
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
         this.getPrintStream().println("JBoss EJB3 Plugin Installation Directory: " + this.installationDirectory);
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
    * Returns all Libraries as references
    */
   private List<JarEntry> getAllLibraries()
   {
      return this.getAllJarEntriesInDirectory(Installer.FILENAME_LIB_DIRECTORY);
   }

   /**
    * Returns all configuration files as references
    */
   private List<JarEntry> getAllConfigurationFiles()
   {
      return this.getAllJarEntriesInDirectory(Installer.FILENAME_CONF_DIRECTORY);
   }

   /**
    * Returns all references in the specified directory
    * 
    * @param directory The directory
    */
   private List<JarEntry> getAllJarEntriesInDirectory(String directory)
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
         if (entry.getName().startsWith(directory) && !entry.isDirectory())
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

      try
      {
         // Get the Process
         Process antProcess = this.getAntProcess();

         // Capture the output
         Thread captureProcess = new RedirectProcessOutputToSystemOutThread(antProcess);
         captureProcess.start();

         // Ensure proper completion, block until done
         int exitValue = antProcess.waitFor();
         if (exitValue == 0)
         {
            this.getPrintStream().println("Ant Build Completed");
         }
         else
         {
            throw new RuntimeException("Ant Process completed improperly with Exit Value " + exitValue);
         }
      }
      catch (IOException ioe)
      {
         // Other I/O Error
         throw new RuntimeException(ioe);
      }
      catch (InterruptedException ie)
      {
         throw new RuntimeException(ie);
      }

   }

   /**
    * Obtains the Ant Process
    * 
    * @return
    * @throws IOException
    */
   private Process getAntProcess() throws IOException
   {
      return this.getAntProcess(false);
   }

   /**
    * Obtains the Ant Process.  If "useBatchExtension" is false, no extension will 
    * be added on first attempt, but the batch extension will be tried if the first 
    * try without it fails.
    * 
    * @param useBatchExtension
    * @return
    * @throws IOException
    */
   private Process getAntProcess(boolean useBatchExtension) throws IOException
   {
      // Initialize
      Process antProcess = null;
      String buildfile = this.getInstallationDirectory() + File.separator + Installer.FILENAME_BUILDFILE;

      // Try
      String antCommandPath = System.getenv(Installer.ENV_PROPERTY_ANT_CMD);
      if (antCommandPath == null)
      {
         // Obtain ANT_HOME and ensure specified
         String antHome = System.getenv(Installer.ENV_PROPERTY_ANT_HOME);
         if (antHome == null || "".equals(antHome))
         {
            throw new RuntimeException("Environment Variable '" + Installer.ENV_PROPERTY_ANT_HOME
                  + "' must be specified.");
         }
         this.getPrintStream().println("Using ANT_HOME: " + antHome);

         // Construct "ant" command path
         antCommandPath = antHome + File.separator + "bin" + File.separator + Installer.COMMAND_ANT;
      }

      // If we should use the batch extension
      if (useBatchExtension)
      {
         // Add batch extension
         antCommandPath = antCommandPath + Installer.COMMAND_EXTENSION_BATCH;
      }

      // Construct the Process
      ProcessBuilder antProcessBuilder = new ProcessBuilder(antCommandPath, Installer.SWITCH_ANT_BUILDFILE, buildfile);
      antProcessBuilder.redirectErrorStream(true);
      antProcessBuilder.environment().put(Installer.ENV_PROPERTY_JBOSS_HOME,
            this.getJbossAsInstallationDirectory().getAbsolutePath());
      antProcessBuilder.environment().put(Installer.ENV_PROPERTY_INSTALL_LOCATION,
            this.getInstallationDirectory().getAbsolutePath());

      try
      {
         // Start the Process
         this.getPrintStream().println(
               "Starting Ant> " + antCommandPath + " " + Installer.SWITCH_ANT_BUILDFILE + " " + buildfile);
         antProcess = antProcessBuilder.start();
      }
      catch (IOException ioe)
      {
         // The command could not be found, and we've tried the batch extension
         if (antProcess == null && useBatchExtension)
         {
            throw new RuntimeException(
                  "Ensure Apache Ant is properly installed and Environment Variable ANT_HOME is set", ioe);
         }
         // The command could not be found, but we haven't yet tried the batch extension
         else if (antProcess == null && !useBatchExtension)
         {
            return this.getAntProcess(true);
         }
         // Miscellaneous IOException
         else
         {
            throw ioe;
         }
      }

      // Return
      return antProcess;
   }

   /**
    * Performs cleanup operations after execution has completed
    */
   private void cleanup()
   {
      // Log
      this.getPrintStream().println("Starting Cleanup...");

      // Remove installation directory
      this.rmAndChildren(this.getInstallationDirectory());

      // Log
      this.getPrintStream().println("Cleanup Complete.");
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
      finally
      {
         // Close the Streams
         try
         {
            in.close();
            out.close();
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }

      // Log
      this.getPrintStream().println("Copied " + fileToCopy.getName() + " to " + destinationFile.getAbsolutePath());
   }

   /**
    * Creates the directory and all requisite parents
    * 
    * @param entry
    */
   private void mkdirAndParents(File directory)
   {
      try
      {
         if (!directory.mkdirs())
         {
            throw new IOException("Could not make directory " + directory.toString());
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
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
      // Only delete if exists
      if (!file.exists())
      {
         return;
      }

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
      boolean isDir = file.isDirectory();
      boolean removed = file.delete();
      if (removed && isDir)
      {
         // Uncomment to log for debugging
         //this.getPrintStream().println(file.getAbsolutePath() + " Removed.");
      }
      else if (removed)
      {
         // Removed a file, do nothing (too verbose to log here)
      }
      // Error in deletion
      else
      {
         this.getPrintStream().println("Unable to remove " + file.getAbsolutePath());
      }

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

   /**
    * Return the Output Stream
    * @return
    */
   private PrintStream getPrintStream()
   {
      return System.out;
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
         getPrintStream().println("Shutdown Hook called...");

         // Cleanup
         cleanup();

         // Log
         getPrintStream().println("");
      }
   }
}
