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
package org.jboss.ejb3.test.service;

import javax.ejb.Remote;
import javax.ejb.Local;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jboss.annotation.ejb.Service;
import org.jboss.ejb3.Container;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class ServiceSix implements ServiceSixLocal, ServiceSixRemote, ServiceSixManagement
{
   boolean called;
   
   int localMethodCalls;
   int remoteMethodCalls;
   int jmxAttribute;
   int someJmxAttribute;
   int otherJmxAttribute;
   int readWriteOnlyAttribute;
   
   StatelessRemote stateless;
   StatelessLocal statelessLocal;
   DataSource testDatasource;
   
   public void setStatelessLocal(StatelessLocal statelessLocal)
   {
      this.statelessLocal = statelessLocal;
   }
   
   public void testInjection() throws Exception
   {
      try 
      {
         stateless.test();
         statelessLocal.testLocal();
         testDatasource.getConnection();
         
         Context initCtx = new InitialContext();
         Context myEnv = (Context) initCtx.lookup(Container.ENC_CTX_NAME + "/env");
         Object obj = myEnv.lookup("res/aQueue");
         if ((obj instanceof javax.jms.Queue) == false)
            throw new NamingException("res/aQueue is not a javax.jms.Queue");
      } 
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }

   public boolean getCalled()
   {
      return called;
   }

   public void setCalled(boolean called)
   {
      this.called = called;
   }

   public void localMethod()
   {
      called = true;
   }

   public void remoteMethod()
   {
      called = true;
   }
   
   public String jmxOperation(String s)
   {
      return "x" + s + "x";
   }

   public String[] jmxOperation(String[] s)
   {
      for (int i = 0 ; i < s.length ; i++)
      {
         s[i] = jmxOperation(s[i]);
      }
      return s;
   }

   public int getAttribute()
   {
      return jmxAttribute;
   }

   public void setAttribute(int i)
   {
      jmxAttribute = i;
   }

   public int getSomeAttr()
   {
      return someJmxAttribute;
   }

   public void setSomeAttr(int i)
   {
      someJmxAttribute = i;
   }

   public int getOtherAttr()
   {
      return otherJmxAttribute;
   }

   public void setOtherAttr(int i)
   {
      otherJmxAttribute = i;
   }

   public void setWriteOnly(int i)
   {
      readWriteOnlyAttribute = i;
   }

   public int getReadOnly()
   {
      return readWriteOnlyAttribute;
   }


   public void create() throws Exception
   {
     
   }

   public void start() throws Exception
   {
    
   }

   public void stop()
   {
    
   }

   public void destroy()
   {
   
   }

}
