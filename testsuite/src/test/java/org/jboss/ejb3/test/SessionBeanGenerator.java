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
package org.jboss.ejb3.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Generate a large stateless session bean to test deployment times
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public class SessionBeanGenerator
{
   public static void main(String[] args) throws Exception
   {
      File intf = new File("BigInterface.java");
      File bean = new File("BigBean.java");
      FileOutputStream intfFp = new FileOutputStream(intf);
      FileOutputStream beanFp = new FileOutputStream(bean);
      PrintWriter intfWriter = new PrintWriter(intfFp);
      PrintWriter beanWriter = new PrintWriter(beanFp);

      intfWriter.println("package org.jboss.tutorial.simple;");
      intfWriter.println();
      intfWriter.println("public interface BigInterface");
      intfWriter.println("{");

      beanWriter.println("package org.jboss.tutorial.simple;");
      beanWriter.println();
      beanWriter.println("import javax.ejb.Stateless;");
      beanWriter.println();
      beanWriter.println("@Stateless");
      beanWriter.println("public class BigBean implements BigInterface");
      beanWriter.println("{");

      for (int i = 0; i < 100; i++)
      {
         String methodSignature = "   public void method" + i + "(String param)";
         intfWriter.println(methodSignature + ";");
         beanWriter.println(methodSignature + " {}");
      }

      beanWriter.println("}");
      intfWriter.println("}");
      beanWriter.flush();
      intfWriter.flush();
      intfFp.close();
      beanFp.close();
   }
}
