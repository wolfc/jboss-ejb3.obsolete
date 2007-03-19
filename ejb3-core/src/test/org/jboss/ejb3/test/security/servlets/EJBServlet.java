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
package org.jboss.ejb3.test.security.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import org.jboss.ejb3.test.security.JobDAO;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import javax.security.auth.login.LoginContext;


/** A servlet that accesses an EJB and tests whether the call argument
 is serialized.

 @author  Scott.Stark@jboss.org
 @version $Revision$
 */
public class EJBServlet extends HttpServlet
{
   private static final Logger log = Logger.getLogger(EJBServlet.class);
   
   public void init(ServletConfig config) throws ServletException
   {
   }
   
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      try
      {
         UsernamePasswordHandler handler = new UsernamePasswordHandler("scott", "echoman"); 
         
         LoginContext lc = new LoginContext("client-login", handler); 
         lc.login(); 
         
         InitialContext jndiContext = new InitialContext();
         JobDAO bean = (JobDAO)jndiContext.lookup("JobDAOBean/local");
         bean.foo();
      } catch (Exception e)
      {
         e.printStackTrace();
         throw new ServletException(e);
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
