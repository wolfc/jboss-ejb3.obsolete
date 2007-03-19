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
package org.jboss.ejb3.test.servlet.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.test.servlet.Session30;
import org.jboss.ejb3.test.servlet.Session30LocalHome;
import org.jboss.ejb3.test.servlet.Session30Home;
import org.jboss.ejb3.test.servlet.TestObject;
import org.jboss.ejb3.test.servlet.WarTestObject;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/** A servlet that accesses an EJB and tests whether the call argument
 is serialized.

 @author  Scott.Stark@jboss.org
 @version $Revision$
 */
public class EJBServlet extends HttpServlet
{
   private static final Logger log = Logger.getLogger(EJBServlet.class);
   
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      try
      {
         SecurityAssociation.setPrincipal(new SimplePrincipal("somebody"));
         SecurityAssociation.setCredential("password".toCharArray());
         
         InitialContext ctx = new InitialContext();
        
         Session30 session = (Session30)ctx.lookup("ejb/Session30");
         
         session.hello();
         session.goodbye();
         
         TestObject o = new TestObject();
         session.access(o);
         o = session.createTestObject();
         
         session = (Session30)ctx.lookup("ejb/Session30Local");
         o = new TestObject();
         session.access(o);
         o = session.createTestObject();
         
         WarTestObject warObject = (WarTestObject)session.getWarTestObject();
         
         Session30Home home = (Session30Home)ctx.lookup("Session30/home");
         session = home.create();
         session.access(o);
         
         Session30LocalHome localHome = (Session30LocalHome)ctx.lookup("Session30/localHome");
         session = localHome.create();
         session.access(o);
         
         home = (Session30Home)ctx.lookup("java:comp/env/ejb/remote/Session30");
         session = home.create();
         session.access(o);
         
         localHome = (Session30LocalHome)ctx.lookup("java:comp/env/ejb/local/Session30");
         session = localHome.create();
         session.access(o);
      }
      catch (Exception e)
      {
         e.printStackTrace();
        
         throw new ServletException("Failed to call OptimizedEJB/Session30 through remote and local interfaces", e);
      }
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>EJBServlet</title></head>");
      out.println("<body>Tests passed<br></body>");
      out.println("</html>");
      out.close();
   }
   
   private void lookup(String name)
   {
      log.info("lookup " + name);
      try {
         InitialContext jndiContext = new InitialContext();
         NamingEnumeration names = jndiContext.list(name);
         if (names != null){
            while (names.hasMore()){
               log.info("  " + names.next());
            }
         }
      } catch (Exception e){
      }
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      processRequest(request, response);
   }

   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      processRequest(request, response);
   }
}
