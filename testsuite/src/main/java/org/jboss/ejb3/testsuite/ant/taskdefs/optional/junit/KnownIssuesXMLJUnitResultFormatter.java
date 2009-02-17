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
package org.jboss.ejb3.testsuite.ant.taskdefs.optional.junit;

import java.lang.reflect.Method;
import java.util.Hashtable;

import junit.framework.Test;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitVersionHelper;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.jboss.ejb3.testsuite.knownissue.KnownIssues;
import org.jboss.ejb3.testsuite.lang.reflect.Invoker;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class KnownIssuesXMLJUnitResultFormatter extends XMLJUnitResultFormatter
{
   private static Method formatErrorMethod;
   static {
      formatErrorMethod = Invoker.getMethod(XMLJUnitResultFormatter.class, "formatError", String.class, Test.class, Throwable.class);
   }
   
   protected Hashtable<Test, Test> failedTests;
   private long runCount = 0, failures = 0, errors = 0;
   
   public KnownIssuesXMLJUnitResultFormatter()
   {
      super();
      this.failedTests = Invoker.getFieldValue(this, XMLJUnitResultFormatter.class, "failedTests");
   }
   
   @Override
   public void addError(Test test, Throwable t)
   {
      errors++;
      super.addError(test, t);
   }
   
   @Override
   public void addFailure(Test test, Throwable t)
   {
      String className = test.getClass().getCanonicalName();
      String name = className + "." + JUnitVersionHelper.getTestCaseName(test);
      KnownIssues.Action action = KnownIssues.getAction(name);
      switch(action)
      {
         case FAIL:
            failures++;
            super.addFailure(test, t);
            return;
         case IGNORE:
            return;
         case SHOW:
            formatError("known-issue", test, t);
            // since nobody understand the above, let's paste it to stderr also
            t.printStackTrace();
            // it's not really a failure, so don't count it
            return;
      }
      throw new RuntimeException("Illegal action encountered " + action + " on test " + name);
   }
   
   @Override
   public void endTest(Test test)
   {
      // formatError also calls endTest
      if(!failedTests.contains(test))
         runCount += test.countTestCases();
      super.endTest(test);
   }
   
   @Override
   public void endTestSuite(JUnitTest suite) throws BuildException
   {
      long oldRunCount = suite.runCount();
      assert runCount == oldRunCount : "runCount (" + runCount + ") != oldRunCount (" + oldRunCount + ")";
      assert (failures + errors) <= runCount : "(failures + errors) > runCount";
      suite.setCounts(runCount, failures, errors);
      super.endTestSuite(suite);
   }
   
   protected void formatError(String type, Test test, Throwable t)
   {
      // expose formatError the hacky way
      Invoker.invoke(formatErrorMethod, this, type, test, t);
   }
   
   @Override
   public void startTestSuite(JUnitTest suite)
   {
      runCount = 0;
      failures = 0;
      errors = 0;
      super.startTestSuite(suite);
   }
}
