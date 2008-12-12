/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.testsuite.knownissue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * Per default all known issues will fail. To override specify
 * a known issues properties file via -DknownIssues=filename.
 * 
 * This file contains the action to take when a known issue is encountered.
 * 
 * The following actions are allowed:
 * - fail : fail the test (set this when an issue is resolved)
 * - ignore : do nothing
 * - show : show the issue on System.err
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public abstract class KnownIssues
{
   public static enum Action
   {
      FAIL,
      IGNORE,
      SHOW
   };
   
   private static Properties actions = null;
   
   private KnownIssues()
   {
   }
   
   public static Action getAction(String issue)
   {
      Properties actions = getActions();
      if(actions == null)
         return Action.FAIL;
      String action = actions.getProperty(issue);
      if(action == null)
         return Action.FAIL;
      action = action.toUpperCase();
      return Action.valueOf(action);
   }
   
   private static Properties getActions()
   {
      if(actions != null)
         return actions;
      
      actions = new Properties();
      String fileName = System.getProperty("knownIssuesFile");
      if(fileName == null || fileName.length() == 0)
         return actions;
      
      try
      {
         File file = new File(fileName);
         FileInputStream in = new FileInputStream(file.getAbsolutePath());
         try
         {
            if(fileName.endsWith(".xml"))
               actions.loadFromXML(in);
            else
               actions.load(in);
         }
         finally
         {
            in.close();
         }
         return actions;
      }
      catch(IOException e)
      {
         throw new RuntimeException(e);
      }
   }
}
