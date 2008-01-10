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
package org.jboss.ejb3.test.dd.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.jboss.ejb3.test.dd.web.interfaces.ReferenceTest;
import org.jboss.ejb3.test.dd.web.interfaces.StatelessSession;
import org.jboss.ejb3.test.dd.web.interfaces.StatelessSessionLocal;
import org.jboss.ejb3.test.dd.web.util.Util;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * A servlet that accesses an EJB and tests the speed of optimized versus
 * non-optimized invocations.
 * 
 * @author Adrian.Brock@HappeningTimes.com
 * @version $Revision$
 */
public class SpeedServlet extends HttpServlet
{
   private static final Logger log = Logger.getLogger(SpeedServlet.class);
   
   public static final int REPEATS = 10;

   public static final int ITERATIONS = 100;

   protected long[] runRemoteTest(StatelessSession bean, boolean optimized)
         throws Exception
   {
      ReferenceTest test = new ReferenceTest();
      long[] results = new long[REPEATS];
      for (int loop = 0; loop < REPEATS; loop++)
      {
         long start = System.currentTimeMillis();
         for (int i = 0; i < ITERATIONS; i++)
            bean.noop(test, optimized);
         results[loop] = System.currentTimeMillis() - start;
      }
      return results;
   }

   protected void displayResults(PrintWriter out, long[] results)
         throws IOException
   {
      long total = 0;
      out.print("<table><tr>");
      for (int i = 0; i < results.length; i++)
      {
         total += results[i];

         out.print("<td>" + results[i] + " ms</td>");
      }
      out.println("</tr></table><br />");
      out.println("Total time  = " + total + " ms<br />");
      out.println("Invocations = " + ITERATIONS * REPEATS);
      float average = total * 1000;
      average /= (ITERATIONS * REPEATS);
      out.println("Average time= " + average + " micro-seconds<br />");
   }
   
   private void testNaming()
   {
      lookup("");
      lookup("jmx");
      lookup("java:comp");
      lookup("java:comp/env");
      lookup("java:comp/env/ejb");
      lookup("java:comp/env/ejb/local");
      lookup("containers");
      lookup("jbosstest");
      lookup("jbosstest/ejbs");
      lookup("local");
      lookup("dd");
      lookup("dd/web");
   }
   
   private void lookup(String name)
   {
      try {
         log.info("lookup " + name);
         InitialContext jndiContext = new InitialContext();
         javax.naming.NamingEnumeration names = jndiContext.list(name);
         if (names != null){
            while (names.hasMore()){
               log.info("  " + names.next());
            }
         }
      } catch (Exception e){
      }
   }

   protected void processRequest(HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException
   {
      testNaming();
      
      SecurityAssociation.setPrincipal(new SimplePrincipal("jduke"));
      SecurityAssociation.setCredential("theduke".toCharArray());

      long[] optimized = null;
      long[] notOptimized = null;
      try
      {
         InitialContext ctx = new InitialContext();

         StatelessSession bean = (StatelessSession) ctx.lookup("OptimizedEJB");
         optimized = runRemoteTest(bean, true);

         bean = (StatelessSession) ctx.lookup("dd/web/NotOptimizedEJB");
         
         notOptimized = runRemoteTest(bean, false);
      } catch (Exception e)
      {
         throw new ServletException("Failed to run speed tests", e);
      }
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>SpeedServlet</title></head>");
      out.println("<body>");
      out.println("Number of invocations=" + ITERATIONS + " repeated "
            + REPEATS + " times.<br />");
      out.println("<h2>ejb/OptimizedEJB</h2>");
      displayResults(out, optimized);
      out.println("<h2>ejb/NotOptimizedEJB</h2>");
      displayResults(out, notOptimized);
      out.println("</body>");
      out.println("</html>");
      out.close();
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      processRequest(request, response);
   }

   protected void doPost(HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException
   {
      processRequest(request, response);
   }
}
