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
import java.net.URL;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.jboss.ejb3.test.dd.web.ejb.Address;
import org.jboss.ejb3.test.dd.web.interfaces.ReferenceTest;
import org.jboss.ejb3.test.dd.web.interfaces.Session30;
import org.jboss.ejb3.test.dd.web.interfaces.StatelessSession;
import org.jboss.ejb3.test.dd.web.interfaces.StatelessSessionLocal;
import org.jboss.ejb3.test.dd.web.util.Util;
import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;

/**
 *
 * tests injection annotations of @EJB, @PersistenceContext, @PersistenceUnit, etc...

 @author  Scott.Stark@jboss.org
 @version $Revision$
 */
public class EJBServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(EJBServlet.class);

   @EJB Session30 injectedSession30;
   @PersistenceContext(unitName="../dd-web-ejbs.jar#tempdb") EntityManager injectedEntityManager;
   @PersistenceUnit(unitName="../dd-web-ejbs.jar#tempdb") EntityManagerFactory injectedEntityManagerFactory;
   @Resource int nonOverridentConstant = 5;
   @Resource(name="overridenConstant") int overridenConstant = 1;
   @Resource UserTransaction tx;

   public void init(ServletConfig config) throws ServletException
   {
       String param = config.getInitParameter("failOnError");
       boolean failOnError = true;
       if( param != null && Boolean.valueOf(param).booleanValue() == false )
           failOnError = false;
       try
       {
           // Access the Util.configureLog4j() method to test classpath resource
           URL propsURL = Util.configureLog4j();
           log.debug("log4j.properties = "+propsURL);
       }
       catch(Exception e)
       {
           log.debug("failed", e);
           ClassLoader loader = Thread.currentThread().getContextClassLoader();
           try
           {
              log.debug(Util.displayClassLoaders(loader));
           }
           catch(NamingException ne)
           {
              log.debug("failed", ne);
           }
           if( failOnError == true )
               throw new ServletException("Failed to init EJBOnStartupServlet", e);
       }
   }
   
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      try
      {
         SecurityClient client = SecurityClientFactory.getSecurityClient();
         client.setSimple("jduke", "theduke");
         client.login();
         
         InitialContext ctx = new InitialContext();
        
         StatelessSession optimizedBean = (StatelessSession)ctx.lookup("java:comp/env/ejb/OptimizedEJB");
         optimizedBean.noop(new ReferenceTest(), false);
         
         StatelessSessionLocal localOptimizedBean = (StatelessSessionLocal)ctx.lookup("java:comp/env/ejb/local/OptimizedEJB");
         localOptimizedBean.noop(new ReferenceTest(), true);
         
         Session30 session30 = (Session30)ctx.lookup("java:comp/env/ejb/Session30");
         String access = session30.access();
         access = injectedSession30.access();
               
         //TODO EMPTYSTRING can't be right - correct lookup when fixed
         EntityManager entityManager = (EntityManager)ctx.lookup("java:/WebDDEntityManager");
         
         Address address = new Address();
         address.setStreet("Clarendon Street");
         address.setCity("Boston");
         address.setState("MA");
         address.setZip("02116");
         
         Address address2 = new Address();
         address.setStreet("Newbury Street");
         address.setCity("Boston");
         address.setState("MA");
         address.setZip("02115");

         Address address3 = new Address();
         address.setStreet("Clarendon Street");
         address.setCity("Boston");
         address.setState("MA");
         address.setZip("02116");

         //tx = (UserTransaction)ctx.lookup("UserTransaction");
         EntityManager em = injectedEntityManagerFactory.createEntityManager();
         tx.begin();
         entityManager.persist(address);
         injectedEntityManager.persist(address2);
         em.persist(address3);
         tx.commit();
         em.close();
      }
      catch (Exception e)
      {
         if (tx != null)
         {
            try {
               tx.rollback();
            } catch (Exception e1){
              
            }
         }
         throw new ServletException("Failed to call OptimizedEJB/Session30 through remote and local interfaces", e);
      }
      if (overridenConstant != 42) throw new RuntimeException("@Resource constant not overriden by XML");
      if (nonOverridentConstant != 5) throw new RuntimeException("@Resource constant should not have been overriden");
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>EJBServlet</title></head>");
      out.println("<body>Tests passed<br>Time:" + Util.getTime() + "</body>");
      out.println("</html>");
      out.close();
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
