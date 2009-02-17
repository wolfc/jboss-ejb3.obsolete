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
package org.jboss.ejb3.test.schema.unit;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jboss.test.JBossTestCase;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * A DDValidationUnitTestCase.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class DDValidationUnitTestCase extends JBossTestCase
{   
   private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
   static
   {
      FACTORY.setValidating(true);
      FACTORY.setNamespaceAware(true);
   }

   private static final File RESOURCES;

   // files to ignore
   private static final Set<String> IGNORE_ABS_PATHS = new HashSet<String>();
   
   // a set of file name patterns that should be validated
   private static final Set<Pattern> VALIDATE_PATTERNS = new HashSet<Pattern>();

   static
   {
      // running from eclipse and command line the current dir is different
      File curFile = new File(System.getProperty("user.dir"));
      if(curFile.getName().endsWith("target"))
         curFile = curFile.getParentFile();
      RESOURCES = new File(curFile, "src" + File.separatorChar + "test" + File.separatorChar + "resources" + File.separatorChar + "test");
      
      VALIDATE_PATTERNS.add(Pattern.compile("ejb-jar.*\\.xml")); // ejb-jar.xml pattern
      VALIDATE_PATTERNS.add(Pattern.compile("jboss[^\\-]*\\.xml")); // jboss.xml pattern
      VALIDATE_PATTERNS.add(Pattern.compile("jboss-reference(.)*\\.xml")); // these are in fact jboss.xml
      VALIDATE_PATTERNS.add(Pattern.compile("jboss-app(.)*\\.xml")); // jboss-app.xml pattern
      VALIDATE_PATTERNS.add(Pattern.compile("application.*\\.xml")); // application, application-client pattern
      
      ignore("schema/META-INF/jboss.xml");
      ignore("schema/META-INF/ejb-jar.xml");
   }
   
   /**
    * @param path  relative to the RESOURCES to ignore
    */
   private static void ignore(String path)
   {
      if('/' != File.separatorChar)
         path = path.replace('/', File.separatorChar);
      path = RESOURCES.getAbsolutePath() + File.separatorChar + path;
      IGNORE_ABS_PATHS.add(path);
   }

   private int total;
   private int invalid;

   public DDValidationUnitTestCase(String name)
   {
      super(name);
   }

   protected void configureLogging()
   {
      //enableTrace("org.jboss.util.xml.JBossEntityResolver");
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      total = 0;
      invalid = 0;
      configureLogging();
   }

   public void testValidation() throws Exception
   {
      List<String> invalidList = new ArrayList<String>();
      scan(RESOURCES, invalidList, false);
      assertTrue("No descriptors were found", total > 0);
      assertEquals(invalid + " out of " + total + " are invalid (see logs for details)", 0, invalid);
   }

   /**
    * @param f  the directory in which to search for files
    * @param names  the files to validate
    * @param invalidList  a list of error messages
    * @param failIfInvalid  whether to fail immediately after the first invalid file
    */
   private void scan(java.io.File f, final List<String> invalidList, final boolean failIfInvalid)
   {
      f.listFiles(new FileFilter()
      {
         public boolean accept(File pathname)
         {
            if (pathname.isDirectory())
            {
               scan(pathname, invalidList, failIfInvalid);
               return true;
            }

            if(IGNORE_ABS_PATHS.contains(pathname.getAbsolutePath()))
               return false;

            for(Iterator<Pattern> i = VALIDATE_PATTERNS.iterator(); i.hasNext();)
            {
               Pattern p = i.next();
               if(p.matcher(pathname.getName()).matches())
               {
                  ++total;
                  if (!validate(pathname, invalidList, failIfInvalid))
                     ++invalid;
                  return false;
               }
            }
            
            return false;
         }
      });
   }

   private boolean validate(File file, List<String> invalidList, boolean failIfInvalid)
   {
      InputStream is;
      try
      {
         is = file.toURL().openStream();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to open file: " + file.getAbsolutePath(), e);
      }

      boolean valid;
      try
      {
         JBossEntityResolver resolver = new JBossEntityResolver();
         resolver.registerLocalEntity("http://www.jboss.com/xml/ns/javaee", "jboss_5_0.xsd");
         parse(is, resolver);
         valid = true;
      }
      catch (JBossXBRuntimeException e)
      {
         valid = false;
         if (e.getCause() instanceof SAXException)
         {
            SAXException sax = (SAXException) e.getCause();

            StringBuffer msg = new StringBuffer();
            msg.append("Failed to parse: ").append(file.getAbsolutePath()).append(": ").append(sax.getMessage());

            if (sax instanceof SAXParseException)
            {
               SAXParseException parseException = (SAXParseException) sax;
               msg.append(" [").append(parseException.getLineNumber()).append(",").append(
                     parseException.getColumnNumber()).append("]");
            }

            if (failIfInvalid)
            {
               fail(msg.toString());
            }
            else
            {
               getLog().error(msg.toString());
               System.err.println(msg.toString());
            }

            invalidList.add(msg.toString());
         }
         else
         {
            throw e;
         }
      }

      return valid;
   }

   private static void parse(InputStream xmlIs, final EntityResolver resolver)
   {
      SAXParser parser;
      try
      {
         parser = FACTORY.newSAXParser();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to instantiate a SAX parser: " + e.getMessage());
      }

      try
      {
         parser.getXMLReader().setFeature("http://apache.org/xml/features/validation/schema", true);
      }
      catch (SAXException e)
      {
         throw new IllegalStateException("Schema validation feature is not supported by the parser: " + e.getMessage());
      }

      try
      {
         parser.parse(xmlIs, new DefaultHandler()
         {
            public void warning(SAXParseException e)
            {
            }

            public void error(SAXParseException e)
            {
               throw new JBossXBRuntimeException("Error", e);
            }

            public void fatalError(SAXParseException e)
            {
               throw new JBossXBRuntimeException("Fatal error", e);
            }

            public InputSource resolveEntity(String publicId, String systemId)
            {
               InputSource is = null;
               if (resolver != null)
               {
                  try
                  {
                     is = resolver.resolveEntity(publicId, systemId);
                  }
                  catch (Exception e)
                  {
                     throw new IllegalStateException("Failed to resolveEntity " + systemId + ": " + systemId);
                  }
               }

               if(is == null)
               {
                  fail("Failed to resolve entity: publicId=" + publicId + " systemId=" + systemId);
               }

               return is;
            }
         });
      }
      catch(JBossXBRuntimeException e)
      {
         throw e;
      }
      catch (SAXException e)
      {
         throw new JBossXBRuntimeException("Parsing failed.", e);
      }
      catch (IOException e)
      {
         throw new JBossXBRuntimeException("Parsing failed.", e);
      }
   }

}
